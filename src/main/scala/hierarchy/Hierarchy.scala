package fr.valentinhenry
package hierarchy

import hierarchy.Hierarchy.{EmptyEnvironment, NothingProvided}
import hierarchy.capabilities._

import cats._
import cats.effect.Sync
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.annotation.implicitNotFound
import scala.annotation.unchecked.uncheckedVariance

sealed trait Hierarchy[F[_], -Environment, -Provided, -Requirements, +A] {
  def run(e: Environment)(implicit
    @implicitNotFound("A runtime cannot be run unless all requirements are fulfilled")
    isRunnable: Provided @uncheckedVariance <:< Requirements,
    P: Parallel[F],
    S: Sync[F]
  ): F[Unit]

  def map[B](f: A => B): Hierarchy[F, Environment, Provided, Requirements, B]

  def as[B](b: => B): Hierarchy[F, Environment, Provided, Requirements, B] = map(_ => b)

  def <*[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, A]

  def *>[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B]

  def >>[E, P, R, B](
    f: A => Hierarchy[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B]

  def flatMap[E, P, R, B](
    f: A => Hierarchy[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B]

  def flatTap[E, P, R, B](
    f: A => Hierarchy[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, A]

  def flatMapF[E, P, R, B](
    f: A => F[Hierarchy[F, E, P, R, B]]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B]

  def flatTapF[E, P, R, B](
    f: A => F[Hierarchy[F, E, P, R, B]]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, A]

  def evalMap[B](f: A => F[B]): Hierarchy[F, Environment, Provided, Requirements, B]
  def evalTap[B](f: A => F[B]): Hierarchy[F, Environment, Provided, Requirements, A]

  def product[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, (A, B)]

  def product_[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, Unit]

  def productL[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, A]

  def productR[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B]

  def provide[Module: ModuleKey](module: Module)(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, Module]

  def provideF[Module: ModuleKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, Module]

  def provide_[Module: ModuleKey](module: Module)(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, Unit]

  def provideF_[Module: ModuleKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, Unit]

  def provideTap[Module: ModuleKey](module: Module)(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, A]

  def provideFTap[Module: ModuleKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, A]

  def swap[Module: ModuleKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Hierarchy[F, Environment, Provided, R.Requirements with Requirements with Module, Module]

  def swapF[Module: ModuleKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module]
  ): Hierarchy[F, Environment, Provided, R.Requirements with Requirements with Module, Module]

  def swap_[Module: ModuleKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Hierarchy[F, Environment, Provided, R.Requirements with Requirements with Module, Unit]

  def swapF_[Module: ModuleKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module]
  ): Hierarchy[F, Environment, Provided, R.Requirements with Requirements with Module, Unit]

  def swapTap[Module: ModuleKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Hierarchy[F, Environment, Provided, R.Requirements with Requirements with Module, A]

  def swapFTap[Module: ModuleKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module]
  ): Hierarchy[F, Environment, Provided, R.Requirements with Requirements with Module, A]

  def narrow[P >: Provided @uncheckedVariance]: Hierarchy[F, Environment, P, Requirements, A]

  def widen[B >: A]: Hierarchy[F, Environment, Provided, Requirements, B]
}

object Hierarchy {
  type EmptyEnvironment = Any
  type NothingProvided  = Any
  type NoRequirement    = Any

  type HEPR[F[_], -E, -P, R] = Hierarchy[F, E, P, R, Unit]
  type HEP[F[_], -E, -P]     = Hierarchy[F, E, P, NoRequirement, Unit]
  type HE[F[_], -E]          = Hierarchy[F, E, NothingProvided, NoRequirement, Unit]
  type HA[F[_], A]           = Hierarchy[F, EmptyEnvironment, NothingProvided, NoRequirement, A]

  def empty[F[_]: Monad]: HA[F, Unit] =
    HierarchyImpls.Consumer[F, EmptyEnvironment, NothingProvided, NoRequirement, Unit, Unit](
      inner = (_, _) => ().pure[F],
      extractProvided = _ => ()
    )

  // Gets an environment value
  def get[F[_]: Monad, E]: Hierarchy[F, E, NothingProvided, NoRequirement, E] =
    HierarchyImpls.Consumer[F, E, NothingProvided, NoRequirement, Unit, E](
      inner = (env, _) => env.pure[F],
      extractProvided = _ => ()
    )

  // Summon a provided module
  def summon[F[_]: Monad, M: ModuleKey]: Hierarchy[F, EmptyEnvironment, NothingProvided, M, M] =
    HierarchyImpls.Consumer[F, EmptyEnvironment, NothingProvided, M, M, M](
      inner = (_, requirements) => requirements.pure[F],
      extractProvided = _(ModuleKey[M]).asInstanceOf[M]
    )

  def provide[F[_]: Monad, Module: ModuleKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Hierarchy[F, EmptyEnvironment, Module, R.Requirements, Module] =
    HierarchyImpls.Producer[F, Module, R.Requirements, Module, Module](
      inner = (module, module).pure[F]
    )

  def provide_[F[_]: Monad, Module: ModuleKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Hierarchy[F, EmptyEnvironment, Module, R.Requirements, Unit] =
    HierarchyImpls.Producer[F, Module, R.Requirements, Module, Unit](
      inner = (module, ()).pure[F]
    )

  def swap[F[_]: Monad, Module: ModuleKey](newModule: Module)(implicit
    R: RequirementExtractor[F, Module] // TODO: Find a way to remove old module requirements //FIXME maybe remove ?
  ): Hierarchy[F, EmptyEnvironment, NothingProvided, Module with R.Requirements, Module] =
    HierarchyImpls.Producer[F, NothingProvided, Module with R.Requirements, Module, Module](
      inner = (newModule, newModule).pure[F]
    )

  def swap_[F[_]: Monad, Module: ModuleKey](newModule: Module)(implicit
    R: RequirementExtractor[F, Module] // TODO: Find a way to remove old module requirements //FIXME maybe remove ?
  ): Hierarchy[F, EmptyEnvironment, NothingProvided, Module with R.Requirements, Unit] =
    HierarchyImpls.Producer[F, NothingProvided, Module with R.Requirements, Module, Unit](
      inner = (newModule, ()).pure[F]
    )

  def update[F[_]: Monad, Module: ModuleKey](
    update: Module => Module
  ): Hierarchy[F, EmptyEnvironment, NothingProvided, Module, Module] =
    HierarchyImpls.Updater[F, Module, Module](
      inner = oldModule => Monad[F].pure { val newModule = update(oldModule); (newModule, newModule) }
    )

  def pure[F[_]: Monad, A](a: A): HA[F, A] =
    HierarchyImpls.Consumer[F, EmptyEnvironment, NothingProvided, NoRequirement, Unit, A](
      inner = (_, _) => Monad[F].pure(a),
      extractProvided = _ => ()
    )

  def delay[F[_]: Sync, A](a: => A): HA[F, A] =
    HierarchyImpls.Consumer[F, EmptyEnvironment, NothingProvided, NoRequirement, Unit, A](
      inner = (_, _) => Sync[F].delay(a),
      extractProvided = _ => ()
    )

  def liftF[F[_]: Monad, A](fa: F[A]): HA[F, A] =
    HierarchyImpls.Consumer[F, EmptyEnvironment, NothingProvided, NoRequirement, Unit, A](
      inner = (_, _) => fa,
      extractProvided = _ => ()
    )

  def dsl[F[_]: Monad]: HierarchyDSL[F] = new HierarchyDSL[F] {}

  abstract class HierarchyDSL[F[_]: Monad] {
    def empty: HA[F, Unit] = Hierarchy.empty[F]

    def get[E]: Hierarchy[F, E, NothingProvided, NoRequirement, E] = Hierarchy.get[F, E]

    def summon[M: ModuleKey]: Hierarchy[F, EmptyEnvironment, NothingProvided, M, M] = Hierarchy.summon[F, M]

    def provide[A: ModuleKey](a: A)(implicit
      R: RequirementExtractor[F, A]
    ): Hierarchy[F, EmptyEnvironment, A, R.Requirements, A] =
      Hierarchy.provide[F, A](a)

    def provide_[A: ModuleKey](a: A)(implicit
      R: RequirementExtractor[F, A]
    ): Hierarchy[F, EmptyEnvironment, A, R.Requirements, Unit] =
      Hierarchy.provide_[F, A](a)

    def swap[Module: ModuleKey](newModule: Module)(implicit
      R: RequirementExtractor[F, Module] // TODO: Find a way to remove old module requirements
    ): Hierarchy[F, EmptyEnvironment, NothingProvided, Module with R.Requirements, Module] =
      Hierarchy.swap[F, Module](newModule)

    def swap_[Module: ModuleKey](newModule: Module)(implicit
      R: RequirementExtractor[F, Module] // TODO: Find a way to remove old module requirements
    ): Hierarchy[F, EmptyEnvironment, NothingProvided, Module with R.Requirements, Unit] =
      Hierarchy.swap_[F, Module](newModule)

    def delay[A](a: => A)(implicit S: Sync[F]): HA[F, A] = Hierarchy.delay[F, A](a)

    def liftF[A](fa: F[A]): HA[F, A] = Hierarchy.liftF[F, A](fa)
  }
}

private[hierarchy] object HierarchyImpls {
  final val loggerName = "runtime4s.Hierarchy"

  type ModuleMap = Map[ModuleKey[_], Any]

  def logger[F[_]: Sync]: F[Logger[F]] = Sync[F].delay(Slf4jLogger.getLoggerFromName[F](loggerName))

  sealed abstract class Impl[F[_]: Monad, Environment, Provided, Requirements, A]
      extends Hierarchy[F, Environment, Provided, Requirements, A]
      with Product {
    override final def run(e: Environment)(implicit
      @implicitNotFound("A runtime cannot be run unless all requirements are fulfilled")
      isRunnable: Provided <:< Requirements,
      P: Parallel[F],
      S: Sync[F]
    ): F[Unit] =
      logger[F].flatMap { implicit logger =>
        def printHierarchy(modules: ModuleMap): F[Unit] = {
          val modifiers: List[String] = modules.values.toList.collect { case m: ModifierHierarchyModule[F] @unchecked =>
            m.getClass.getSimpleName
          }
          val runners: List[String]   = modules.values.toList.collect { case r: RunnableHierarchyModule[F] @unchecked =>
            r.getClass.getSimpleName
          }

          Logger[F].info(
            s"""
               ||‾| |‾(_)                       |‾|
               || |_| |_  ___ _ __ __ _ _ __ ___| |__  _   _
               ||  _  | |/ _ \\ '__/ _` | '__/ __| '_ \\| | | |
               || | | | |  __/ | | (_| | | | (__| | | | |_| |
               |\\_| |_/_|\\___|_|  \\__,_|_|  \\___|_| |_|\\__, |
               |                                        __/ |
               |                                       |___/
               |Hierarchy runtime modified by:
               |${modifiers.mkString(" - ", "\n - ", "")}
               |Registered modules to be run:
               |${runners.mkString(" - ", "\n - ", "")}
               |""".stripMargin
          )
        }

        def run(modules: ModuleMap): F[Unit] =
          modules.values.toList.collect { case m: RunnableHierarchyModule[F] @unchecked => m }
            .map(_.run)
            .parSequence_

        for {
          _         <- Logger[F].debug("Collecting modules")
          collected <- collect(e, HierarchyModuleCollector(Map.empty[ModuleKey[_], Any], Nil))
          _         <- Logger[F].debug(s"Running before run on ${collected._1.modules.size} modules")
          modules   <- collected._1.toCollection[F].beforeRun
          _         <- printHierarchy(modules)
          _         <- run(modules)
        } yield ()
      }

    protected[hierarchy] def collect(
      requirements: Environment,
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, A)]

    def narrow[P >: Provided]: Hierarchy[F, Environment, P, Requirements, A] =
      this.asInstanceOf[Hierarchy[F, Environment, P, Requirements, A]]

    def widen[B >: A]: Impl[F, Environment, Provided, Requirements, B]

    override final def as[B](b: => B): Hierarchy[F, Environment, Provided, Requirements, B] =
      map(_ => b)

    override def map[B](f: A => B): Hierarchy[F, Environment, Provided, Requirements, B] =
      evalMap(f(_).pure[F])

    override def evalMap[B](f: A => F[B]): Impl[F, Environment, Provided, Requirements, B]

    def <*[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, A] = productL(r)

    def *>[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B] = productR(r)

    def >>[E, P, R, B](
      f: A => Hierarchy[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B] = flatMap(f)

    override def flatMap[E, P, R, B](
      f: A => Hierarchy[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B] =
      flatMapF[E, P, R, B](f(_).pure[F])

    override def flatTap[E, P, R, B](f: A => Hierarchy[F, E, P, R, B])(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, A] =
      flatMap(a => f(a).as(a))

    override def flatMapF[E, P, R, B](
      f: A => F[Hierarchy[F, E, P, R, B]]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B] =
      FlatMap[F, Environment, Provided, Requirements, E, P, R, A, B](this, f)

    override def flatTapF[E, P, R, B](f: A => F[Hierarchy[F, E, P, R, B]])(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, A] =
      flatMapF(a => f(a).map(_.as(a)))

    override final def product[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, (A, B)] =
      flatMap[E, P, R, (A, B)](v => r.map(v -> _))

    override final def product_[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, Unit] =
      flatMap[E, P, R, B](_ => r).as(())

    override final def productL[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, A] =
      flatMap[E, P, R, A](a => r.as(a))

    override final def productR[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Hierarchy[F, Environment with E, Provided with P, Requirements with R, B] =
      flatMap[E, P, R, B](_ => r)

    override def evalTap[B](f: A => F[B]): Hierarchy[F, Environment, Provided, Requirements, A] =
      evalMap(a => f(a).as(a))

    override def provide[Module: ModuleKey](module: Module)(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Hierarchy[F, Environment, Provided with Module, Requirements with R.Requirements, Module] =
      productR(Hierarchy.provide[F, Module](module))

    override def provideF[Module: ModuleKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Hierarchy[F, Environment, Provided with Module, Requirements with R.Requirements, Module] =
      flatMapF(_ => module.map(Hierarchy.provide[F, Module](_)))

    override def provide_[Module: ModuleKey](module: Module)(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Hierarchy[F, Environment, Provided with Module, Requirements with R.Requirements, Unit] =
      product_(Hierarchy.provide[F, Module](module))

    override def provideF_[Module: ModuleKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Hierarchy[F, Environment, Provided with Module, Requirements with R.Requirements, Unit] =
      flatMapF(_ => module.map(Hierarchy.provide[F, Module](_))).as(())

    override def provideTap[Module: ModuleKey](module: Module)(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Hierarchy[F, Environment, Provided with Module, Requirements with R.Requirements, A] =
      productL(Hierarchy.provide[F, Module](module))

    override def provideFTap[Module: ModuleKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Hierarchy[F, Environment, Provided with Module, Requirements with R.Requirements, A] =
      flatTapF(_ => module.map(Hierarchy.provide[F, Module](_)))

    override def swap[Module: ModuleKey](module: Module)(implicit
      R: RequirementExtractor[F, Module]
    ): Hierarchy[F, Environment, Provided, Requirements with Module with R.Requirements, Module] =
      productR(Hierarchy.swap[F, Module](module))

    override def swapF[Module: ModuleKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module]
    ): Hierarchy[F, Environment, Provided, Requirements with Module with R.Requirements, Module] =
      flatMapF(_ => module.map(Hierarchy.swap[F, Module](_)))

    override def swap_[Module: ModuleKey](module: Module)(implicit
      R: RequirementExtractor[F, Module]
    ): Hierarchy[F, Environment, Provided, Requirements with Module with R.Requirements, Unit] =
      product_(Hierarchy.swap[F, Module](module))

    override def swapF_[Module: ModuleKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module]
    ): Hierarchy[F, Environment, Provided, Requirements with Module with R.Requirements, Unit] =
      flatMapF(_ => module.map(Hierarchy.swap[F, Module](_))).as(())

    override def swapTap[Module: ModuleKey](module: Module)(implicit
      R: RequirementExtractor[F, Module]
    ): Hierarchy[F, Environment, Provided, Requirements with Module with R.Requirements, A] =
      productL(Hierarchy.swap[F, Module](module))

    override def swapFTap[Module: ModuleKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module]
    ): Hierarchy[F, Environment, Provided, Requirements with Module with R.Requirements, A] =
      flatTapF(_ => module.map(Hierarchy.swap[F, Module](_)))
  }

  final case class Consumer[F[_]: Monad, Environment, Provided, Requirements, UsedRequirements, A](
    inner: (Environment, UsedRequirements) => F[A],
    extractProvided: ModuleMap => UsedRequirements
  ) extends Impl[F, Environment, Provided, Requirements, A] {

    override def widen[B >: A]: Impl[F, Environment, Provided, Requirements, B] =
      copy[F, Environment, Provided, Requirements, UsedRequirements, B](inner = inner(_, _).widen[B])

    override def evalMap[B](f: A => F[B]): Impl[F, Environment, Provided, Requirements, B] =
      copy[F, Environment, Provided, Requirements, UsedRequirements, B](inner = inner(_, _).flatMap(f))

    override protected[hierarchy] def collect(
      environment: Environment,
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, A)] =
      inner(environment, extractProvided(collector.modules)).map(collector -> _)
  }

  final case class Producer[F[_]: Monad, Provided, Requirements, M: ModuleKey, A](inner: F[(M, A)])
      extends Impl[F, EmptyEnvironment, Provided, Requirements, A] {

    override def widen[B >: A]: Impl[F, EmptyEnvironment, Provided, Requirements, B] =
      copy[F, Provided, Requirements, M, B](inner = inner.widen[(M, B)])

    override def evalMap[B](f: A => F[B]): Impl[F, EmptyEnvironment, Provided, Requirements, B] =
      copy[F, Provided, Requirements, M, B](inner = inner.flatMap { case (module, a) =>
        f(a).map(module -> _)
      })

    override protected[hierarchy] def collect(
      requirements: EmptyEnvironment,
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, A)] =
      inner.map { case (module, a) => collector.addModule[M](module) -> a }
  }

  final case class Updater[F[_]: Monad, M: ModuleKey, A](inner: M => F[(M, A)])
      extends Impl[F, EmptyEnvironment, NothingProvided, M, A] {
    override def widen[B >: A]: Impl[F, EmptyEnvironment, NothingProvided, M, B] =
      copy[F, M, B](inner = inner(_).widen[(M, B)])

    override def evalMap[B](f: A => F[B]): Impl[F, EmptyEnvironment, NothingProvided, M, B] =
      copy[F, M, B](inner = inner(_).flatMap { case (module, a) =>
        f(a).map(module -> _)
      })

    override protected[hierarchy] def collect(
      requirements: EmptyEnvironment,
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, A)] =
      inner(collector.modules(ModuleKey[M]).asInstanceOf[M]).map { case (m, a) => collector.addModule[M](m) -> a }

  }

  final case class FlatMap[
    F[_]: Monad,
    SelfEnvironment,
    SelfProvided,
    SelfRequirements,
    NextEnvironment,
    NextProvided,
    NextRequirements,
    A,
    NextA
  ](
    self: Impl[F, SelfEnvironment, SelfProvided, SelfRequirements, A],
    next: A => F[Hierarchy[F, NextEnvironment, NextProvided, NextRequirements, NextA]]
  ) extends Impl[
        F,
        SelfEnvironment with NextEnvironment,
        SelfProvided with NextProvided,
        SelfRequirements with NextRequirements,
        NextA
      ] {
    override def widen[B >: NextA]: Impl[
      F,
      SelfEnvironment with NextEnvironment,
      SelfProvided with NextProvided,
      SelfRequirements with NextRequirements,
      B
    ] =
      copy[F, SelfEnvironment, SelfProvided, SelfRequirements, NextEnvironment, NextProvided, NextRequirements, A, B](
        next = next(_).map(_.widen[B])
      )

    override def evalMap[B](
      f: NextA => F[B]
    ): Impl[
      F,
      SelfEnvironment with NextEnvironment,
      SelfProvided with NextProvided,
      SelfRequirements with NextRequirements,
      B
    ] =
      copy[F, SelfEnvironment, SelfProvided, SelfRequirements, NextEnvironment, NextProvided, NextRequirements, A, B](
        next = next(_).map(_.evalMap(f))
      )

    override protected[hierarchy] def collect(
      requirements: SelfEnvironment with NextEnvironment,
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, NextA)] =
      self
        .collect(requirements, collector)
        .flatMap { case (collector, a) =>
          next(a).flatMap(
            _.asInstanceOf[Impl[F, NextEnvironment, NextProvided, NextRequirements, NextA]]
              .collect(requirements, collector)
          )
        }
  }

  final case class HierarchyModuleCollector(
    modules: ModuleMap,
    order: List[ModuleKey[_]]
  ) extends Product {
    def addModule[ModuleT: ModuleKey](
      module: ModuleT
    ): HierarchyModuleCollector =
      HierarchyModuleCollector(
        modules = modules + (ModuleKey[ModuleT] -> module),
        order =
          if (order.contains(ModuleKey[ModuleT])) order // Swap of module
          else ModuleKey[ModuleT] :: order
      )

    def toCollection[F[_]: Sync]: HierarchyModuleCollection[F, Nothing] =
      HierarchyModuleCollection(modules.pure[F], order)
  }

  final case class HierarchyModuleCollection[F[_]: Sync, Provided](
    modules: F[ModuleMap],
    order: List[ModuleKey[_]]
  ) extends Product {

    private[hierarchy] val beforeRun: F[ModuleMap] =
      logger[F].flatMap { implicit logger =>
        def beforeRun(updatedModules: ModuleMap, remaining: List[ModuleKey[_]]): F[ModuleMap] = remaining match {
          case Nil       => updatedModules.pure[F]
          case h :: tail =>
            updatedModules(h) match {
              case module: ModifierHierarchyModule[F] @unchecked =>
                Logger[F].trace(s"Module ${h.id} is a modifier, applying itself") *>
                  module
                    .beforeRun(copy(modules = updatedModules.pure[F]))
                    .flatMap(_.modules.flatMap(beforeRun(_, tail)))
              case _                                             =>
                Logger[F].trace(s"Module ${h.id} is not a modifier, doing nothing") *>
                  beforeRun(updatedModules, tail)
            }
        }

        Logger[F].debug(s"Before run in order: ${order.map(_.id).mkString(" -> ")}") *>
          modules.flatMap(beforeRun(_, order))
      }

    def update[ModuleT: ModuleKey](modifyFn: ModuleT => ModuleT)(implicit
      hasModule: Provided <:< ModuleT
    ): HierarchyModuleCollection[F, Provided] =
      updateF[ModuleT](modifyFn(_).pure[F])

    def updateF[ModuleT: ModuleKey](
      modifyFn: ModuleT => F[ModuleT]
    )(implicit hasModule: Provided <:< ModuleT): HierarchyModuleCollection[F, Provided] =
      copy[F, Provided](modules = modules.flatMap { modules =>
        val oldModule = modules(ModuleKey[ModuleT]).asInstanceOf[ModuleT]

        modifyFn(oldModule).map(modules.updated(ModuleKey[ModuleT], _))
      })

    def useF[ModuleT: ModuleKey](useF: ModuleT => F[Unit])(implicit
      hasModule: Provided <:< ModuleT
    ): HierarchyModuleCollection[F, Provided] =
      updateF[ModuleT](module => useF(module).as(module))
  }
}
