package scala.tools.eclipse.debug

import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.core.resources.IFile
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget
import org.eclipse.jdt.debug.core.JDIDebugModel
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.debug.core.IDebugEventSetListener
import org.eclipse.debug.core.DebugEvent
import org.eclipse.jdt.internal.debug.core.model.JDIThread
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame

class ScalaDebugTestSession(launchConfigurationFile: IFile) extends IDebugEventSetListener {

  object State extends Enumeration {
    type State = Value
    val NOT_LAUNCHED, RUNNING, SUSPENDED, TERMINATED = Value
  }
  import State._

  // from IDebugEventSetListener

  DebugPlugin.getDefault.addDebugEventListener(this)

  def handleDebugEvents(events: Array[DebugEvent]) {
    events.foreach(event =>
      event.getKind match {
        case DebugEvent.CREATE =>
          event.getSource match {
            case target: JDIDebugTarget =>
              setLaunched(target)
            case _ =>
          }
        case DebugEvent.RESUME =>
          setRunning
        case DebugEvent.SUSPEND =>
          event.getSource match {
            case thread: JDIThread =>
              setSuspended(thread.getTopStackFrame.asInstanceOf[JDIStackFrame])
            case _ =>
              setSuspended(null)
          }
        case DebugEvent.TERMINATE =>
          event.getSource match {
            case target: JDIDebugTarget =>
              setTerminated
            case _ =>
          }
        case _ =>
      })
  }

  // ----

  def setLaunched(target: JDIDebugTarget) {
    this.synchronized {
      debugTarget = target
      setRunning
    }
  }

  def setRunning() {
    this.synchronized {
      state = RUNNING
      currentStackFrame = null
    }
  }

  def setSuspended(stackFrame: JDIStackFrame) {
    this.synchronized {
      currentStackFrame = stackFrame
      state = SUSPENDED
      this.notify
    }
  }

  def setTerminated() {
    this.synchronized {
      state = TERMINATED
      debugTarget = null
      this.notify
    }
  }

  def waitUntilSuspended() {
    this.synchronized {
      if (state != SUSPENDED && state != TERMINATED)
        this.wait
    }
  }

  // ----

  var state = NOT_LAUNCHED
  var debugTarget: JDIDebugTarget = null
  var currentStackFrame: JDIStackFrame = null

  def runToLine(typeName: String, breakpointLine: Int) {
    assertThat("Bad state before runToBreakpoint", state, anyOf(is(NOT_LAUNCHED), is(SUSPENDED)))

    val breakpoint = JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace.getRoot, typeName, breakpointLine, -1, -1, -1, true, null)

    if (state eq NOT_LAUNCHED) {
      launch()
    } else {
      currentStackFrame.resume
    }

    waitUntilSuspended
    breakpoint.delete

    assertEquals("Bad state after runToBreakpoint", SUSPENDED, state)
  }

  def stepOver() {
    assertEquals("Bad state before stepOver", SUSPENDED, state)

    ScalaDebugger.stepOver(currentStackFrame)

    waitUntilSuspended

    assertEquals("Bad state after stepOver", SUSPENDED, state)
  }

  def terminate() {
    if ((state ne NOT_LAUNCHED) && (state ne TERMINATED)) {
      debugTarget.terminate
      waitUntilSuspended
      assertEquals("Bad state after terminate", TERMINATED, state)
    }
  }

  private def launch() {
    val launchConfiguration = DebugPlugin.getDefault.getLaunchManager.getLaunchConfiguration(launchConfigurationFile)
    launchConfiguration.launch(ILaunchManager.DEBUG_MODE, null).getDebugTarget.asInstanceOf[JDIDebugTarget]
  }

  // -----

  def checkStackFrame(typeName: String, methodName: String, line: Int) {
    assertEquals("Bad state before checkStackFrame", SUSPENDED, state)

    assertEquals("Wrong typeName", typeName, currentStackFrame.getDeclaringTypeName)
    assertEquals("Wrong method", methodName, currentStackFrame.getMethodName)
    assertEquals("Wrong line", line, currentStackFrame.getLineNumber)
  }

}