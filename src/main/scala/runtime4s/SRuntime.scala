package fr.valentinhenry
package runtime4s

import runtime4s.Runtime.{EmptyEnvironment, EmptyProvided}
import runtime4s.capabilities._

import cats._
import cats.effect.Sync
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.annotation.implicitNotFound
import scala.annotation.unchecked.uncheckedVariance

sealed trait Runtime[F[_], -Environment, -Provided, -Requirements, +A] {
  def run(e: Environment)(implicit
    @implicitNotFound("A runtime cannot be run unless all requirements are fulfilled")
    isRunnable: Provided @uncheckedVariance <:< Requirements,
    P: Parallel[F],
    S: Sync[F]
  ): F[Unit]

  def map[B](f: A => B): Runtime[F, Environment, Provided, Requirements, B]

  def as[B](b: => B): Runtime[F, Environment, Provided, Requirements, B] = map(_ => b)

  def flatMap[E, P, R, B](
    f: A => Runtime[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Runtime[F, Environment with E, Provided with P, Requirements with R, B]

  def flatTap[E, P, R, B](
    f: A => Runtime[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Runtime[F, Environment with E, Provided with P, Requirements with R, A]

  def flatMapF[E, P, R, B](
    f: A => F[Runtime[F, E, P, R, B]]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Runtime[F, Environment with E, Provided with P, Requirements with R, B]

  def flatTapF[E, P, R, B](
    f: A => F[Runtime[F, E, P, R, B]]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Runtime[F, Environment with E, Provided with P, Requirements with R, A]

  def evalMap[B](f: A => F[B]): Runtime[F, Environment, Provided, Requirements, B]
  def evalTap[B](f: A => F[B]): Runtime[F, Environment, Provided, Requirements, A]

  def product[E, P, R, B](
    r: Runtime[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Runtime[F, Environment with E, Provided with P, Requirements with R, (A, B)]

  def product_[E, P, R, B](
    r: Runtime[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Runtime[F, Environment with E, Provided with P, Requirements with R, Unit]

  def productL[E, P, R, B](
    r: Runtime[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Runtime[F, Environment with E, Provided with P, Requirements with R, A]

  def productR[E, P, R, B](
    r: Runtime[F, E, P, R, B]
  )(implicit
    ev: NoPostProvidedModules[Provided, Requirements, P] @uncheckedVariance
  ): Runtime[F, Environment with E, Provided with P, Requirements with R, B]

  def provide[Module: RuntimeKey](module: Module)(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Runtime[F, Environment, Module with Provided, R.Requirements with Requirements, Module]

  def provideF[Module: RuntimeKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Runtime[F, Environment, Module with Provided, R.Requirements with Requirements, Module]

  def provide_[Module: RuntimeKey](module: Module)(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Runtime[F, Environment, Module with Provided, R.Requirements with Requirements, Unit]

  def provideF_[Module: RuntimeKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Runtime[F, Environment, Module with Provided, R.Requirements with Requirements, Unit]

  def provideTap[Module: RuntimeKey](module: Module)(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Runtime[F, Environment, Module with Provided, R.Requirements with Requirements, A]

  def provideFTap[Module: RuntimeKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module],
    ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
  ): Runtime[F, Environment, Module with Provided, R.Requirements with Requirements, A]

  def swap[Module: RuntimeKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Runtime[F, Environment, Provided, R.Requirements with Requirements with Module, Module]

  def swapF[Module: RuntimeKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module]
  ): Runtime[F, Environment, Provided, R.Requirements with Requirements with Module, Module]

  def swap_[Module: RuntimeKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Runtime[F, Environment, Provided, R.Requirements with Requirements with Module, Unit]

  def swapF_[Module: RuntimeKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module]
  ): Runtime[F, Environment, Provided, R.Requirements with Requirements with Module, Unit]

  def swapTap[Module: RuntimeKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Runtime[F, Environment, Provided, R.Requirements with Requirements with Module, A]

  def swapFTap[Module: RuntimeKey](module: F[Module])(implicit
    R: RequirementExtractor[F, Module]
  ): Runtime[F, Environment, Provided, R.Requirements with Requirements with Module, A]

  def narrow[P >: Provided @uncheckedVariance]: Runtime[F, Environment, P, Requirements, A]

  def widen[B >: A]: Runtime[F, Environment, Provided, Requirements, B]
}

object Runtime {

  type REPR[F[_], -E, -P, R] = Runtime[F, E, P, R, Unit]
  type REP[F[_], -E, -P]     = REPR[F, E, P, EmptyRequirement]
  type RE[F[_], -E]          = REP[F, E, EmptyProvided]
  type RA[F[_], A]           = Runtime[F, EmptyEnvironment, EmptyProvided, EmptyRequirement, A]

  type EmptyEnvironment = Any
  type EmptyProvided    = Any
  type EmptyRequirement = Any

  def empty[F[_]: Monad]: RA[F, Unit] =
    RuntimeImpls.Consumer[F, EmptyEnvironment, EmptyProvided, EmptyRequirement, Unit, Unit](
      inner = (_, _) => ().pure[F],
      extractProvided = _ => ()
    )

  // Gets an environment value
  def get[F[_]: Monad, E]: Runtime[F, E, EmptyProvided, EmptyRequirement, E] =
    RuntimeImpls.Consumer[F, E, EmptyProvided, EmptyRequirement, Unit, E](
      inner = (env, _) => env.pure[F],
      extractProvided = _ => ()
    )

  // Summon a provided module
  def summon[F[_]: Monad, M: RuntimeKey]: Runtime[F, EmptyEnvironment, EmptyProvided, M, M] =
    RuntimeImpls.Consumer[F, EmptyEnvironment, EmptyProvided, M, M, M](
      inner = (_, requirements) => requirements.pure[F],
      extractProvided = _(RuntimeKey[M]).asInstanceOf[M]
    )

  def provide[F[_]: Monad, Module: RuntimeKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Runtime[F, EmptyEnvironment, Module, R.Requirements, Module] =
    RuntimeImpls.Producer[F, Module, R.Requirements, Module, Module](
      inner = (module, module).pure[F]
    )

  def provide_[F[_]: Monad, Module: RuntimeKey](module: Module)(implicit
    R: RequirementExtractor[F, Module]
  ): Runtime[F, EmptyEnvironment, Module, R.Requirements, Unit] =
    RuntimeImpls.Producer[F, Module, R.Requirements, Module, Unit](
      inner = (module, ()).pure[F]
    )

  def swap[F[_]: Monad, Module: RuntimeKey](newModule: Module)(implicit
    R: RequirementExtractor[F, Module] // TODO: Find a way to remove old module requirements //FIXME maybe remove ?
  ): Runtime[F, EmptyEnvironment, EmptyProvided, Module with R.Requirements, Module] =
    RuntimeImpls.Producer[F, EmptyProvided, Module with R.Requirements, Module, Module](
      inner = (newModule, newModule).pure[F]
    )

  def swap_[F[_]: Monad, Module: RuntimeKey](newModule: Module)(implicit
    R: RequirementExtractor[F, Module] // TODO: Find a way to remove old module requirements //FIXME maybe remove ?
  ): Runtime[F, EmptyEnvironment, EmptyProvided, Module with R.Requirements, Unit] =
    RuntimeImpls.Producer[F, EmptyProvided, Module with R.Requirements, Module, Unit](
      inner = (newModule, ()).pure[F]
    )

  def pure[F[_]: Monad, A](a: A): RA[F, A] =
    RuntimeImpls.Consumer[F, EmptyEnvironment, EmptyProvided, EmptyRequirement, Unit, A](
      inner = (_, _) => Monad[F].pure(a),
      extractProvided = _ => ()
    )

  def delay[F[_]: Sync, A](a: => A): RA[F, A] =
    RuntimeImpls.Consumer[F, EmptyEnvironment, EmptyProvided, EmptyRequirement, Unit, A](
      inner = (_, _) => Sync[F].delay(a),
      extractProvided = _ => ()
    )

  def liftF[F[_]: Monad, A](fa: F[A]): RA[F, A] =
    RuntimeImpls.Consumer[F, EmptyEnvironment, EmptyProvided, EmptyRequirement, Unit, A](
      inner = (_, _) => fa,
      extractProvided = _ => ()
    )

  def dsl[F[_]: Monad]: RuntimeFDSL[F] = new RuntimeFDSL[F] {}

  abstract class RuntimeFDSL[F[_]: Monad] {
    def empty: RA[F, Unit] = Runtime.empty[F]

    def get[E]: Runtime[F, E, EmptyProvided, EmptyRequirement, E] = Runtime.get[F, E]

    def summon[M: RuntimeKey]: Runtime[F, EmptyEnvironment, EmptyProvided, M, M] = Runtime.summon[F, M]

    def provide[A: RuntimeKey](a: A)(implicit
      R: RequirementExtractor[F, A]
    ): Runtime[F, EmptyEnvironment, A, R.Requirements, A] =
      Runtime.provide[F, A](a)

    def provide_[A: RuntimeKey](a: A)(implicit
      R: RequirementExtractor[F, A]
    ): Runtime[F, EmptyEnvironment, A, R.Requirements, Unit] =
      Runtime.provide_[F, A](a)

    def swap[Module: RuntimeKey](newModule: Module)(implicit
      R: RequirementExtractor[F, Module] // TODO: Find a way to remove old module requirements
    ): Runtime[F, EmptyEnvironment, EmptyProvided, Module with R.Requirements, Module] =
      Runtime.swap[F, Module](newModule)

    def swap_[Module: RuntimeKey](newModule: Module)(implicit
      R: RequirementExtractor[F, Module] // TODO: Find a way to remove old module requirements
    ): Runtime[F, EmptyEnvironment, EmptyProvided, Module with R.Requirements, Unit] =
      Runtime.swap_[F, Module](newModule)

    def delay[A](a: => A)(implicit S: Sync[F]): RA[F, A] = Runtime.delay[F, A](a)

    def liftF[A](fa: F[A]): RA[F, A] = Runtime.liftF[F, A](fa)
  }
}

private[runtime4s] object RuntimeImpls {
  final val loggerName = "runtime4s.Runtime"

  type ModuleMap = Map[RuntimeKey[_], Any]

  def logger[F[_]: Sync]: F[Logger[F]] = Sync[F].delay(Slf4jLogger.getLoggerFromName[F](loggerName))

  sealed abstract class Impl[F[_]: Monad, Environment, Provided, Requirements, A]
      extends Runtime[F, Environment, Provided, Requirements, A]
      with Product {
    override final def run(e: Environment)(implicit
      @implicitNotFound("A runtime cannot be run unless all requirements are fulfilled")
      isRunnable: Provided <:< Requirements,
      P: Parallel[F],
      S: Sync[F]
    ): F[Unit] =
      logger[F].flatMap { implicit logger =>
        def printRuntime(modules: ModuleMap): F[Unit] = {
          val modifiers: List[String] = modules.values.toList.collect { case m: ModifierRuntimeModule[F] @unchecked =>
            m.getClass.getSimpleName
          }
          val runners: List[String]   = modules.values.toList.collect { case r: RunnableRuntimeModule[F] @unchecked =>
            r.getClass.getSimpleName
          }

          Logger[F].info(
            s"""
               |==============================================================================
               |d8888b. db    db d8b   db d888888b d888888b .88b  d88. d88888b   j88D  .d8888.
               |88  `8D 88    88 888o  88 `~~88~~'   `88'   88'YbdP`88 88'      j8~88  88'  YP
               |88oobY' 88    88 88V8o 88    88       88    88  88  88 88ooooo j8' 88  `8bo.
               |88`8b   88    88 88 V8o88    88       88    88  88  88 88~~~~~ V88888D   `Y8b.
               |88 `88. 88b  d88 88  V888    88      .88.   88  88  88 88.         88  db   8D
               |88   YD ~Y8888P' VP   V8P    YP    Y888888P YP  YP  YP Y88888P     VP  `8888Y'
               |==============================================================================
               |Runtime modified by:
               |${modifiers.mkString(" - ", "\n - ", "")}
               |Registered modules to be run:
               |${runners.mkString(" - ", "\n - ", "")}
               |""".stripMargin
          )
        }

        def run(modules: ModuleMap): F[Unit] =
          modules.values.toList.collect { case m: RunnableRuntimeModule[F] @unchecked => m }
            .map(_.run)
            .parSequence_

        for {
          _         <- Logger[F].debug("Collecting modules")
          collected <- collect(e, RuntimeModuleCollector(Map.empty[RuntimeKey[_], Any], Nil))
          _         <- Logger[F].debug(s"Running before run on ${collected._1.modules.size} modules")
          modules   <- collected._1.toCollection[F].beforeRun
          _         <- printRuntime(modules)
          _         <- run(modules)
        } yield ()
      }

    protected[runtime4s] def collect(
      requirements: Environment,
      collector: RuntimeModuleCollector
    ): F[(RuntimeModuleCollector, A)]

    def narrow[P >: Provided]: Runtime[F, Environment, P, Requirements, A] =
      this.asInstanceOf[Runtime[F, Environment, P, Requirements, A]]

    def widen[B >: A]: Impl[F, Environment, Provided, Requirements, B]

    override final def as[B](b: => B): Runtime[F, Environment, Provided, Requirements, B] =
      map(_ => b)

    override def map[B](f: A => B): Runtime[F, Environment, Provided, Requirements, B] =
      evalMap(f(_).pure[F])

    override def evalMap[B](f: A => F[B]): Impl[F, Environment, Provided, Requirements, B]

    override def flatMap[E, P, R, B](
      f: A => Runtime[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Runtime[F, Environment with E, Provided with P, Requirements with R, B] =
      flatMapF[E, P, R, B](f(_).pure[F])

    override def flatTap[E, P, R, B](f: A => Runtime[F, E, P, R, B])(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Runtime[F, Environment with E, Provided with P, Requirements with R, A] =
      flatMap(a => f(a).as(a))

    override def flatMapF[E, P, R, B](
      f: A => F[Runtime[F, E, P, R, B]]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Runtime[F, Environment with E, Provided with P, Requirements with R, B] =
      FlatMap[F, Environment, Provided, Requirements, E, P, R, A, B](this, f)

    override def flatTapF[E, P, R, B](f: A => F[Runtime[F, E, P, R, B]])(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Runtime[F, Environment with E, Provided with P, Requirements with R, A] =
      flatMapF(a => f(a).map(_.as(a)))

    override final def product[E, P, R, B](
      r: Runtime[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Runtime[F, Environment with E, Provided with P, Requirements with R, (A, B)] =
      flatMap[E, P, R, (A, B)](v => r.map(v -> _))

    override final def product_[E, P, R, B](
      r: Runtime[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Runtime[F, Environment with E, Provided with P, Requirements with R, Unit] =
      flatMap[E, P, R, B](_ => r).as(())

    override final def productL[E, P, R, B](
      r: Runtime[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Runtime[F, Environment with E, Provided with P, Requirements with R, A] =
      flatMap[E, P, R, A](a => r.as(a))

    override final def productR[E, P, R, B](
      r: Runtime[F, E, P, R, B]
    )(implicit
      ev: NoPostProvidedModules[Provided, Requirements, P]
    ): Runtime[F, Environment with E, Provided with P, Requirements with R, B] =
      flatMap[E, P, R, B](_ => r)

    override def evalTap[B](f: A => F[B]): Runtime[F, Environment, Provided, Requirements, A] =
      evalMap(a => f(a).as(a))

    override def provide[Module: RuntimeKey](module: Module)(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Runtime[F, Environment, Provided with Module, Requirements with R.Requirements, Module] =
      productR(Runtime.provide[F, Module](module))

    override def provideF[Module: RuntimeKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Runtime[F, Environment, Provided with Module, Requirements with R.Requirements, Module] =
      flatMapF(_ => module.map(Runtime.provide[F, Module](_)))

    override def provide_[Module: RuntimeKey](module: Module)(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Runtime[F, Environment, Provided with Module, Requirements with R.Requirements, Unit] =
      product_(Runtime.provide[F, Module](module))

    override def provideF_[Module: RuntimeKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Runtime[F, Environment, Provided with Module, Requirements with R.Requirements, Unit] =
      flatMapF(_ => module.map(Runtime.provide[F, Module](_))).as(())

    override def provideTap[Module: RuntimeKey](module: Module)(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Runtime[F, Environment, Provided with Module, Requirements with R.Requirements, A] =
      productL(Runtime.provide[F, Module](module))

    override def provideFTap[Module: RuntimeKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module],
      ev: NoPostProvidedModules[Provided, Requirements, Module] @uncheckedVariance
    ): Runtime[F, Environment, Provided with Module, Requirements with R.Requirements, A] =
      flatTapF(_ => module.map(Runtime.provide[F, Module](_)))

    override def swap[Module: RuntimeKey](module: Module)(implicit
      R: RequirementExtractor[F, Module]
    ): Runtime[F, Environment, Provided, Requirements with Module with R.Requirements, Module] =
      productR(Runtime.swap[F, Module](module))

    override def swapF[Module: RuntimeKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module]
    ): Runtime[F, Environment, Provided, Requirements with Module with R.Requirements, Module] =
      flatMapF(_ => module.map(Runtime.swap[F, Module](_)))

    override def swap_[Module: RuntimeKey](module: Module)(implicit
      R: RequirementExtractor[F, Module]
    ): Runtime[F, Environment, Provided, Requirements with Module with R.Requirements, Unit] =
      product_(Runtime.swap[F, Module](module))

    override def swapF_[Module: RuntimeKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module]
    ): Runtime[F, Environment, Provided, Requirements with Module with R.Requirements, Unit] =
      flatMapF(_ => module.map(Runtime.swap[F, Module](_))).as(())

    override def swapTap[Module: RuntimeKey](module: Module)(implicit
      R: RequirementExtractor[F, Module]
    ): Runtime[F, Environment, Provided, Requirements with Module with R.Requirements, A] =
      productL(Runtime.swap[F, Module](module))

    override def swapFTap[Module: RuntimeKey](module: F[Module])(implicit
      R: RequirementExtractor[F, Module]
    ): Runtime[F, Environment, Provided, Requirements with Module with R.Requirements, A] =
      flatTapF(_ => module.map(Runtime.swap[F, Module](_)))
  }

  final case class Consumer[F[_]: Monad, Environment, Provided, Requirements, UsedRequirements, A](
    inner: (Environment, UsedRequirements) => F[A],
    extractProvided: ModuleMap => UsedRequirements
  ) extends Impl[F, Environment, Provided, Requirements, A] {

    override def widen[B >: A]: Impl[F, Environment, Provided, Requirements, B] =
      copy[F, Environment, Provided, Requirements, UsedRequirements, B](inner = inner(_, _).widen[B])

    override def evalMap[B](f: A => F[B]): Impl[F, Environment, Provided, Requirements, B] =
      copy[F, Environment, Provided, Requirements, UsedRequirements, B](inner = inner(_, _).flatMap(f))

    override protected[runtime4s] def collect(
      environment: Environment,
      collector: RuntimeModuleCollector
    ): F[(RuntimeModuleCollector, A)] =
      inner(environment, extractProvided(collector.modules)).map(collector -> _)
  }

  final case class Producer[F[_]: Monad, Provided, Requirements, M: RuntimeKey, A](inner: F[(M, A)])
      extends Impl[F, EmptyEnvironment, Provided, Requirements, A] {

    override def widen[B >: A]: Impl[F, EmptyEnvironment, Provided, Requirements, B] =
      copy[F, Provided, Requirements, M, B](inner = inner.widen[(M, B)])

    override def evalMap[B](f: A => F[B]): Impl[F, EmptyEnvironment, Provided, Requirements, B] =
      copy[F, Provided, Requirements, M, B](inner = inner.flatMap { case (module, a) =>
        f(a).map(module -> _)
      })

    override protected[runtime4s] def collect(
      requirements: EmptyEnvironment,
      collector: RuntimeModuleCollector
    ): F[(RuntimeModuleCollector, A)] =
      inner.map { case (module, a) => collector.addModule[M](module) -> a }
  }

  final case class Updater[F[_]: Monad, M: RuntimeKey, A](inner: M => F[(M, A)])
      extends Impl[F, EmptyEnvironment, EmptyProvided, M, A] {
    override def widen[B >: A]: Impl[F, EmptyEnvironment, EmptyProvided, M, B] =
      copy[F, M, B](inner = inner(_).widen[(M, B)])

    override def evalMap[B](f: A => F[B]): Impl[F, EmptyEnvironment, EmptyProvided, M, B] =
      copy[F, M, B](inner = inner(_).flatMap { case (module, a) =>
        f(a).map(module -> _)
      })

    override protected[runtime4s] def collect(
      requirements: EmptyEnvironment,
      collector: RuntimeModuleCollector
    ): F[(RuntimeModuleCollector, A)] =
      inner(collector.modules(RuntimeKey[M]).asInstanceOf[M]).map { case (m, a) => collector.addModule[M](m) -> a }

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
    next: A => F[Runtime[F, NextEnvironment, NextProvided, NextRequirements, NextA]]
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

    override protected[runtime4s] def collect(
      requirements: SelfEnvironment with NextEnvironment,
      collector: RuntimeModuleCollector
    ): F[(RuntimeModuleCollector, NextA)] =
      self
        .collect(requirements, collector)
        .flatMap { case (collector, a) =>
          next(a).flatMap(
            _.asInstanceOf[Impl[F, NextEnvironment, NextProvided, NextRequirements, NextA]]
              .collect(requirements, collector)
          )
        }
  }

  final case class RuntimeModuleCollector(
    modules: ModuleMap,
    order: List[RuntimeKey[_]]
  ) extends Product {
    def addModule[ModuleT: RuntimeKey](
      module: ModuleT
    ): RuntimeModuleCollector =
      RuntimeModuleCollector(
        modules = modules + (RuntimeKey[ModuleT] -> module),
        order =
          if (order.contains(RuntimeKey[ModuleT])) order // Swap of module
          else RuntimeKey[ModuleT] :: order
      )

    def toCollection[F[_]: Sync]: RuntimeModuleCollection[F, Nothing] = RuntimeModuleCollection(modules.pure[F], order)
  }

  final case class RuntimeModuleCollection[F[_]: Sync, Provided](
    modules: F[ModuleMap],
    order: List[RuntimeKey[_]]
  ) extends Product {

    private[runtime4s] val beforeRun: F[ModuleMap] =
      logger[F].flatMap { implicit logger =>
        def beforeRun(updatedModules: ModuleMap, remaining: List[RuntimeKey[_]]): F[ModuleMap] = remaining match {
          case Nil       => updatedModules.pure[F]
          case h :: tail =>
            updatedModules(h) match {
              case module: ModifierRuntimeModule[F] @unchecked =>
                Logger[F].trace(s"Module ${h.id} is a modifier, applying itself") *>
                  module
                    .beforeRun(copy(modules = updatedModules.pure[F]))
                    .flatMap(_.modules.flatMap(beforeRun(_, tail)))
              case _                                           =>
                Logger[F].trace(s"Module ${h.id} is not a modifier, doing nothing") *>
                  beforeRun(updatedModules, tail)
            }
        }

        Logger[F].debug(s"Before run in order: ${order.map(_.id).mkString(" -> ")}") *>
          modules.flatMap(beforeRun(_, order))
      }

    def update[ModuleT: RuntimeKey](modifyFn: ModuleT => ModuleT)(implicit
      hasModule: Provided <:< ModuleT
    ): RuntimeModuleCollection[F, Provided] =
      updateF[ModuleT](modifyFn(_).pure[F])

    def updateF[ModuleT: RuntimeKey](
      modifyFn: ModuleT => F[ModuleT]
    )(implicit hasModule: Provided <:< ModuleT): RuntimeModuleCollection[F, Provided] =
      copy[F, Provided](modules = modules.flatMap { modules =>
        val oldModule = modules(RuntimeKey[ModuleT]).asInstanceOf[ModuleT]

        modifyFn(oldModule).map(modules.updated(RuntimeKey[ModuleT], _))
      })

    def useF[ModuleT: RuntimeKey](useF: ModuleT => F[Unit])(implicit
      hasModule: Provided <:< ModuleT
    ): RuntimeModuleCollection[F, Provided] =
      updateF[ModuleT](module => useF(module).as(module))
  }
}
