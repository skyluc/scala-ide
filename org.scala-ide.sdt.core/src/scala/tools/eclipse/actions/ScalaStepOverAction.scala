package scala.tools.eclipse.actions

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mutableMapAsJavaMapConverter
import scala.collection.mutable.Map
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.debug.core.model.IBreakpoint
import org.eclipse.jdt.debug.core.JDIDebugModel
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame
import org.eclipse.jface.action.IAction
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.ui.IActionDelegate2
import org.eclipse.ui.INullSelectionListener
import org.eclipse.ui.IWorkbenchPart
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.IWorkbenchWindowActionDelegate
import com.sun.jdi.AbsentInformationException
import com.sun.jdi.Method
import org.eclipse.swt.widgets.Event

class ScalaStepOverAction extends IWorkbenchWindowActionDelegate with IActionDelegate2 with INullSelectionListener{
  
  var action: IAction= null
  
  var selectedStackFrame: JDIStackFrame= null
  
  def dispose() {}
  
  def init(window: IWorkbenchWindow) {
    window.getSelectionService.addSelectionListener("org.eclipse.debug.ui.DebugView", this)
  }
  
  def init(a: IAction) {
    action= a
  }
  
  def runWithEvent(a: IAction, event: Event) {
    run(a)
  }

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
    // this event is not used. Only the events from the debug view are interesting
    println("arggg")
  }
  
  def selectionChanged(part: IWorkbenchPart, selection: ISelection ) {
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