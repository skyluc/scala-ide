package scala.tools.eclipse.debug

import com.sun.jdi.StackFrame
import org.eclipse.debug.core.model.IStackFrame
import org.eclipse.jdt.debug.core.IJavaStackFrame

class ScalaStackFrame(target: ScalaDebugTarget, val thread: ScalaThread, val stackFrame: StackFrame) extends ScalaDebugElement(target) with IStackFrame {

  // Members declared in org.eclipse.debug.core.model.IStackFrame
  
  def getCharEnd(): Int = -1
  def getCharStart(): Int = -1
  def getLineNumber(): Int = stackFrame.location.lineNumber // TODO: cache data ?
  def getName(): String = stackFrame.location.declaringType.name // TODO: cache data ?
  def getRegisterGroups(): Array[org.eclipse.debug.core.model.IRegisterGroup] = ???
  def getThread(): org.eclipse.debug.core.model.IThread = thread
  def getVariables(): Array[org.eclipse.debug.core.model.IVariable] = Array() // TODO: need real logic
  def hasRegisterGroups(): Boolean = ???
  def hasVariables(): Boolean = ???
  
  // Members declared in org.eclipse.debug.core.model.IStep
  
  def canStepInto(): Boolean = false // TODO: need real logic
  def canStepOver(): Boolean = true // TODO: need real logic
  def canStepReturn(): Boolean = false // TODO: need real logic
  def isStepping(): Boolean = ???
  def stepInto(): Unit = ???
  def stepOver(): Unit = thread.stepOver
  def stepReturn(): Unit = ???
  
  // Members declared in org.eclipse.debug.core.model.ISuspendResume
  
  def canResume(): Boolean = false // TODO: need real logic
  def canSuspend(): Boolean = false // TODO: need real logic
  def isSuspended(): Boolean = true // TODO: need real logic
  def resume(): Unit = ???
  def suspend(): Unit = ???
  
  // ---
  
  fireCreationEvent
  
  def getSourceName(): String = stackFrame.location.sourceName

}