package scala.tools.eclipse.debug

import scala.tools.eclipse.testsetup.TestProjectSetup
import org.junit.Test
import org.junit.Assert._
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.jdt.debug.core.JDIDebugModel
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.debug.core.IDebugEventSetListener
import org.eclipse.debug.core.DebugEvent
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame
import org.eclipse.jdt.internal.debug.core.model.JDIThread
import scala.tools.eclipse.actions.ScalaStepOverAction
import org.eclipse.debug.core.model.RuntimeProcess
import org.hamcrest.Matcher
import org.junit.matchers.JUnitMatchers
import org.eclipse.jdt.debug.core.IJavaBreakpointListener
import org.eclipse.jdt.debug.core.IJavaDebugTarget
import org.eclipse.jdt.debug.core.IJavaBreakpoint
import org.eclipse.jdt.debug.core.IJavaType
import org.eclipse.debug.core.IBreakpointListener
import org.eclipse.jdt.debug.core.IJavaThread
import org.eclipse.debug.core.DebugException
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint
import org.eclipse.jdt.core.dom.Message
import scala.tools.eclipse.logging.HasLogger
import org.junit.After

object ScalaDebugSteppingTest extends TestProjectSetup("debug")

class ScalaDebugSteppingTest {

  import ScalaDebugSteppingTest._

  var session: ScalaDebugTestSession = null

  @After
  def cleanDebugSession() {
    if (session ne null) {
      session.terminate()
      session = null
    }
  }

  @Test
  def StepOverIntoForComprehension() {

    session = new ScalaDebugTestSession(file("ForComprehensionListString.launch"))

    val TYPENAME = "stepping.ForComprehensionListString$"

    session.runToLine(TYPENAME, 9)
    
    session.checkStackFrame(TYPENAME, "main", 9)
    
    session.stepOver()
    
    session.checkStackFrame(TYPENAME + "$anonfun$main$1", "apply", 10)
  }

}