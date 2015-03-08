/*
 * Copyright (c) 2014 Contributor. All rights reserved.
 */
package org.scalaide.debug.internal.preferences

import org.scalaide.debug.internal.ui.ExpressionEvaluatorViewLayoutType
import org.scalaide.debug.internal.expression.ScalaExpressionEvaluatorPlugin

/**
 * Provides more comfortable way to access preference values related to expression evaluator
 */
object ExpressionEvaluatorPreferences {
  import ExprEvalPreferencePage._

  private lazy val preferenceStore = ScalaExpressionEvaluatorPlugin().getPreferenceStore()

  def collectionAndArrayValuesGroupSize: Int =
    preferenceStore.getInt(EXP_EVAL_COLLECTION_AND_ARRAY_VALUES_GROUP_SIZE)

  def layoutType: ExpressionEvaluatorViewLayoutType.Value =
    ExpressionEvaluatorViewLayoutType(preferenceStore.getString(EXP_EVAL_LAYOUT_TYPE).toInt)

  def layoutType_=(layoutType: ExpressionEvaluatorViewLayoutType.Value): Unit =
    preferenceStore.setValue(EXP_EVAL_LAYOUT_TYPE, layoutType.id.toString())

  def isCodeCompletionEnabled: Boolean =
    preferenceStore.getBoolean(EXP_EVAL_ENABLE_CODE_COMPLETION)

  def showLineNumbers: Boolean =
    preferenceStore.getBoolean(EXP_EVAL_SHOW_LINE_NUMBERS)

  def showLineNumbers_=(enabled: Boolean): Unit =
    preferenceStore.setValue(EXP_EVAL_SHOW_LINE_NUMBERS, enabled)

  def showStaticFieldsInTreeView: Boolean =
    preferenceStore.getBoolean(EXP_EVAL_SHOW_STATIC_FIELDS_IN_TREE_VIEW)

  def showSyntheticFieldsInTreeView: Boolean =
    preferenceStore.getBoolean(EXP_EVAL_SHOW_SYNTHETIC_FIELDS_IN_TREE_VIEW)

  def showCollectionsLogicalStructure: Boolean =
    preferenceStore.getBoolean(EXP_EVAL_SHOW_COLLECTIONS_LOGICAL_STRUCTURE)

  def showCollectionsLogicalStructure_=(enabled: Boolean): Unit =
    preferenceStore.setValue(EXP_EVAL_SHOW_COLLECTIONS_LOGICAL_STRUCTURE, enabled)
}
