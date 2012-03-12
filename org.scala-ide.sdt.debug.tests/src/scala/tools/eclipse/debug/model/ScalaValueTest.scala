package scala.tools.eclipse.debug.model

import org.junit.Test
import org.mockito.Mockito._
import org.junit.Assert._
import org.junit.Before
import org.eclipse.debug.core.DebugPlugin
import com.sun.jdi.BooleanValue
import com.sun.jdi.ByteValue
import com.sun.jdi.CharValue
import com.sun.jdi.DoubleValue
import com.sun.jdi.FloatValue
import com.sun.jdi.IntegerValue
import com.sun.jdi.LongValue
import com.sun.jdi.ShortValue
import com.sun.jdi.StringReference
import com.sun.jdi.ReferenceType
import com.sun.jdi.Field

class ScalaValueTest {

  @Before
  def initializeDebugPlugin() {
    if (DebugPlugin.getDefault == null) {
      new DebugPlugin
    }
  }

  @Test
  def booleanValueTrue() {
    val jdiValue = mock(classOf[BooleanValue])
    when(jdiValue.value).thenReturn(true)

    val scalaValue = ScalaValue(jdiValue, null)

    assertEquals("Bad type", "scala.Boolean", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "true", scalaValue.getValueString)
    assertFalse("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 0, scalaValue.getVariables.length)
  }

  @Test
  def booleanValueFalse() {
    val jdiValue = mock(classOf[BooleanValue])
    when(jdiValue.value).thenReturn(false)

    val scalaValue = ScalaValue(jdiValue, null)

    assertEquals("Bad type", "scala.Boolean", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "false", scalaValue.getValueString)
    assertFalse("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 0, scalaValue.getVariables.length)
  }
  
  @Test
  def byteValue() {
    val jdiValue = mock(classOf[ByteValue])
    when(jdiValue.value).thenReturn(64.asInstanceOf[Byte])
    
    val scalaValue = ScalaValue(jdiValue, null)
    
    assertEquals("Bad type", "scala.Byte", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "64", scalaValue.getValueString)
    assertFalse("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 0, scalaValue.getVariables.length)
  }

  @Test
  def charValue() {
    val jdiValue = mock(classOf[CharValue])
    when(jdiValue.value).thenReturn('z')
    
    val scalaValue = ScalaValue(jdiValue, null)
    
    assertEquals("Bad type", "scala.Char", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "z", scalaValue.getValueString)
    assertFalse("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 0, scalaValue.getVariables.length)
  }

  @Test
  def doubleValue() {
    val jdiValue = mock(classOf[DoubleValue])
    when(jdiValue.value).thenReturn(4.55d)
    
    val scalaValue = ScalaValue(jdiValue, null)
    
    assertEquals("Bad type", "scala.Double", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "4.55", scalaValue.getValueString)
    assertFalse("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 0, scalaValue.getVariables.length)
  }

  @Test
  def floatValue() {
    val jdiValue = mock(classOf[FloatValue])
    when(jdiValue.value).thenReturn(82.9f)
    
    val scalaValue = ScalaValue(jdiValue, null)
    
    assertEquals("Bad type", "scala.Float", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "82.9", scalaValue.getValueString)
    assertFalse("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 0, scalaValue.getVariables.length)
  }

  @Test
  def intValue() {
    val jdiValue = mock(classOf[IntegerValue])
    when(jdiValue.value).thenReturn(32)
    
    val scalaValue = ScalaValue(jdiValue, null)
    
    assertEquals("Bad type", "scala.Int", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "32", scalaValue.getValueString)
    assertFalse("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 0, scalaValue.getVariables.length)
  }

  @Test
  def longValue() {
    val jdiValue = mock(classOf[LongValue])
    when(jdiValue.value).thenReturn(128L)
    
    val scalaValue = ScalaValue(jdiValue, null)
    
    assertEquals("Bad type", "scala.Long", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "128", scalaValue.getValueString)
    assertFalse("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 0, scalaValue.getVariables.length)
  }

  @Test
  def shortValue() {
    val jdiValue = mock(classOf[ShortValue])
    when(jdiValue.value).thenReturn(334.asInstanceOf[Short])
    
    val scalaValue = ScalaValue(jdiValue, null)
    
    assertEquals("Bad type", "scala.Short", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "334", scalaValue.getValueString)
    assertFalse("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 0, scalaValue.getVariables.length)
  }

  @Test
  def stringValue() {
    import scala.collection.JavaConverters._
    val jdiValue = mock(classOf[StringReference])
    when(jdiValue.value).thenReturn("some string")
    when(jdiValue.uniqueID).thenReturn(15)
    val jdiReferenceType = mock(classOf[ReferenceType])
    when(jdiValue.referenceType).thenReturn(jdiReferenceType)
    val field= mock(classOf[Field])
    val allFields= List(field, field, field, field).asJava
    when(jdiReferenceType.allFields).thenReturn(allFields)
    
    val scalaValue = ScalaValue(jdiValue, null)
    
    assertEquals("Bad type", "java.lang.String", scalaValue.getReferenceTypeName)
    assertEquals("Bad value", "\"some string\" (id=15)", scalaValue.getValueString)
    assertTrue("Should not have variables", scalaValue.hasVariables)
    assertEquals("Should not have variables", 4, scalaValue.getVariables.length)
  }

}