package fr.valentinhenry
package runtime4s

import runtime4s.RuntimeImpls.RuntimeModuleCollection

import scala.reflect.{ClassTag, classTag}

final class RuntimeKey[ModuleT](val id: String) extends AnyVal {
  def narrowed[SubModuleT <: ModuleT]: RuntimeKey[SubModuleT] = this.asInstanceOf[RuntimeKey[SubModuleT]]
}

object RuntimeKey {
  def apply[ModuleT: RuntimeKey]: RuntimeKey[ModuleT]           = implicitly
  implicit def instance[ModuleT: ClassTag]: RuntimeKey[ModuleT] =
    new RuntimeKey(classTag[ModuleT].toString())
}

trait ModifierRuntimeModule[F[_]] {
  type Requirements

  def beforeRun(collection: RuntimeModuleCollection[F, Requirements]): F[RuntimeModuleCollection[F, Requirements]]
}
object ModifierRuntimeModule      {
  type Aux[F[_], R] = ModifierRuntimeModule[F] { type Requirements = R }
}

trait RunnableRuntimeModule[F[_]] {
  def run: F[Unit]
}
