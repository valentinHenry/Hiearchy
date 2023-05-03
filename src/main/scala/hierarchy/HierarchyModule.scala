package fr.valentinhenry
package hierarchy

import hierarchy.HierarchyImpls.HierarchyModuleCollection

import scala.reflect.{ClassTag, classTag}

final class Key[ModuleT](val id: String) extends AnyVal {
  def narrowed[SubModuleT <: ModuleT]: Key[SubModuleT] = this.asInstanceOf[Key[SubModuleT]]
}

object Key {
  def apply[ModuleT: Key]: Key[ModuleT]            = implicitly
  implicit def instance[ModuleT: ClassTag]: Key[ModuleT] =
    new Key(classTag[ModuleT].toString())
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
