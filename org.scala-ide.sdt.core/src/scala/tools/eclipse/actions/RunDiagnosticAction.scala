package scala.tools.eclipse
package actions

import org.eclipse.jface.action.IAction
import org.eclipse.jface.viewers.ISelection
import org.eclipse.ui.IObjectActionDelegate
import org.eclipse.ui.IWorkbenchPart
import org.eclipse.ui.IWorkbenchWindowActionDelegate
import org.eclipse.ui.IWorkbenchWindow
import scala.tools.eclipse.util.Utils
import scala.tools.eclipse.logging.LogManager
import scala.tools.eclipse.ui.OpenExternalFile

class RunDiagnosticAction extends IObjectActionDelegate with IWorkbenchWindowActionDelegate {
  private var parentWindow: IWorkbenchWindow = null

  val RUN_DIAGNOSTICS = "org.scala-ide.sdt.ui.runDiag.action"
  val REPORT_BUG      = "org.scala-ide.sdt.ui.reportBug.action"
  val OPEN_LOG_FILE   = "org.scala-ide.sdt.ui.openLogFile.action"

  override def init(window: IWorkbenchWindow) {
    parentWindow = window
  }

  def dispose = { }

  def selectionChanged(action: IAction, selection: ISelection) {  }

  def run(action: IAction) {
    Utils tryExecute {
      action.getId match {
        case RUN_DIAGNOSTICS =>
          val shell = if (parentWindow == null) ScalaPlugin.getShell else parentWindow.getShell
          new diagnostic.DiagnosticDialog(shell).open
        case REPORT_BUG =>
          val shell = if (parentWindow == null) ScalaPlugin.getShell else parentWindow.getShell
          new diagnostic.ReportBugDialog(shell).open
        case OPEN_LOG_FILE =>
          OpenExternalFile(LogManager.logFile).open()
        case _ =>
      }
    }
  }

  def setActivePart(action: IAction, targetPart: IWorkbenchPart) { }
}
