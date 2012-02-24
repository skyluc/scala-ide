package scala.tools.eclipse.debug

import org.eclipse.debug.ui.IDebugModelPresentation
import org.eclipse.debug.ui.DebugUITools
import org.eclipse.debug.ui.IDebugUIConstants
import org.eclipse.ui.IEditorInput
import org.eclipse.core.resources.IFile
import org.eclipse.ui.part.FileEditorInput
import org.eclipse.ui.ide.IDE
import org.eclipse.ui.IFileEditorInput

class ScalaDebugModelPresentation extends IDebugModelPresentation {

  // Members declared in org.eclipse.jface.viewers.IBaseLabelProvider

  def addListener(x$1: org.eclipse.jface.viewers.ILabelProviderListener): Unit = ???
  def dispose(): Unit = {} // TODO: need real logic
  def isLabelProperty(x$1: Any, x$2: String): Boolean = ???
  def removeListener(x$1: org.eclipse.jface.viewers.ILabelProviderListener): Unit = ???

  // Members declared in org.eclipse.debug.ui.IDebugModelPresentation

  def computeDetail(x$1: org.eclipse.debug.core.model.IValue, x$2: org.eclipse.debug.ui.IValueDetailListener): Unit = ???
  
  def getImage(element: Any): org.eclipse.swt.graphics.Image = {
    element match {
      case target: ScalaDebugTarget =>
        // TODO: right image depending of state
        DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET)
      case thread: ScalaThread =>
        // TODO: right image depending of state
        DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING)
      case stackFrame: ScalaStackFrame =>
        // TODO: right image depending of state
        DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_STACKFRAME)
      case _ =>
        ???
    }
  }
  
  def getText(element: Any): String = {
    element match {
      case target: ScalaDebugTarget =>
        target.getName // TODO: everything
      case thread: ScalaThread =>
        thread.getName // TODO: everything
      case stackFrame: ScalaStackFrame =>
        stackFrame.getName // TODO: everything
      case _ =>
        ???
    }
  }
  
  def setAttribute(x$1: String, x$2: Any): Unit = ???

  // Members declared in org.eclipse.debug.ui.ISourcePresentation

  def getEditorId(input: IEditorInput, element: Any): String = {
    input match {
      case fileInput: IFileEditorInput =>
        IDE.getEditorDescriptor(fileInput.getFile).getId
      case _ =>
        null
    }
  }
  
  def getEditorInput(input: Any): IEditorInput = {
    input match {
      case file: IFile =>
        new FileEditorInput(file)
      case _ =>
        ???
    }
  }

  // ----

}