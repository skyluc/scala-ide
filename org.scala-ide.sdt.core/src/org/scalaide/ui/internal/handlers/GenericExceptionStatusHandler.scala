package org.scalaide.ui.internal.handlers

import org.eclipse.core.runtime.IStatus
import org.eclipse.jface.dialogs.MessageDialog
import org.scalaide.core.ScalaPlugin

object GenericExceptionStatusHandler {

  /**
   * Status code indicating there was an error at launch time
   *  Linked to ScalaLaunchDelegate via our statusHandlers extension (see plugin.xml)
   */
  final val STATUS_CODE_EXCEPTION = 1010

}

/**
 * Generic Class for showing a generic exception, and that's all
 */
class GenericExceptionStatusHandler extends RichStatusHandler {

  def doHandleStatus(status: IStatus, source: Object) = {
      if (!ScalaPlugin.plugin.headlessMode) {
        MessageDialog.openError(ScalaPlugin.getShell, "An exception occurred", status.getException().getMessage())
      }
  }

}
