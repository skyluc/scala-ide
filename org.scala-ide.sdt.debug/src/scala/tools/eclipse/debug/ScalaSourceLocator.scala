package scala.tools.eclipse.debug

import scala.tools.eclipse.ScalaPlugin
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.debug.core.model.{ IStackFrame, ISourceLocator }
import org.eclipse.debug.core.ILaunch
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants
import model.ScalaStackFrame
import scala.tools.eclipse.ScalaProject
import org.eclipse.core.resources.IFile
import org.eclipse.jdt.core.IType

/*
 * TODO: bad, bad implementation of ISourceLocator
 * Kind of work for current project, or in workspace plug-in projects
 */

class ScalaSourceLocator(launch: ILaunch) extends ISourceLocator {

  def getSourceElement(stackFrame: IStackFrame): AnyRef = {
    stackFrame match {
      case scalaStackFrame: ScalaStackFrame =>
        getSourceElement(scalaStackFrame)
      case _ =>
        null
    }
  }

  def getSourceElement(stackFrame: ScalaStackFrame): AnyRef = {
//    val sourceName = stackFrame.getSourceName
    
    val attributes = launch.getLaunchConfiguration.getAttributes

    val projectName = launch.getLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, null.asInstanceOf[String])

    val scalaProjects = if (projectName != null) { // launch configuration on one project
      val project = ResourcesPlugin.getWorkspace.getRoot.getProject(projectName)

      ScalaPlugin.plugin.asScalaProject(project).toList

    } else { // launch configuration for plugins
      val workspacePlugins = launch.getLaunchConfiguration.getAttribute("selected_workspace_plugins", "").split(',')
      workspacePlugins.flatMap(s => ScalaPlugin.plugin.asScalaProject(ResourcesPlugin.getWorkspace.getRoot.getProject(s.substring(0, s.indexOf('@'))))).toList
    }
    
    // TODO: goes through all project instead of stopping at the first occurrence
    val t = scalaProjects.flatMap(findType(_, stackFrame.stackFrame.location.declaringType.name))
    t.headOption.getOrElse(null)
  }
  
  def findType(project: ScalaProject, typeName: String) : Option[IType] = {
    Some(project.javaProject.findType(typeName))
  }

}