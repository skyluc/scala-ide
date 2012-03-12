package scala.tools.eclipse.debug

import junit.framework.TestSuite
import org.junit.runners.Suite
import org.junit.runner.RunWith
import scala.tools.eclipse.debug.model.ScalaThreadTest
import scala.tools.eclipse.debug.model.ScalaDebugModelPresentationTest
import scala.tools.eclipse.debug.model.ScalaStackFrameTest
import scala.tools.eclipse.debug.model.ScalaValueTest

/**
 * Junit test suite for the Scala debugger.
 */

@RunWith(classOf[Suite])
@Suite.SuiteClasses(
  Array(
    classOf[ScalaDebugSteppingTest],
    classOf[ScalaThreadTest],
    classOf[ScalaDebugModelPresentationTest],
    classOf[ScalaStackFrameTest],
    classOf[ScalaValueTest]))
class ScalaDebugTestSuite {
}