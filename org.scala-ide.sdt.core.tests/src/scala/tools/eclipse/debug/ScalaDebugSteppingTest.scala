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

object ScalaDebugSteppingTest extends TestProjectSetup("debug") with ScalaDebugTest

class ScalaDebugSteppingTest {
  
  import ScalaDebugSteppingTest._
  
  @Test
  def StepOverIntoForComprehension() {
    
    val TYPENAME= "stepping.ForComprehensionListString$"
    
    val initialStackFrame= launchToBreakpoint("ForComprehensionListString", TYPENAME, 9)
    
    assertEquals("Suspended in the wrong type", TYPENAME, initialStackFrame.get.getDeclaringTypeName)
    assertEquals("Suspended on the wrong line", 9, initialStackFrame.get.getLineNumber)
    
    ScalaDebugger.stepOver(initialStackFrame.get)
    
    val nextStackFrame= runToSuspend()
    
    assertThat("Suspended in the wrong type", nextStackFrame.get.getDeclaringTypeName, JUnitMatchers.containsString(TYPENAME + '$'))
    assertEquals("Suspended at the wrong line", 10, nextStackFrame.get.getLineNumber)
   
  }

}

trait ScalaDebugTest extends TestProjectSetup {
  
  val launchManager= DebugPlugin.getDefault.getLaunchManager
  
  val eventManager= new DebugEventManager()
    
  def launchToBreakpoint(launchConfigurationName: String, typeName: String, breakpointLine: Int): Option[JDIStackFrame] = {
    
    // find the launch configuration
    val launchConfiguration= DebugPlugin.getDefault.getLaunchManager.getLaunchConfiguration(file(launchConfigurationName + ".launch"))
    
    // create the breakpoint
    val breakpoint= JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace.getRoot, typeName, breakpointLine, -1, -1, -1, true, null)
    
    // launch the configuration
    launchConfiguration.launch(ILaunchManager.DEBUG_MODE, null)
    
    // wait until suspend event
    val suspendedStackFrame= eventManager.waitUntilSuspended
    
    // remove the breakpoint
    breakpoint.delete
    
    // reset the states
    eventManager.reset
    
    //return the stack frame
    suspendedStackFrame
  }
  
  def runToSuspend(): Option[JDIStackFrame] = {
    eventManager.waitUntilSuspended
  }
  
}

class DebugEventManager extends IDebugEventSetListener {
  
  DebugPlugin.getDefault.addDebugEventListener(this)
  
  var suspended= false
  var stackFrame: Option[JDIStackFrame]= None
  
  def handleDebugEvents(events: Array[DebugEvent]) {
    events.find(event =>
        event.getKind match {
          case DebugEvent.SUSPEND =>
            event.getSource match {
              case thread: JDIThread =>
                stackFrame= Some(thread.getTopStackFrame.asInstanceOf[JDIStackFrame])
              case _ =>
            }
            setSuspended
            true
          case DebugEvent.TERMINATE => 
            if (event.getSource.isInstanceOf[JDIDebugTarget])
              setSuspended
            true
          case _ =>
            false
        })
  }
  
  private def setSuspended() {
    this.synchronized {
      suspended= true
      this.notify
    }
  }
  
  def reset() {
    this.synchronized {
      suspended= false
      stackFrame= None
    }
  }

  def waitUntilSuspended(): Option[JDIStackFrame]=  {
    if (!suspended) {
      this.synchronized {
        if (!suspended) {
          this.wait
        }
      }
    }
    stackFrame
  }
  

}