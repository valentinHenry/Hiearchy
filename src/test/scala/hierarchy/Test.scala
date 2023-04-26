package fr.valentinhenry
package hierarchy

import hierarchy.Hierarchy._
import hierarchy.HierarchyImpls.HierarchyModuleCollection

import cats.effect.ExitCode.Success
import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Test extends IOApp {
  val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    val H: HierarchyDSL[IO] = dsl[IO]

    def provideModule1: HEP[IO, Config1, Module1] =
      H.get[Config1].flatMap { config =>
        H.provide_(Module1(config))
      }

    def provideModule2: HEP[IO, Config2, Module2] =
      H.get[Config2].flatMap { config =>
        H.provide_[Module2](Module2.Live(config))
      }

    def makeSubModule: HEPR[IO, Config3, Module3, Module2 with Module1] =
      for {
        m1      <- H.summon[Module1]
        m2      <- H.summon[Module2]
        config3 <- H.get[Config3]
        _       <- H.provide_(Module3(config3, m1, m2))
      } yield ()

//    Hierarchy.empty not necessary
//      .product(provideModule1)

    for {
      m1 <- provideModule1
      m2 <- provideModule2
      m3 <- makeSubModule
      // infra <- makeInfra(m1, m2, m3)
    } yield ()
    val pomme: Hierarchy[
      IO,
      Config1 with Config2 with Config3,
      Module4 with Module1 with Module2 with Module3,
      Module2 with Module1,
      Module2
    ] = provideModule1
      .product(provideModule2)
      .product(makeSubModule)
      .provide(Module4())
      .evalTap(_ => logger.info("Salut"))
      .swapF[Module2](logger.info("swapping module2").as(Module2.Test()))

    pomme
      .run(Config())
      .as(Success)
  }
}

trait Config1
trait Config2
trait Config3
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
