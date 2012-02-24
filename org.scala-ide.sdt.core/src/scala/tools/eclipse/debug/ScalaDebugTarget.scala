package scala.tools.eclipse.debug

import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget
import org.eclipse.debug.core.model.IDebugTarget
import com.sun.jdi.VirtualMachine
import org.eclipse.debug.core.model.IProcess
import com.sun.jdi.request.ThreadStartRequest
import com.sun.jdi.request.EventRequest
import org.eclipse.jdt.internal.debug.core.IJDIEventListener
import com.sun.jdi.event.Event
import com.sun.jdi.event.EventSet
import com.sun.jdi.request.ThreadDeathRequest
import com.sun.jdi.event.ThreadStartEvent
import com.sun.jdi.event.ThreadDeathEvent
import org.eclipse.jdt.internal.debug.core.model.JDIThread

object ScalaDebugTarget {

  def apply(javaTarget: JDIDebugTarget): ScalaDebugTarget = {

    val virtualMachine = javaTarget.getVM

    val threadStartRequest = virtualMachine.eventRequestManager.createThreadStartRequest
    threadStartRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE)

    val threadDeathRequest = virtualMachine.eventRequestManager.createThreadDeathRequest
    threadDeathRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE)

    val target = new ScalaDebugTarget(javaTarget, threadStartRequest, threadDeathRequest)

    // enable the requests
    javaTarget.addJDIEventListener(target, threadStartRequest)
    threadStartRequest.enable
    javaTarget.addJDIEventListener(target, threadDeathRequest)
    threadDeathRequest.enable

    target
  }

}

class ScalaDebugTarget(val javaTarget: JDIDebugTarget, threadStartRequest: ThreadStartRequest, threadDeathRequest: ThreadDeathRequest) extends ScalaDebugElement(null) with IDebugTarget with IJDIEventListener {

  // Members declared in org.eclipse.core.runtime.IAdaptable

  override def getAdapter(adapter: Class[_]): Object = {
    adapter match {
      case ScalaDebugger.classIJavaDebugTarget =>
        null
      case ScalaDebugger.classIJavaStackFrame =>
        null
      case _ =>
        super.getAdapter(adapter)
    }
  }

  // Members declared in org.eclipse.debug.core.IBreakpointListener

  def breakpointAdded(x$1: org.eclipse.debug.core.model.IBreakpoint): Unit = ???
  def breakpointChanged(x$1: org.eclipse.debug.core.model.IBreakpoint, x$2: org.eclipse.core.resources.IMarkerDelta): Unit = ???
  def breakpointRemoved(x$1: org.eclipse.debug.core.model.IBreakpoint, x$2: org.eclipse.core.resources.IMarkerDelta): Unit = ???

  // Members declared in org.eclipse.debug.core.model.IDebugElement

  override def getDebugTarget(): org.eclipse.debug.core.model.IDebugTarget = this
  override def getLaunch(): org.eclipse.debug.core.ILaunch = javaTarget.getLaunch

  // Members declared in org.eclipse.debug.core.model.IDebugTarget

  def getName(): String = "Scala Debug Target" // TODO: need better name
  def getProcess(): org.eclipse.debug.core.model.IProcess = javaTarget.getProcess
  def getThreads(): Array[org.eclipse.debug.core.model.IThread] = threads.toArray
  def hasThreads(): Boolean = !threads.isEmpty
  def supportsBreakpoint(x$1: org.eclipse.debug.core.model.IBreakpoint): Boolean = ???

  // Members declared in org.eclipse.debug.core.model.IDisconnect

  def canDisconnect(): Boolean = false // TODO: need real logic
  def disconnect(): Unit = ???
  def isDisconnected(): Boolean = false // TODO: need real logic

  // Members declared in org.eclipse.debug.core.model.IMemoryBlockRetrieval

  def getMemoryBlock(x$1: Long, x$2: Long): org.eclipse.debug.core.model.IMemoryBlock = ???
  def supportsStorageRetrieval(): Boolean = ???

  // Members declared in org.eclipse.debug.core.model.ISuspendResume

  def canResume(): Boolean = false // TODO: need real logic
  def canSuspend(): Boolean = false // TODO: need real logic
  def isSuspended(): Boolean = false // TODO: need real logic
  def resume(): Unit = ???
  def suspend(): Unit = ???

  // Members declared in org.eclipse.debug.core.model.ITerminate

  override def canTerminate(): Boolean = running // TODO: need real logic
  override def isTerminated(): Boolean = !running // TODO: need real logic
  override def terminate(): Unit = javaTarget.terminate

  // Members declared in org.eclipse.jdt.internal.debug.core.IJDIEventListener

  def eventSetComplete(event: Event, target: JDIDebugTarget, suspend: Boolean, eventSet: EventSet): Unit = {
    // nothing to do
  }

  def handleEvent(event: Event, target: JDIDebugTarget, suspendVote: Boolean, eventSet: EventSet): Boolean = {
    event match {
      case threadStartEvent: ThreadStartEvent =>
        threads += new ScalaThread(this, threadStartEvent.thread)
      case threadDeathEvent: ThreadDeathEvent =>
        threads --= threads.find(_.thread == threadDeathEvent.thread)
      case _ =>
        ???
    }
    suspendVote
  }

  // ---
  
  var running: Boolean= true

  val threads = {
    import scala.collection.JavaConverters._
    javaTarget.getVM.allThreads.asScala.map(new ScalaThread(this, _))
  }
  
  fireCreationEvent

  def javaThreadSuspended(thread: JDIThread, eventDetail: Int) {
    threads.find(_.thread == thread.getUnderlyingThread).get.suspendedFromJava(eventDetail)
  }
  
  def terminatedFromJava() {
    threads.clear
    running= false
    fireTerminateEvent
  }

}