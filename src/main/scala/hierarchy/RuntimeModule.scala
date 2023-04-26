package fr.valentinhenry
package hierarchy

import hierarchy.HierarchyImpls.HierarchyModuleCollection

import scala.reflect.{ClassTag, classTag}

final class ModuleKey[ModuleT](val id: String) extends AnyVal {
  def narrowed[SubModuleT <: ModuleT]: ModuleKey[SubModuleT] = this.asInstanceOf[ModuleKey[SubModuleT]]
}

object ModuleKey {
  def apply[ModuleT: ModuleKey]: ModuleKey[ModuleT]            = implicitly
  implicit def instance[ModuleT: ClassTag]: ModuleKey[ModuleT] =
    new ModuleKey(classTag[ModuleT].toString())
}

trait ModifierHierarchyModule[F[_]] {
  type Requirements

  def beforeRun(collection: HierarchyModuleCollection[F, Requirements]): F[HierarchyModuleCollection[F, Requirements]]
}
object ModifierHierarchyModule      {
  type Aux[F[_], R] = ModifierHierarchyModule[F] { type Requirements = R }
}

trait RunnableHierarchyModule[F[_]] {
  def run: F[Unit]
}
