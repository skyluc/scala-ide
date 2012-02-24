package scala.tools.eclipse.debug

import org.eclipse.jdt.debug.core.IJavaBreakpointListener
import org.eclipse.jdt.debug.core.IJavaDebugTarget
import org.eclipse.jdt.debug.core.IJavaBreakpoint
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint
import org.eclipse.jdt.debug.core.IJavaThread
import org.eclipse.jdt.debug.core.IJavaType
import org.eclipse.jdt.debug.core.JDIDebugModel
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame
import com.sun.jdi.AbsentInformationException
import com.sun.jdi.Method
import org.eclipse.debug.core.model.IBreakpoint
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.internal.debug.core.IJDIEventListener
import com.sun.jdi.event.Event
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget
import com.sun.jdi.event.EventSet
import org.eclipse.debug.core.IDebugEventSetListener
import org.eclipse.debug.core.DebugEvent
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.model.IDebugModelProvider
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory2
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider
import org.eclipse.debug.core.ILaunch
import org.eclipse.jdt.debug.core.IJavaStackFrame
import org.eclipse.jdt.core.dom.Message
import org.eclipse.jdt.internal.debug.core.model.JDIThread
import scala.collection.mutable.Buffer
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector

object ScalaDebugger extends IDebugEventSetListener {
  
  val classIDebugModelProvider= classOf[IDebugModelProvider]
  val classIJavaDebugTarget= classOf[IJavaDebugTarget]
  val classIJavaStackFrame= classOf[IJavaStackFrame]
  
  val modelProvider= new IDebugModelProvider {
    def getModelIdentifiers()= {
      Array(modelId)
    }
  }
  
  val modelId= "org.scala-ide.sdt.debug"
  
  // Members declared in org.eclipse.debug.core.IDebugEventSetListener
  
  def handleDebugEvents(events: Array[DebugEvent]) {
    events.foreach(event => {
      event.getKind match {
        case DebugEvent.CREATE =>
          event.getSource match {
            case target: JDIDebugTarget => 
              javaDebugTargetCreated(target)
            case _ =>
          }
        case DebugEvent.SUSPEND =>
          event.getSource match {
            case thread: JDIThread =>
              javaThreadSuspended(thread, event.getDetail)
            case _ =>
          }
        case DebugEvent.TERMINATE =>
          event.getSource match {
            case target: JDIDebugTarget =>
              javaDebugTargetTerminated(target)
            case _ =>
          }
        case _ =>
      }
    })
  }
  
  // ----
  
  val debugTargets= Buffer[ScalaDebugTarget]()
  
  def init() {
    DebugPlugin.getDefault.addDebugEventListener(this)
  }
  
  private def javaDebugTargetCreated(target: JDIDebugTarget) {
    val scalaTarget= ScalaDebugTarget(target)
    debugTargets += scalaTarget
    val launch= target.getLaunch
    launch.removeDebugTarget(target)
    launch.addDebugTarget(scalaTarget)
    
    // TODO: do that in a better place
    launch.setSourceLocator(new ScalaSourceLocator(launch))
  }
  
  private def javaDebugTargetTerminated(target: JDIDebugTarget) {
    debugTargets.find(target == _.javaTarget).foreach(_.terminatedFromJava())
  }

  private def javaThreadSuspended(thread: JDIThread, eventDetail: Int) {
    debugTargets.find(thread.getDebugTarget == _.javaTarget).foreach(_.javaThreadSuspended(thread, eventDetail))
  }

}

class ScalaDebugger