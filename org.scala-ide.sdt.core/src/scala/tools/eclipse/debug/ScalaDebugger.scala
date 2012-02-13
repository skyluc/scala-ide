package scala.tools.eclipse.debug

import org.eclipse.jdt.debug.core.IJavaBreakpointListener
import org.eclipse.jdt.debug.core.IJavaDebugTarget
import org.eclipse.jdt.debug.core.IJavaBreakpoint
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint
import org.eclipse.jdt.debug.core.IJavaThread
import org.eclipse.jdt.debug.core.IJavaType
import org.eclipse.jdt.debug.core.JDIDebugModel
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame
import com.sun.jdi.AbsentInformationException
import com.sun.jdi.Method
import org.eclipse.debug.core.model.IBreakpoint
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.internal.debug.core.IJDIEventListener
import com.sun.jdi.event.Event
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget
import com.sun.jdi.event.EventSet

object ScalaDebugger {
  
  /**
   * Seamless stepover
   * TODO: move to a Scala stack frame class
   */
  def stepOver(stackFrame: JDIStackFrame) {
    ScalaStepOver(stackFrame)
    stackFrame.stepOver
  }
}

class ScalaDebugger