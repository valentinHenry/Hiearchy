package fr.valentinhenry
package hierarchy

import hierarchy.Hierarchy.{HA, HierarchyDSL, NoRequirement, NothingProvided}
import hierarchy.capabilities.NoPostProvidedModules

import cats.effect.{IO, Ref}
import munit.CatsEffectSuite

import scala.annotation.{implicitAmbiguous, unused}

class OrderSpecs extends CatsEffectSuite {
  val H: HierarchyDSL[IO] = Hierarchy.dsl[IO]

  test("Monad is evaluated in the right order") {
    Ref.of[IO, Int](0).flatMap { ref =>
      def checkEqual(value: Int): HA[IO, Unit] =
        H.liftF {
          ref.getAndSet(value + 1).map(assertEquals(_, value))
        }

      val embedded: HA[IO, Unit] = checkEqual(1) *> checkEqual(2)

      val rt = for {
        _ <- checkEqual(0)
        _ <- embedded
        _ <- checkEqual(3)
      } yield ()

      rt.run()
    }
  }

  test("Before run is evaluating modules in reverse order of provided") {
    Ref.of[IO, Int](1).flatMap { ref =>
      H.provide(new Module1(ref, 3))
        .provide(new Module21(ref, 2))
        .provide(new Module3(ref, 1))
        .run()
    }
  }

  test("Swap does not change before run order") {
    Ref.of[IO, Int](1).flatMap { ref =>
      H.provide(new Module1(ref, 3))
        .provide[Module2](new Module21(ref, 2))
        .provide(new Module3(ref, 1))
        .swap[Module2](new Module22(ref, 2))
        .run()
    }
  }

  final class Module1(ref: Ref[IO, Int], expected: Int) extends ModifierHierarchyModule[IO] {
    override type Requirements = NoRequirement

    override def beforeRun(
      collection: HierarchyImpls.HierarchyModuleCollection[IO, NoRequirement]
    ): IO[HierarchyImpls.HierarchyModuleCollection[IO, NoRequirement]] =
      ref
        .getAndSet(expected + 1)
        .map(assertEquals(_, expected))
        .as(collection)
  }

  trait Module2                                          extends ModifierHierarchyModule[IO] {
    override type Requirements = NoRequirement
  }
  final class Module21(ref: Ref[IO, Int], expected: Int) extends Module2                     {

    override def beforeRun(
      collection: HierarchyImpls.HierarchyModuleCollection[IO, NoRequirement]
    ): IO[HierarchyImpls.HierarchyModuleCollection[IO, NoRequirement]] =
      ref
        .getAndSet(expected + 1)
        .map(assertEquals(_, expected))
        .as(collection)
  }

  final class Module22(ref: Ref[IO, Int], expected: Int) extends Module2 {

    override def beforeRun(
      collection: HierarchyImpls.HierarchyModuleCollection[IO, NoRequirement]
    ): IO[HierarchyImpls.HierarchyModuleCollection[IO, NoRequirement]] =
      ref
        .getAndSet(expected + 1)
        .map(assertEquals(_, expected))
        .as(collection)
  }

  final class Module3(ref: Ref[IO, Int], expected: Int) extends ModifierHierarchyModule[IO] {
    override type Requirements = NoRequirement
    override def beforeRun(
      collection: HierarchyImpls.HierarchyModuleCollection[IO, NoRequirement]
    ): IO[HierarchyImpls.HierarchyModuleCollection[IO, NoRequirement]] =
      ref
        .getAndSet(expected + 1)
        .map(assertEquals(_, expected))
        .as(collection)
  }
}

trait InvalidRequirementCheck[LP, LR, RP]

object InvalidRequirementCheck {
  final val singleton = new InvalidRequirementCheck[Any, Any, Any] {}

  implicit def base[LP, LR, RP]: InvalidRequirementCheck[LP, LR, RP] =
    singleton.asInstanceOf[InvalidRequirementCheck[LP, LR, RP]]

  @implicitAmbiguous(
    "Test fails. The requirement from ${LR}, with provided ${LP} is not invalid with the additional provided modules ${RP}"
  )
  implicit def instance1[LP, LR, RP](implicit
    @unused ev: NoPostProvidedModules[LP, LR, RP]
  ): InvalidRequirementCheck[LP, LR, RP] =
    singleton.asInstanceOf[InvalidRequirementCheck[LP, LR, RP]]
}
