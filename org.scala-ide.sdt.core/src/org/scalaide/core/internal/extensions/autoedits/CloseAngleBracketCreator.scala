package org.scalaide.core.internal.extensions.autoedits

import org.scalaide.core.text.Document
import org.scalaide.core.text.TextChange
import org.scalaide.extensions.autoedits.CloseAngleBracket

object CloseAngleBracketCreator {
  def create(doc: Document, change: TextChange): CloseAngleBracket =
    new CloseAngleBracket {
      override val document = doc
      override val textChange = change
    }
}
