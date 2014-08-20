package org.scalaide.ui.internal.handlers

import org.eclipse.core.runtime.IStatus
import org.eclipse.jface.dialogs.MessageDialog
import org.scalaide.core.api.ScalaPlugin
import org.scalaide.util.internal.ui.UIStatusesConverter
import org.eclipse.swt.SWT
import org.scalaide.util.internal.eclipse.SWTUtils

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
      if (!ScalaPlugin().headlessMode) {
        MessageDialog.open(UIStatusesConverter.MessageDialogOfIStatus(status.getSeverity()), SWTUtils.getShell, "An exception occurred", status.getException().getMessage(), SWT.NONE)
      }
  }

}
