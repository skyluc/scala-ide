package org.scalaide.core
package structurebuilder

import org.junit._
import testsetup.SDTTestUtils._
import testsetup.TestProjectSetup
import org.eclipse.core.resources.IFile
import java.util.NoSuchElementException
import org.scalaide.core.compiler.IScalaPresentationCompiler
import org.scalaide.core.compiler.IScalaPresentationCompiler.Implicits._

object ScalaJavaMapperTest extends TestProjectSetup("javamapper") {
  val unit = scalaCompilationUnit("/pack/Target.scala")
}

class ScalaJavaMapperTest {
  import ScalaJavaMapperTest._

  @Test
  def intDescriptor() {
    withTargetTree("abstract class Target { val target: Int }") {
      new TypeTest {
        def apply(compiler: IScalaPresentationCompiler)(tpe: compiler.Type) {
          val desc = compiler.javaDescriptor(tpe)
          Assert.assertEquals("wrong descriptor", "I", desc)
        }
      }
    }
  }

  @Test
  def listDescriptor() {
    withTargetTree("abstract class Target { val target: List[Int] }") {
      new TypeTest {
        def apply(compiler: IScalaPresentationCompiler)(tpe: compiler.Type) {
          val desc = compiler.javaDescriptor(tpe)
          Assert.assertEquals("wrong descriptor", "Lscala/collection/immutable/List;", desc)
        }
      }
    }
  }

  @Test
  def primitiveArrayDescriptor() {
    withTargetTree("abstract class Target { val target: Array[Array[Char]] }") {
      new TypeTest {
        def apply(compiler: IScalaPresentationCompiler)(tpe: compiler.Type) {
          val desc = compiler.javaDescriptor(tpe)
          Assert.assertEquals("wrong descriptor", "[[C", desc)
        }
      }
    }
  }

  @Test
  def refArrayDescriptor() {
    withTargetTree("abstract class Target { val target: Array[Object] }") {
      new TypeTest {
        def apply(compiler: IScalaPresentationCompiler)(tpe: compiler.Type) {
          val desc = compiler.javaDescriptor(tpe)
          Assert.assertEquals("wrong descriptor", "[Ljava/lang/Object;", desc)
        }
      }
    }
  }

  @Test
  def innerClassDescriptor() {
    withTargetTree("abstract class Target { class Inner; val target: Inner }") {
      new TypeTest {
        def apply(compiler: IScalaPresentationCompiler)(tpe: compiler.Type) {
          val desc = compiler.javaDescriptor(tpe)
          Assert.assertEquals("wrong descriptor", "LTarget/Inner;", desc)
        }
      }
    }
  }

  @Test
  def typeVarClassDescriptor() {
    withTargetTree("abstract class Target[T] { val target: T }") {
      new TypeTest {
        def apply(compiler: IScalaPresentationCompiler)(tpe: compiler.Type) {
          val desc = compiler.javaDescriptor(tpe)
          Assert.assertEquals("wrong descriptor", "Ljava/lang/Object;", desc)
        }
      }
    }
  }

  @Test
  def errorClassDescriptor() {
    withTargetTree("abstract class Target { val target: NotFount }") {
      new TypeTest {
        def apply(compiler: IScalaPresentationCompiler)(tpe: compiler.Type) {
          val desc = compiler.javaDescriptor(tpe)
          Assert.assertEquals("wrong descriptor", "Ljava/lang/Object;", desc)
        }
      }
    }
  }
  /** Retrieve the `target` type from the given source and pass it to the type test.
   *
   *  The `src` is supposed to contain one abstract val called `target`, whose type
   *  is retrieved and passed to the type test.
   *
   *  This method reloads `src` in the presentation compiler and waits for the source
   *  to be fully-typechecked, before traversing the tree to find the `target` definition.
   */
  def withTargetTree(src: String)(f: TypeTest) = {
    changeContentOfFile(unit.getResource().asInstanceOf[IFile], src)

    unit.withSourceFile { (srcFile, compiler) =>
      compiler.askReload(unit, srcFile)
      val targets = compiler.askLoadedTyped(srcFile, keepLoaded = false).get match {
        case Left(loadedType) =>
          loadedType.collect {
            case t: compiler.DefDef if t.name.toString startsWith "target" => t
          }
        case Right(e) =>
          throw e
      }
      compiler.asyncExec {
        f(compiler)(targets.head.symbol.info.finalResultType)
      }.getOption()
    } getOrElse (throw new NoSuchElementException(s"Could not find target element in $src"))
  }
}

/** This class is a function of 2 parameters, compiler and a compiler.Type.
 *  It is needed because path dependent type as parameters are not allowed
 *  in anonymous functions.
 */
trait TypeTest {
  def apply(compiler: IScalaPresentationCompiler)(tree: compiler.Type)
}
