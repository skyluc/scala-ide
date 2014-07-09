package org.scalaide.ui.internal.actions

import org.eclipse.core.resources.IProject
import org.scalaide.core.api.ScalaPlugin

class RestartPresentationCompilerAction extends AbstractPopupAction {
  override def performAction(project: IProject): Unit = {
    val scalaProject = ScalaPlugin().asScalaProject(project)
    scalaProject foreach (_.presentationCompiler.askRestart())
  }
}
