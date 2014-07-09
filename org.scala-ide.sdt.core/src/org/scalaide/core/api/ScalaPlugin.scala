package org.scalaide.core.api

import org.eclipse.core.resources.IProject
import scala.collection.mutable
import org.scalaide.logging.HasLogger
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.core.runtime.CoreException
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.ui.plugin.AbstractUIPlugin
import scala.tools.nsc.settings.ScalaVersion
import org.scalaide.util.internal.CompilerUtils

object ScalaPlugin {

  /** The runtime instance of ScalaPlugin
   */
  def apply(): ScalaPlugin = org.scalaide.core.internal.ScalaPlugin()

}

/** The public interface of the plugin runtime class of the SDT plugin.
 *  
 *  All methods defined inside this trait are thread-safe.
 *  For the inherited methods, check their own documentation.
 */
trait ScalaPlugin extends AbstractUIPlugin with HasLogger {

  import SdtConstants._

  /** Indicates if the `sdtcore.notimeouts` flag is set.
   */
  lazy val noTimeoutMode: Boolean = System.getProperty(NoTimeoutsProperty) ne null

  /** Indicates if the `sdtcore.headless` flag is set.
   */
  lazy val headlessMode: Boolean = System.getProperty(HeadlessProperty) ne null

  /** The Scala version the SDT plugin is running on.
   */
  lazy val scalaVersion: ScalaVersion = ScalaVersion.current
  
  /** The `major.minor` string for the Scala version the SDT plugin is running on.
   */
  lazy val shortScalaVersion: String = CompilerUtils.shortString(scalaVersion)

  /** Returns the ScalaProject for the given project.
   *  Bevahior is undefined if the given project doesn't have the Scala nature.
   */
  def getScalaProject(project: IProject): ScalaProject

  /**
   * Return Some(ScalaProject) if the project has the Scala nature, None otherwise.
   */
  def asScalaProject(project: IProject): Option[ScalaProject]
}
