package scala.tools.eclipse.debug.model

import org.junit.Test
import com.sun.jdi.ThreadReference
import org.mockito.Mockito._
import org.junit.Assert._
import com.sun.jdi.VMDisconnectedException
import org.junit.Before
import org.eclipse.debug.core.DebugPlugin
import com.sun.jdi.ObjectCollectedException

/**
 * Tests for ScalaThread.
 */
class ScalaThreadTest {
  
  @Before
  def initializeDebugPlugin() {
    new DebugPlugin
  }
  
  @Test
  def getName() {
    val jdiThread= mock(classOf[ThreadReference])
    
    when(jdiThread.name).thenReturn("some test string")
    
    val thread= new ScalaThread(null, jdiThread)
    
    assertEquals("Bad thread name", "some test string", thread.getName)
  }
  
  @Test
  def vmDisconnectedExceptionOnGetName() {
    val jdiThread= mock(classOf[ThreadReference])
    
    when(jdiThread.name).thenThrow(new VMDisconnectedException)
    
    val thread= new ScalaThread(null, jdiThread)
    
    assertEquals("Bad thread name on VMDisconnectedException", "<disconnected>", thread.getName)
  }
  
  @Test
  def objectCollectedExceptionOnGetName() {
    val jdiThread= mock(classOf[ThreadReference])
    
    when(jdiThread.name).thenThrow(new ObjectCollectedException)
    
    val thread= new ScalaThread(null, jdiThread)
    
    assertEquals("Bad thread name", "<garbage collected>", thread.getName)
  }

}