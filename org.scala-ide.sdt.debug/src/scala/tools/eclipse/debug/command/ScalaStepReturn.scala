package scala.tools.eclipse.debug.command

import com.sun.jdi.event.Event
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget
import com.sun.jdi.event.EventSet
import org.eclipse.jdt.internal.debug.core.IJDIEventListener
import scala.tools.eclipse.debug.model.ScalaStackFrame
import com.sun.jdi.request.StepRequest
import com.sun.jdi.request.EventRequest
import scala.tools.eclipse.debug.model.ScalaThread
import scala.tools.eclipse.debug.model.ScalaDebugTarget
import org.eclipse.debug.core.DebugEvent
import com.sun.jdi.event.StepEvent
import com.sun.jdi.request.MethodExitRequest
import com.sun.jdi.event.MethodExitEvent
import scala.tools.eclipse.debug.model.ScalaValue

object ScalaStepReturn {

  def apply(scalaStackFrame: ScalaStackFrame): ScalaStepReturn = {

    val eventRequestManager = scalaStackFrame.stackFrame.virtualMachine.eventRequestManager

    val stepReturnRequest = eventRequestManager.createStepRequest(scalaStackFrame.stackFrame.thread, StepRequest.STEP_LINE, StepRequest.STEP_OUT)
    stepReturnRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD)
    
    val methodExitRequest = eventRequestManager.createMethodExitRequest
    methodExitRequest.addThreadFilter(scalaStackFrame.stackFrame.thread)
    methodExitRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD)

    new ScalaStepReturn(scalaStackFrame.getScalaDebugTarget, scalaStackFrame.thread, stepReturnRequest, methodExitRequest)
  }

}

// TODO: when implementing support without filtering, need to workaround problem reported in Eclipse bug #38744
class ScalaStepReturn(target: ScalaDebugTarget, thread: ScalaThread, stepReturnRequest: StepRequest, methodExitRequest: MethodExitRequest) extends IJDIEventListener with ScalaStep {

  // Members declared in org.eclipse.jdt.internal.debug.core.IJDIEventListener

  def eventSetComplete(event: Event, target: JDIDebugTarget, suspend: Boolean, eventSet: EventSet): Unit = {
    // nothing to do
  }

  def handleEvent(event: Event, javaTarget: JDIDebugTarget, suspendVote: Boolean, eventSet: EventSet): Boolean = {
    event match {
      case stepEvent: StepEvent =>
        if (target.isValidLocation(stepEvent.location)) {
          stop
          thread.suspendedFromScala(DebugEvent.STEP_RETURN)
          false
        } else {
          true
        }
      case methodExitEvent: MethodExitEvent =>
        thread.lastExitValue= ScalaValue(methodExitEvent.returnValue, thread.getScalaDebugTarget)
        true
      case _ =>
        suspendVote
    }
  }

  // Members declared in scala.tools.eclipse.debug.command.ScalaStep

  def step() {
    val eventDispatcher = target.javaTarget.getEventDispatcher

    eventDispatcher.addJDIEventListener(this, stepReturnRequest)
    eventDispatcher.addJDIEventListener(this, methodExitRequest)
    stepReturnRequest.enable
    methodExitRequest.enable
    thread.resumedFromScala(DebugEvent.STEP_RETURN)
    thread.thread.resume
  }

  def stop() {
    val eventDispatcher = target.javaTarget.getEventDispatcher

    val eventRequestManager = thread.thread.virtualMachine.eventRequestManager

    stepReturnRequest.disable
    methodExitRequest.disable
    eventDispatcher.removeJDIEventListener(this, stepReturnRequest)
    eventDispatcher.removeJDIEventListener(this, methodExitRequest)
    eventRequestManager.deleteEventRequest(stepReturnRequest)
    eventRequestManager.deleteEventRequest(methodExitRequest)
  }

}