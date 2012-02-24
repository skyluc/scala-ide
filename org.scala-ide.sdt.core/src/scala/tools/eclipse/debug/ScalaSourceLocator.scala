package scala.tools.eclipse.debug

import org.eclipse.debug.core.model.ISourceLocator
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.model.IStackFrame
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants
import scala.tools.eclipse.ScalaPlugin
import org.eclipse.core.resources.ResourcesPlugin
import scala.tools.eclipse.ScalaProject

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
     val sourceName= stackFrame.getSourceName
     
     val projectName= launch.getLaunchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "") // TODO: should be null here
     
     val project= ResourcesPlugin.getWorkspace.getRoot.getProject(projectName)
     
     val scalaProject= ScalaPlugin.plugin.asScalaProject(project)

     scalaProject.flatMap(_.allSourceFiles.find(_.getName == sourceName)).getOrElse(null)
   }
  
}