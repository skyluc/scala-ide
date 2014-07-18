package org.scalaide.ui.internal.actions

import org.eclipse.core.resources.IProject
import org.eclipse.jface.action.IAction
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.core.runtime.Platform
import org.eclipse.ui.IObjectActionDelegate
import org.eclipse.ui.IWorkbenchPart
import org.scalaide.util.internal.Utils
import org.scalaide.core.internal.project.ScalaLibraryPluginDependencyUtils
import org.scalaide.core.ScalaConstants

object ToggleScalaNatureAction {
  val PDE_PLUGIN_NATURE = "org.eclipse.pde.PluginNature" /* == org.eclipse.pde.internal.core.natures.PDE.PLUGIN_NATURE */
  val PDE_BUNDLE_NAME = "org.eclipse.pde.ui"
}

class ToggleScalaNatureAction extends AbstractPopupAction {
  import ToggleScalaNatureAction._

  override def performAction(project: IProject) {
    toggleScalaNature(project)
  }

  private def toggleScalaNature(project: IProject) =
    Utils tryExecute {
      if (project.hasNature(ScalaConstants.NatureId)) {
        doIfPdePresent(project) { ScalaLibraryPluginDependencyUtils.removeScalaLibraryRequirement(project) }
        updateNatureIds(project) { _ filterNot (_ == ScalaConstants.NatureId) }
      } else {
        doIfPdePresent(project) { ScalaLibraryPluginDependencyUtils.addScalaLibraryRequirement(project) }
        updateNatureIds(project) { ScalaConstants.NatureId +: _ }
      }
    }

  private def doIfPdePresent(project: IProject)(proc: => Unit) =
    if (project.hasNature(PDE_PLUGIN_NATURE) && Platform.getBundle(PDE_BUNDLE_NAME) != null)
      proc

  private def updateNatureIds(project: IProject)(natureIdUpdater: Array[String] => Array[String]) {
    val projectDescription = project.getDescription
    val currentNatureIds = projectDescription.getNatureIds
    val updatedNatureIds = natureIdUpdater(currentNatureIds)
    projectDescription.setNatureIds(updatedNatureIds)
    project.setDescription(projectDescription, null)
    project.touch(null)
  }
}
