package fr.valentinhenry
package runtime4s

import runtime4s.Runtime._
import runtime4s.RuntimeImpls.RuntimeModuleCollection

import cats.effect.ExitCode.Success
import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Test extends IOApp {
  val jsp = implicitly[Any with String with Float <:< Float]

  override def run(args: List[String]): IO[ExitCode] = {
    val Runtime: RuntimeFDSL[IO] = dsl[IO]

    def provideModule1: REP[IO, Config1, Module1] =
      Runtime.get[Config1].flatMap { config =>
        Runtime.provide_(Module1(config))
      }

    def provideModule2: REP[IO, Config2, Module2] =
      Runtime.get[Config2].flatMap { config =>
        Runtime.provide_(Module2(config))
      }

    def makeSubModule: Runtime[IO, Config3, Module3, Module2 with Module1, Unit] =
      for {
        m1      <- Runtime.summon[Module1]
        m2      <- Runtime.summon[Module2]
        config3 <- Runtime.get[Config3]
        _       <- Runtime.provide_(Module3(config3, m1, m2))
      } yield ()

    Runtime.empty
      .product_(provideModule2)
      .product_(provideModule2)
      .product_(provideModule2)
      .product_(provideModule1)
      .product_(Runtime.provide_(Module4()))
      .product_(Runtime.provide_(Module4()))
      .product_(Runtime.provide_(Module4()))
      .product_(makeSubModule)
      .run(Config())
      .as(Success)
  }
}

trait Config1
trait Config2
trait Config3
final case class Config() extends Config1 with Config2 with Config3

final case class Module1(c1: Config1)                               extends RunnableRuntimeModule[IO] {
  override def run: IO[Unit] = Slf4jLogger.getLogger[IO].info("I am running")
}
final case class Module2(c2: Config2) {
  val text: String = "hello there"
}
final case class Module3(config: Config3, m1: Module1, m2: Module2) extends ModifierRuntimeModule[IO] {
  override type Requirements = Module2

  override def beforeRun(
    collection: RuntimeModuleCollection[IO, Requirements]
  ): IO[RuntimeModuleCollection[IO, Requirements]] =
    IO.pure(
      collection.useF[Module2](m2 => Slf4jLogger.getLogger[IO].info(s"Well, i'm before running: ${m2.text}"))
    )
}

final case class Module4()