package fr.valentinhenry
package hierarchy

import hierarchy.Hierarchy.HierarchyDSL
import hierarchy.HierarchyImpls.HierarchyModuleCollection
import hierarchy.instances.AndSyntax

import cats.effect.ExitCode.Success
import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger
//import shapeless.ops.record.Merger

object Test extends IOApp {
//  val merger: Merger = ???
  val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    val H: HierarchyDSL[IO] = Hierarchy.dsl[IO]

    def provideModule1 =
      H.get[Config1].flatMap { config =>
        H.provide_(Module1(config))
      }

    def provideModule2 =
      H.get[Config2].flatMap { config =>
        H.provide_[Module2](Module2.Live(config))
      }

    def makeSubModule =
      for {
        m1      <- H.summon[Module1]
        m2      <- H.summon[Module2]
        config3 <- H.get[Config3]
        _       <- H.provide_(Module3(config3, m1, m2))
      } yield ()

    provideModule1
      .product(provideModule2)
      .product(makeSubModule)
      .product_(H.provide[Module4](Module4()))
      .evalTap(_ => logger.info("Salut"))
      .swapF[Module2](logger.info("swapping module2").as(Module2.Test()))
      .runner
//      .give[Config1 &: Config2 &: Config3](C1 &: C2 &: C3)
      .give(C1)
      .give(C2)
      .give(C3)
      .run
      .as(Success)
  }

  implicitly[EnvRemover.Aux[Config, Config1 &: Config2 &: Config3, Any]]: Unit
}

trait Config1
case object C1 extends Config1
trait Config2
case object C2 extends Config2
trait Config3
case object C3 extends Config3

final case class Config() extends Config1 with Config2 with Config3

final case class Module1(c1: Config1) extends RunnableHierarchyModule[IO] {
  override def run: IO[Unit] = Slf4jLogger.getLogger[IO].info("I am running")
}
trait Module2 {
  def text: String
}
object Module2 {
  final case class Live(c2: Config2) extends Module2 {
    val text: String = "hello there"
  }
  final case class Test()            extends Module2 {
    override def text: String = "this is a test"
  }

}

final case class Module3(config: Config3, m1: Module1, m2: Module2) extends ModifierHierarchyModule[IO] {
  override type Requirements = Module2

  override def beforeRun(
    collection: HierarchyModuleCollection[IO, Requirements]
  ): IO[HierarchyModuleCollection[IO, Requirements]] =
    IO.pure(
      collection.useF[Module2](m2 => Slf4jLogger.getLogger[IO].info(s"Well, i'm before running: ${m2.text}"))
    )
}

final case class Module4()
