package scala.tools.eclipse.debug

import org.eclipse.debug.core.model.IWatchExpressionDelegate
import org.eclipse.debug.core.model.IDebugElement
import org.eclipse.debug.core.model.IWatchExpressionListener
import org.eclipse.debug.core.model.IWatchExpressionResult
import scala.tools.eclipse.debug.model.ScalaStackFrame
import scala.tools.eclipse.debug.model.ScalaValue

class ScalaWatchExpressionDelegate extends IWatchExpressionDelegate {
  
  def evaluateExpression(expression: String , context: IDebugElement, listener: IWatchExpressionListener ) {
    context match {
      case scalaStackFrame: ScalaStackFrame =>
        val value= scalaStackFrame.thread.lastExitValue
        if (value == null) {
          listener.watchEvaluationFinished(NotAvailableResult)
        } else {
          listener.watchEvaluationFinished(new Result(value))
        }
      case _ =>
      listener.watchEvaluationFinished(NotAvailableResult)
    }
  }

}

object NotAvailableResult extends IWatchExpressionResult {
  
   def getErrorMessages(): Array[String] = Array() // TODO: need real logic
   def getException(): org.eclipse.debug.core.DebugException = null // TODO: need real logic
   def getExpressionText(): String = ScalaDebugger.METHOD_EXIT_WATCH_EXPRESSION_KEY // TODO: need real logic
   def getValue(): org.eclipse.debug.core.model.IValue = null // TODO: need real logic
   def hasErrors(): Boolean = false // TODO: need real logic
   
}

class Result(value: ScalaValue) extends IWatchExpressionResult {
  
   def getErrorMessages(): Array[String] = Array()
   def getException(): org.eclipse.debug.core.DebugException = null
   def getExpressionText(): String = ScalaDebugger.METHOD_EXIT_WATCH_EXPRESSION_KEY
   def getValue(): org.eclipse.debug.core.model.IValue = value
   def hasErrors(): Boolean = false
   
}