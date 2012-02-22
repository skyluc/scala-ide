package scala.tools.eclipse.debug

import scala.Option.option2Iterable
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mutableMapAsJavaMapConverter
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.debug.core.model.IBreakpoint
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint
import org.eclipse.jdt.debug.core.JDIDebugModel
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame
import org.eclipse.jdt.internal.debug.core.IJDIEventListener
import com.sun.jdi.event.ClassPrepareEvent
import com.sun.jdi.event.Event
import com.sun.jdi.event.EventSet
import com.sun.jdi.request.ClassPrepareRequest
import com.sun.jdi.Method
import com.sun.jdi.ReferenceType
import JDIUtil._
import ScalaStepOver.anonFunctionsInRange
import ScalaStepOver.createMethodEntryBreakpoint
import org.eclipse.debug.core.IDebugEventSetListener
import org.eclipse.debug.core.DebugEvent
import org.eclipse.jdt.internal.debug.core.model.JDIThread
import org.eclipse.debug.core.model.IThread
import org.eclipse.debug.core.DebugPlugin

object ScalaStepOver {

  def apply(stackFrame: JDIStackFrame): ScalaStepOver = {
    import scala.collection.JavaConverters._

    val target = stackFrame.getJavaDebugTarget

    val classPrepareRequest = target.createClassPrepareRequest(stackFrame.getDeclaringTypeName + "$*", null, false)

    val currentMethod = stackFrame.getUnderlyingMethod

    val currentMethodLastLine = methodToLines(currentMethod).max

    val range = Range(stackFrame.getLineNumber, (currentMethod.declaringType.methods.asScala.flatten(methodToLines(_)).filter(_ > currentMethodLastLine) :+ Int.MaxValue).min)

    val loadedAnonFunctionsInRange = currentMethod.declaringType.nestedTypes.asScala.flatMap(anonFunctionsInRange(_, range))

    val thread = stackFrame.getThread.asInstanceOf[JDIThread]
    val breakpoints = loadedAnonFunctionsInRange.map(createMethodEntryBreakpoint(_, thread))

    val step = new ScalaStepOver(range, thread, classPrepareRequest, breakpoints)
    DebugPlugin.getDefault.addDebugEventListener(step)

    // enable request
    target.getEventDispatcher.addJDIEventListener(step, classPrepareRequest)
    classPrepareRequest.enable

    // enable breakpoints
    breakpoints.foreach(target.breakpointAdded(_))

    step
  }

  def createMethodEntryBreakpoint(method: Method, thread: JDIThread) = {
    import scala.collection.JavaConverters._
    import scala.collection.mutable.Map
    val breakpoint= JDIDebugModel.createMethodEntryBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), method.declaringType.name, method.name, method.signature, method.location.lineNumber, -1, -1, -1, false, Map(IBreakpoint.PERSISTED -> false).asJava)
    breakpoint.setThreadFilter(thread)
    breakpoint
  }

  def anonFunctionsInRange(refType: ReferenceType, range: Range) = {
    import scala.collection.JavaConverters._
    val methods = refType.methods.asScala.filter(method =>
      range.contains(method.location.lineNumber) && method.name.startsWith("apply"))
      
    methods.size match {
      case 3 =>
        methods.find(_.name.startsWith("apply$")).orElse({
          // workaround for SI-5512
          // scalac 2.10.0-M2 add an extraneous apply method in some cases
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

class ScalaStepOver(range: Range, thread: JDIThread, classPrepareRequest: ClassPrepareRequest, breakpoints: Buffer[IJavaMethodEntryBreakpoint]) extends IJDIEventListener with IDebugEventSetListener {
  import ScalaStepOver._

  /* from IJDIEventListener */

  def eventSetComplete(event: Event, target: JDIDebugTarget, suspend: Boolean, eventSet: EventSet): Unit = {
    // nothing to do
  }

  def handleEvent(event: Event, target: JDIDebugTarget, suspendVote: Boolean, eventSet: EventSet): Boolean = {
    event match {
      case classPrepareEvent: ClassPrepareEvent =>
        anonFunctionsInRange(classPrepareEvent.referenceType, range).foreach(method => {
          val breakpoint = createMethodEntryBreakpoint(method, thread)
          breakpoints += breakpoint
          target.breakpointAdded(breakpoint)
        })
        true
      case _ =>
        suspendVote
    }
  }

  /* from IDebugEventSetListener */

  def handleDebugEvents(events: Array[DebugEvent]) {
    events.foreach(event => {
      event.getKind match {
        case DebugEvent.SUSPEND =>
          if (event.getSource == thread)
            cleanAll()
        case DebugEvent.TERMINATE =>
          if (event.getSource == thread)
            cleanAll()
        case _ =>
      }
    })
  }

  // -----

  def cleanAll() {
    val target = thread.getJavaDebugTarget

    DebugPlugin.getDefault.removeDebugEventListener(this)

    classPrepareRequest.disable
    target.getEventDispatcher.removeJDIEventListener(this, classPrepareRequest)

    breakpoints.foreach(target.breakpointRemoved(_, null))
  }

}