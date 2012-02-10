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
import scala.tools.eclipse.debug.ScalaDebugger

class ScalaStepOverAction extends IWorkbenchWindowActionDelegate with IActionDelegate2 with INullSelectionListener{
  
  var action: IAction= null
  
  var selectedStackFrame: JDIStackFrame= null
  
  def dispose() {}
  
  def init(window: IWorkbenchWindow) {
    // register to selection change events from the debug view
    window.getSelectionService.addSelectionListener("org.eclipse.debug.ui.DebugView", this)
  }
  
  def init(a: IAction) {
    action= a
    action.setEnabled(false)
  }
  
  def runWithEvent(a: IAction, event: Event) {
    run(a)
  }

  def run(action: IAction) {
    ScalaDebugger.stepOver(selectedStackFrame)
  }
  
  def selectionChanged(action: IAction, selection: ISelection) {
    // not interested in this event. The other method receives selection change
    // events from the debug view.
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