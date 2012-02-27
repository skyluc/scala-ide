package scala.tools.eclipse.debug

import org.eclipse.debug.core.model.IDebugElement
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.model.IDebugModelProvider
import ScalaDebugger._
import scala.tools.eclipse.logging.HasLogger
import org.eclipse.debug.core.model.IDebugTarget
import org.eclipse.debug.core.model.DebugElement
import org.eclipse.debug.core.model.ITerminate

class ScalaDebugElement(target: ScalaDebugTarget) extends DebugElement(target) with ITerminate with HasLogger {

  // Members declared in org.eclipse.core.runtime.IAdaptable

  override def getAdapter(adapter: Class[_]): Object = {
    adapter match {
      case ScalaDebugger.classIDebugModelProvider =>
        modelProvider
      case _ =>
//        logger.debug("%s getAdapter %s".format(this.getClass.getName, adapter.getName))
        super.getAdapter(adapter)
    }
  }

  // Members declared in org.eclipse.debug.core.model.IDebugElement

  def getModelIdentifier(): String = modelId
  
  // Members declared in org.eclipse.debug.core.model.ITerminate
  
  def canTerminate(): Boolean = target.canTerminate
  def isTerminated(): Boolean = target.isTerminated
  def terminate(): Unit = target.terminate
  
  // ----
  
  def getScalaDebugTarget(): ScalaDebugTarget= target
  

}