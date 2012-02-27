package scala.tools.eclipse
package properties

import org.eclipse.jface.preference._
import org.eclipse.ui.IWorkbenchPreferencePage
import org.eclipse.ui.IWorkbench

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
import org.eclipse.jface.preference.IPreferenceStore

import scala.tools.eclipse.ScalaPlugin

class DebugPreferencePage extends FieldEditorPreferencePage with IWorkbenchPreferencePage {
  import DebugPreferencePage._

  setPreferenceStore(ScalaPlugin.plugin.getPreferenceStore)
  setDescription("""Experimental debugger for Scala.
To use it, launch your Scala application as usual.""")

  override def createFieldEditors() {
    addField(new BooleanFieldEditor(P_ENABLE, "Enable (change will be applied to new debug sessions only)", getFieldEditorParent))
  }

  def init(workbench: IWorkbench) {}

}

object DebugPreferencePage {
  val BASE = "scala.tools.eclipse.debug."
  val P_ENABLE = BASE + "enabled"
}

class DebugPreferenceInitializer extends AbstractPreferenceInitializer {

  import DebugPreferencePage._

  override def initializeDefaultPreferences() {
    val store = ScalaPlugin.plugin.getPreferenceStore
    store.setDefault(P_ENABLE, false)
  }

}