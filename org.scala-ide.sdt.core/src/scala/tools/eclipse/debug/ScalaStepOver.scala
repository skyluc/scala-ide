package scala.tools.eclipse.debug

import com.sun.jdi.request.StepRequest
import com.sun.jdi.request.EventRequest
import JDIUtil._
import com.sun.jdi.ReferenceType
import com.sun.jdi.Method
import com.sun.jdi.ThreadReference
import com.sun.jdi.request.ClassPrepareRequest
import com.sun.jdi.request.BreakpointRequest
import scala.collection.mutable.Buffer
import org.eclipse.jdt.internal.debug.core.IJDIEventListener
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget
import com.sun.jdi.event.EventSet
import com.sun.jdi.event.Event
import com.sun.jdi.event.ClassPrepareEvent
import com.sun.jdi.event.StepEvent
import org.eclipse.debug.core.DebugEvent
import com.sun.jdi.event.BreakpointEvent

object ScalaStepOver {

  def apply(scalaStackFrame: ScalaStackFrame): ScalaStepOver = {
    
    // TODO : two step process is weird and might not be needed and dangerous
    import scala.collection.JavaConverters._
    
    val eventRequestManager= scalaStackFrame.stackFrame.virtualMachine.eventRequestManager
    
    val classPrepareRequest = eventRequestManager.createClassPrepareRequest
    val location = scalaStackFrame.stackFrame.location
    classPrepareRequest.addClassFilter(location.declaringType.name + "$*")
    
    val stepOverRequest = eventRequestManager.createStepRequest(scalaStackFrame.stackFrame.thread, StepRequest.STEP_LINE, StepRequest.STEP_OVER)
    stepOverRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD)

    // find anonFunction in range
    val currentMethodLastLine = methodToLines(location.method).max 
    
    val range = Range(location.lineNumber, (location.method.declaringType.methods.asScala.flatten(methodToLines(_)).filter(_ > currentMethodLastLine) :+ Int.MaxValue).min)

    val loadedAnonFunctionsInRange = location.method.declaringType.nestedTypes.asScala.flatMap(anonFunctionsInRange(_, range))

    // if we are in an anonymous function, add the method
    if (location.declaringType.name.contains("$$anonfun$")) {
      loadedAnonFunctionsInRange ++= scalaStackFrame.getScalaDebugTarget.findAnonFunction(location.declaringType)
    }

    val entryBreakpoints = loadedAnonFunctionsInRange.map(createMethodEntryBreakpoint(_, scalaStackFrame.stackFrame.thread))
    
    new ScalaStepOver(scalaStackFrame.getScalaDebugTarget, range, scalaStackFrame.thread, classPrepareRequest, stepOverRequest, entryBreakpoints)
  }

  def createMethodEntryBreakpoint(method: Method, thread: ThreadReference) = {
    import scala.collection.JavaConverters._
    
    val breakpointRequest= thread.virtualMachine.eventRequestManager.createBreakpointRequest(method.location)
    breakpointRequest.addThreadFilter(thread)    

    breakpointRequest
  }

  
  // TODO: use ScalaDebugTarget#findAnonFunction
  def anonFunctionsInRange(refType: ReferenceType, range: Range) = {
    import scala.collection.JavaConverters._
    val methods = refType.methods.asScala.filter(method =>
      range.contains(method.location.lineNumber) && method.name.startsWith("apply"))
      
    // TODO: using isBridge was not working with List[Int]. Should check if we can use it by default with some extra checks when it fails.
//      methods.find(!_.isBridge)
      
    methods.size match {
      case 3 =>
        // method with primitive parameter
        methods.find(_.name.startsWith("apply$")).orElse({
          // method with primitive return type (with specialization in 2.10.0)
          methods.find(! _.signature.startsWith("(Ljava/lang/Object;)"))
        })
      case 2 =>
        methods.find(_.signature != "(Ljava/lang/Object;)Ljava/lang/Object;")
      case 1 =>
        methods.headOption
      case _ =>
        None
    }
  }
}

class ScalaStepOver(target: ScalaDebugTarget, range: Range, thread: ScalaThread, classPrepareRequest: ClassPrepareRequest, stepOverRequest: StepRequest, entryBreakpoints: Buffer[BreakpointRequest]) extends IJDIEventListener with ScalaStep {
  import ScalaStepOver._
  /* from IJDIEventListener */

  def eventSetComplete(event: Event, target: JDIDebugTarget, suspend: Boolean, eventSet: EventSet): Unit = {
    // nothing to do
  }

  def handleEvent(event: Event, javaTarget: JDIDebugTarget, suspendVote: Boolean, eventSet: EventSet): Boolean = {
    event match {
      case classPrepareEvent: ClassPrepareEvent =>
        anonFunctionsInRange(classPrepareEvent.referenceType, range).foreach(method => {
          val breakpoint = createMethodEntryBreakpoint(method, thread.thread)
          entryBreakpoints += breakpoint
          javaTarget.getEventDispatcher.addJDIEventListener(this, breakpoint)
          breakpoint.enable
        })
        true
      case stepEvent: StepEvent =>
        if (target.isValidLocation(stepEvent.location)) {
          stop
          thread.suspendedFromScala(DebugEvent.STEP_OVER)
          false
        } else {
          true
        }
      case breakpointEvent: BreakpointEvent =>
        stop
        thread.suspendedFromScala(DebugEvent.STEP_OVER)
        false
      case _ =>
        suspendVote
    }
  }
  
  // ----
  
  def step() {
    val eventDispatcher= target.javaTarget.getEventDispatcher
    
    // TODO: they are all request, and have the same life cycle, they can be combined in one collection
    
    eventDispatcher.addJDIEventListener(this, classPrepareRequest)
    classPrepareRequest.enable
    
    entryBreakpoints.foreach(breakpoint => {
      eventDispatcher.addJDIEventListener(this, breakpoint)
      breakpoint.enable
    })
    
    eventDispatcher.addJDIEventListener(this, stepOverRequest)
    stepOverRequest.enable
    thread.resumedFromScala(DebugEvent.STEP_OVER)
    thread.thread.resume
  }
  
  def stop() {
    val eventDispatcher= target.javaTarget.getEventDispatcher
    
    val eventRequestManager= thread.thread.virtualMachine.eventRequestManager
    
    classPrepareRequest.disable
    eventDispatcher.removeJDIEventListener(this, classPrepareRequest)
    eventRequestManager.deleteEventRequest(classPrepareRequest)
    
    entryBreakpoints.foreach(breakpoint => {
      breakpoint.disable
      eventDispatcher.removeJDIEventListener(this, breakpoint)
      eventRequestManager.deleteEventRequest(breakpoint)
    })
    
    stepOverRequest.disable
    eventDispatcher.removeJDIEventListener(this, stepOverRequest)
    eventRequestManager.deleteEventRequest(stepOverRequest)
    
  }

}