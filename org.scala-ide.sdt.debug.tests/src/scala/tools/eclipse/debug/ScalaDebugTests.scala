package scala.tools.eclipse.debug

import junit.framework.TestSuite
import org.junit.runners.Suite
import org.junit.runner.RunWith
import scala.tools.eclipse.debug.model.ScalaThreadTest

/**
 * Junit test suite for the Scala debugger.
 */

@RunWith(classOf[Suite])
@Suite.SuiteClasses(
  Array(
    classOf[ScalaDebugSteppingTest],
    classOf[ScalaThreadTest]))
class ScalaDebugTestSuite {
}