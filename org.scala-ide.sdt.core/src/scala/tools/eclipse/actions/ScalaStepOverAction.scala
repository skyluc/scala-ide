package scala.tools.eclipse.actions

import org.eclipse.ui.IWorkbenchWindowActionDelegate
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.action.IAction
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame
import scala.tools.eclipse.logging.HasLogger
import com.sun.jdi.Location
import com.sun.jdi.Method
import com.sun.jdi.AbsentInformationException
import org.eclipse.jdt.debug.core.JDIDebugModel
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils
import org.eclipse.debug.core.model.IBreakpoint

class ScalaStepOverAction extends IWorkbenchWindowActionDelegate with HasLogger {
  
  var selectedStackFrame: JDIStackFrame= null
  
  def dispose() {}
  
  def init(window: IWorkbenchWindow) {}
  
  def run(action: IAction) {
    run(selectedStackFrame)
  }
  
  def run(stackFrame: JDIStackFrame) {
    
    import scala.collection.JavaConverters._
    
    val method= stackFrame.getUnderlyingMethod
    val nestedTypes= method.declaringType.nestedTypes
    val currentLine= stackFrame.getLineNumber
    val methodLastLine= method.allLineLocations.asScala.map(_.lineNumber).max
    val methods= method.declaringType.methods
    
    def methodToLines(m: Method) =
      try {
        m.allLineLocations.asScala.map(_.lineNumber)
      } catch {
        case e: AbsentInformationException =>
          Nil
        case e =>
          throw e
      }
    
    val nextMethodFirstLine= (methods.asScala.flatten(methodToLines(_)).filter(_ > methodLastLine) :+ Int.MaxValue).min
    
    val closuresInRange= nestedTypes.asScala.flatten(_.methods.asScala).filter(m => {
      val minLine= (methodToLines(m) :+ Int.MaxValue).min
      minLine >= currentLine && minLine < nextMethodFirstLine && m.name.startsWith("apply$")
    })
    
    import scala.collection.mutable.Map
    
    val attributes= Map(IBreakpoint.PERSISTED -> true)
    
    for (m <- closuresInRange) {
      JDIDebugModel.createMethodEntryBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), m.declaringType.name, m.name, m.signature, methodToLines(m).min, -1, -1, -1, true, attributes.asJava)
      
    }
  
    stackFrame.stepOver
  }
  
  def selectionChanged(action: IAction, selection: ISelection) {
    action.setEnabled(selection match {
      case structuredSelection: IStructuredSelection =>
        if (structuredSelection.size == 1) {
          structuredSelection.getFirstElement match {
            case stackFrame: JDIStackFrame =>
              selectedStackFrame= stackFrame
              true
            case _ =>
              false
          }
        } else {
          false
        }
      case _ =>
        false
    })
  }

}