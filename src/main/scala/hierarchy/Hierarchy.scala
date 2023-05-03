package fr.valentinhenry
package hierarchy

import hierarchy.requirements._

import cats._
import cats.effect.Sync
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import shapeless.=:!=

import scala.annotation.{implicitNotFound, unused}

sealed trait Hierarchy[F[_], Environment, Provided, Requirements, +A] {
  def runner(implicit
    @implicitNotFound("A runtime cannot be run unless all requirements are fulfilled")
    isRunnable: HasRequirements[Provided, Requirements]
  ): HierarchyRunner[F, Environment]

  def map[B](f: A => B): Hierarchy[F, Environment, Provided, Requirements, B]

  def as[B](b: => B): Hierarchy[F, Environment, Provided, Requirements, B]

  def <*[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, A]

  def *>[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B]

  def >>[E, P, R, B](
    f: A => Hierarchy[F, E, P, R, B]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B]

  def flatMap[E, P, R, B](
    f: A => Hierarchy[F, E, P, R, B]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B]

  def flatTap[E, P, R, B](
    f: A => Hierarchy[F, E, P, R, B]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, A]

  def flatMapF[E, P, R, B](
    f: A => F[Hierarchy[F, E, P, R, B]]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B]

  def flatTapF[E, P, R, B](
    f: A => F[Hierarchy[F, E, P, R, B]]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, A]

  def evalMap[B](f: A => F[B]): Hierarchy[F, Environment, Provided, Requirements, B]
  def evalTap[B](f: A => F[B]): Hierarchy[F, Environment, Provided, Requirements, A]

  def product[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, (A, B)]

  def product_[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, Unit]

  def productL[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, A]

  def productR[E, P, R, B](
    r: Hierarchy[F, E, P, R, B]
  )(implicit
    evNoReProvided: NoReProvidedModules[Provided, P],
    evNoPostProvided: NoPostProvidedModules[Requirements, P],
    envMerger: Merger[Environment, E],
    providedMerger: Merger[Provided, P],
    requirementMerger: Merger[Requirements, R]
  ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B]

//  def provide[Module: ModuleKey](module: Module)(implicit
//    R: RequirementExtractor[F, Module],
//    evNoReProvided: NoReProvidedModules[Provided, Module],
//    evNoPostProvided: NoPostProvidedModules[Requirements, Module],
//    providedMerger: Merger[Provided, Module],
//    requirementMerger: Merger[Requirements, R.Requirements]
//  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, Module] =
//    __provide[Module, R.Requirements](module)
//
//  def provideF[Module: ModuleKey](module: F[Module])(implicit
//    R: RequirementExtractor[F, Module],
//    evNoReProvided: NoReProvidedModules[Provided, Module],
//    evNoPostProvided: NoPostProvidedModules[Requirements, Module]
//  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, Module]
//
//  def provide_[Module: ModuleKey](module: Module)(implicit
//    R: RequirementExtractor[F, Module],
//    evNoReProvided: NoReProvidedModules[Provided, Module],
//    evNoPostProvided: NoPostProvidedModules[Requirements, Module]
//  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, Unit]
//
//  def provideF_[Module: ModuleKey](module: F[Module])(implicit
//    R: RequirementExtractor[F, Module],
//    evNoReProvided: NoReProvidedModules[Provided, Module],
//    evNoPostProvided: NoPostProvidedModules[Requirements, Module]
//  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, Unit]
//
//  def provideTap[Module: ModuleKey](module: Module)(implicit
//    R: RequirementExtractor[F, Module],
//    evNoReProvided: NoReProvidedModules[Provided, Module],
//    evNoPostProvided: NoPostProvidedModules[Requirements, Module]
//  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, A]
//
//  def provideFTap[Module: ModuleKey](module: F[Module])(implicit
//    R: RequirementExtractor[F, Module],
//    evNoReProvided: NoReProvidedModules[Provided, Module],
//    evNoPostProvided: NoPostProvidedModules[Requirements, Module]
//  ): Hierarchy[F, Environment, Module with Provided, R.Requirements with Requirements, A]

  def swap[ModuleT: Key](module: ModuleT)(implicit
    requirementMerger: Merger[Requirements, ModuleT]
  ): Hierarchy[F, Environment, Provided, requirementMerger.Out, ModuleT]

  def swapF[ModuleT: Key](module: F[ModuleT])(implicit
    requirementMerger: Merger[Requirements, ModuleT]
  ): Hierarchy[F, Environment, Provided, requirementMerger.Out, ModuleT]

  def swap_[ModuleT: Key](module: ModuleT)(implicit
    requirementMerger: Merger[Requirements, ModuleT]
  ): Hierarchy[F, Environment, Provided, requirementMerger.Out, Unit]

  def swapF_[ModuleT: Key](module: F[ModuleT])(implicit
    requirementMerger: Merger[Requirements, ModuleT]
  ): Hierarchy[F, Environment, Provided, requirementMerger.Out, Unit]

  def swapTap[ModuleT: Key](module: ModuleT)(implicit
    requirementMerger: Merger[Requirements, ModuleT]
  ): Hierarchy[F, Environment, Provided, requirementMerger.Out, A]

  def swapFTap[ModuleT: Key](module: F[ModuleT])(implicit
    requirementMerger: Merger[Requirements, ModuleT]
  ): Hierarchy[F, Environment, Provided, requirementMerger.Out, A]

  def narrow[P >: Provided]: Hierarchy[F, Environment, P, Requirements, A] = // FIXME bad
    this.asInstanceOf[Hierarchy[F, Environment, P, Requirements, A]]

  def widen[B >: A]: Hierarchy[F, Environment, Provided, Requirements, B]

//  protected[this] def __provide[Module: ModuleKey, MReq](module: Module)(implicit
//    R: RequirementExtractor.Aux[F, Module, MReq],
//    evNoReprovided: NoReProvidedModules[Provided, Module],
//    evNoPostProvidedModule: NoPostProvidedModules[Requirements, Module],
//    providedMerger: Merger[Provided, Module],
//    requirementMerger: Merger[Requirements, MReq]
//  ): Hierarchy[F, Environment, providedMerger.Out, requirementMerger.Out, Module]
//
//  protected[this] def __provideF[Module: ModuleKey, MReq](module: F[Module])(implicit
//    R: RequirementExtractor.Aux[F, Module, MReq],
//    evNoReprovided: NoReProvidedModules[Provided, Module],
//    evNoPostProvidedModule: NoPostProvidedModules[Requirements, Module],
//    providedMerger: Merger[Provided, Module],
//    requirementMerger: Merger[Requirements, MReq]
//  ): Hierarchy[F, Environment, providedMerger.Out, requirementMerger.Out, Module]
}

object Hierarchy {
  type HEPR[F[_], E, P, R] = Hierarchy[F, E, P, R, Unit]
  type HEP[F[_], E, P]     = Hierarchy[F, E, P, Any, Unit]
  type HE[F[_], E]         = Hierarchy[F, E, Any, Any, Unit]
  type HA[F[_], A]         = Hierarchy[F, Any, Any, Any, A]

  def empty[F[_]: Monad]: HA[F, Unit] =
    HierarchyImpls.Value[F, Unit](
      inner = Applicative[F].unit
    )

  // Gets an environment value
  def get[F[_]: Monad, E: Key]: Hierarchy[F, E, Any, Any, E] =
    HierarchyImpls.EnvironmentConsumer[F, E, E](
      inner = env => env.pure[F]
    )

  // Summon a provided module
  def summon[F[_]: Monad, M: Key]: Hierarchy[F, Any, Any, M, M] =
    HierarchyImpls.ModuleConsumer[F, M, M](
      inner = requirements => requirements.pure[F]
    )

  def provide[F[_], ModuleT](module: ModuleT)(implicit
    M: Monad[F],
    MK: Key[ModuleT],
    R: RequirementExtractor[F, ModuleT]
  ): Hierarchy[F, Any, ModuleT, R.Requirements, ModuleT] =
    HierarchyImpls.Producer[F, ModuleT, R.Requirements, ModuleT, ModuleT](
      inner = (module, module).pure[F]
    )

  def provide_[F[_], ModuleT](module: ModuleT)(implicit
    M: Monad[F],
    MK: Key[ModuleT],
    R: RequirementExtractor[F, ModuleT]
  ): Hierarchy[F, Any, ModuleT, R.Requirements, Unit] =
    HierarchyImpls.Producer[F, ModuleT, R.Requirements, ModuleT, Unit](
      inner = (module, ()).pure[F]
    )

  def swap[F[_]: Monad, ModuleT: Key](newModule: ModuleT): Hierarchy[F, Any, Any, ModuleT, ModuleT] =
    HierarchyImpls.Producer[F, Any, ModuleT, ModuleT, ModuleT](
      inner = (newModule, newModule).pure[F]
    )

  def swap_[F[_]: Monad, ModuleT: Key](newModule: ModuleT): Hierarchy[F, Any, Any, ModuleT, Unit] =
    HierarchyImpls.Producer[F, Any, ModuleT, ModuleT, Unit](
      inner = (newModule, ()).pure[F]
    )

  def update[F[_]: Monad, ModuleT: Key](
    update: ModuleT => ModuleT
  ): Hierarchy[F, Any, Any, ModuleT, ModuleT] =
    HierarchyImpls.Updater[F, ModuleT, ModuleT](
      inner = oldModule => Monad[F].pure { val newModule = update(oldModule); (newModule, newModule) }
    )

  def pure[F[_]: Monad, A](a: A): HA[F, A] =
    HierarchyImpls.Value[F, A](inner = a.pure[F])

  def delay[F[_]: Sync, A](a: => A): HA[F, A] =
    HierarchyImpls.Value[F, A](inner = Sync[F].delay(a))

  def liftF[F[_]: Monad, A](fa: F[A]): HA[F, A] =
    HierarchyImpls.Value[F, A](inner = fa)

  def dsl[F[_]: Monad]: HierarchyDSL[F] = new HierarchyDSL[F] {}

  abstract class HierarchyDSL[F[_]: Monad] {
    def empty: HA[F, Unit] = Hierarchy.empty[F]

    def get[E: Key]: Hierarchy[F, E, Any, Any, E] = Hierarchy.get[F, E]

    def summon[M: Key]: Hierarchy[F, Any, Any, M, M] = Hierarchy.summon[F, M]

    def provide[A: Key](a: A)(implicit
      R: RequirementExtractor[F, A]
    ): Hierarchy[F, Any, A, R.Requirements, A] =
      Hierarchy.provide[F, A](a)(Monad[F], Key[A], R)

    def provide_[A: Key](a: A)(implicit
      R: RequirementExtractor[F, A]
    ): Hierarchy[F, Any, A, R.Requirements, Unit] =
      Hierarchy.provide_[F, A](a)(Monad[F], Key[A], R)

    def swap[ModuleT: Key](newModule: ModuleT): Hierarchy[F, Any, Any, ModuleT, ModuleT] =
      Hierarchy.swap[F, ModuleT](newModule)

    def swap_[ModuleT: Key](newModule: ModuleT): Hierarchy[F, Any, Any, ModuleT, Unit] =
      Hierarchy.swap_[F, ModuleT](newModule)

    def delay[A](a: => A)(implicit S: Sync[F]): HA[F, A] = Hierarchy.delay[F, A](a)

    def liftF[A](fa: F[A]): HA[F, A] = Hierarchy.liftF[F, A](fa)
  }
}

private[hierarchy] object HierarchyImpls {
  final val loggerName = "runtime4s.Hierarchy"

  type ModuleMap = Map[Key[_], Any]

  def logger[F[_]: Sync]: F[Logger[F]] = Sync[F].delay(Slf4jLogger.getLoggerFromName[F](loggerName))

  sealed abstract class Impl[F[_]: Monad, Environment, Provided, Requirements, A]
      extends Hierarchy[F, Environment, Provided, Requirements, A]
      with Product {

    override final def runner(implicit
      @implicitNotFound("A hierarchy cannot be run unless all requirements are fulfilled")
      isRunnable: HasRequirements[Provided, Requirements]
    ): HierarchyRunner[F, Environment] =
      new RunnerImpl[Environment](Map.empty)

    private[this] class RunnerImpl[Env](elts: Map[Key[_], Any]) extends HierarchyRunner[F, Env](elts) {
      override private[hierarchy] def withEnv[NEnv](newEnvs: Map[Key[_], Any]): HierarchyRunner[F, NEnv] =
        new RunnerImpl[NEnv](newEnvs)

      override def run(implicit emptyEnv: Env =:= Any, S: Sync[F], P: Parallel[F]): F[Unit] =
        logger[F].flatMap { implicit logger =>
          def printHierarchy(modules: ModuleMap): F[Unit] = {
            val modifiers: List[String] = modules.values.toList.collect {
              case m: ModifierHierarchyModule[F] @unchecked =>
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
            collected <- collect(elts, HierarchyModuleCollector(Map.empty[Key[_], Any], Nil))
            _         <- Logger[F].debug(s"Running before run on ${collected._1.modules.size} modules")
            modules   <- collected._1.toCollection[F].beforeRun
            _         <- printHierarchy(modules)
            _         <- run(modules)
          } yield ()
        }
    }

    protected[hierarchy] def collect(
      requirements: Map[Key[_], Any],
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, A)]

    override def evalMap[B](f: A => F[B]): Impl[F, Environment, Provided, Requirements, B]

    override def map[B](f: A => B): Hierarchy[F, Environment, Provided, Requirements, B] =
      evalMap(f(_).pure[F])

    override def as[B](b: => B): Hierarchy[F, Environment, Provided, Requirements, B] =
      map(_ => b)

    override def <*[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      evNoReProvided: NoReProvidedModules[Provided, P],
      evNoPostProvided: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, A] =
      productL(r)(
        evNoReProvided,
        evNoPostProvided,
        envMerger,
        providedMerger,
        requirementMerger
      )

    override def *>[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      evNoReProvided: NoReProvidedModules[Provided, P],
      evNoPostProvided: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B] =
      productR(r)(evNoReProvided, evNoPostProvided, envMerger, providedMerger, requirementMerger)

    override def >>[E, P, R, B](
      f: A => Hierarchy[F, E, P, R, B]
    )(implicit
      evNoReProvided: NoReProvidedModules[Provided, P],
      evNoPostProvided: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B] =
      flatMap(f)(evNoReProvided, evNoPostProvided, envMerger, providedMerger, requirementMerger)

    override def flatTap[E, P, R, B](
      f: A => Hierarchy[F, E, P, R, B]
    )(implicit
      evNoReProvided: NoReProvidedModules[Provided, P],
      evNoPostProvided: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, A] =
      flatMap(a => f(a).as(a))(evNoReProvided, evNoPostProvided, envMerger, providedMerger, requirementMerger)

    override def flatMap[E, P, R, B](
      f: A => Hierarchy[F, E, P, R, B]
    )(implicit
      evNoReProvided: NoReProvidedModules[Provided, P],
      evNoPostProvided: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B] =
      flatMapF[E, P, R, B](f(_).pure[F])(
        evNoReProvided,
        evNoPostProvided,
        envMerger,
        providedMerger,
        requirementMerger
      )

    override def flatMapF[E, P, R, B](
      f: A => F[Hierarchy[F, E, P, R, B]]
    )(implicit
      evNoReprovided: NoReProvidedModules[Provided, P],
      evNoPostProvidedModule: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B] =
      FlatMap[
        F,
        Environment,
        Provided,
        Requirements,
        E,
        P,
        R,
        envMerger.Out,
        providedMerger.Out,
        requirementMerger.Out,
        A,
        B
      ](this, f)(
        envMerger,
        providedMerger,
        requirementMerger,
        Monad[F]
      )

    override def flatTapF[E, P, R, B](f: A => F[Hierarchy[F, E, P, R, B]])(implicit
      evNoReprovided: NoReProvidedModules[Provided, P],
      evNoPostProvidedModule: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, A] =
      flatMapF(a => f(a).map(_.as(a)))(
        evNoReprovided,
        evNoPostProvidedModule,
        envMerger,
        providedMerger,
        requirementMerger
      )

    override final def product[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      evNoReprovided: NoReProvidedModules[Provided, P],
      evNoPostProvidedModule: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, (A, B)] =
      flatMap[E, P, R, (A, B)](v => r.map(v -> _))(
        evNoReprovided,
        evNoPostProvidedModule,
        envMerger,
        providedMerger,
        requirementMerger
      )

    override final def product_[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      evNoReprovided: NoReProvidedModules[Provided, P],
      evNoPostProvidedModule: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, Unit] =
      flatMap[E, P, R, B](_ => r)(
        evNoReprovided,
        evNoPostProvidedModule,
        envMerger,
        providedMerger,
        requirementMerger
      ).as(())

    override final def productL[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      evNoReprovided: NoReProvidedModules[Provided, P],
      evNoPostProvidedModule: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, A] =
      flatMap[E, P, R, A](a => r.as(a))(
        evNoReprovided,
        evNoPostProvidedModule,
        envMerger,
        providedMerger,
        requirementMerger
      )

    override final def productR[E, P, R, B](
      r: Hierarchy[F, E, P, R, B]
    )(implicit
      evNoReprovided: NoReProvidedModules[Provided, P],
      evNoPostProvidedModule: NoPostProvidedModules[Requirements, P],
      envMerger: Merger[Environment, E],
      providedMerger: Merger[Provided, P],
      requirementMerger: Merger[Requirements, R]
    ): Hierarchy[F, envMerger.Out, providedMerger.Out, requirementMerger.Out, B] =
      flatMap[E, P, R, B](_ => r)(
        evNoReprovided,
        evNoPostProvidedModule,
        envMerger,
        providedMerger,
        requirementMerger
      )

    override def evalTap[B](f: A => F[B]): Hierarchy[F, Environment, Provided, Requirements, A] =
      evalMap(a => f(a).as(a))

//    override protected[this] def __provide[Module: ModuleKey, MReq](module: Module)(implicit
//      R: RequirementExtractor.Aux[F, Module, MReq],
//      evNoReprovided: NoReProvidedModules[Provided, Module],
//      evNoPostProvidedModule: NoPostProvidedModules[Requirements, Module],
//      providedMerger: Merger[Provided, Module],
//      requirementMerger: Merger[Requirements, MReq]
//    ): Hierarchy[F, Environment, providedMerger.Out, requirementMerger.Out, Module] =
//      productR(Hierarchy.provide[F, Module](module))(
//        evNoReprovided,
//        evNoPostProvidedModule,
//        implicitly[Merger.Aux[Environment, Any, Environment]],
//        providedMerger,
//        requirementMerger
//      )
//
//    override protected[this] def __provideF[Module: ModuleKey, MReq](module: F[Module])(implicit
//      R: RequirementExtractor.Aux[F, Module, MReq],
//      evNoReprovided: NoReProvidedModules[Provided, Module],
//      evNoPostProvidedModule: NoPostProvidedModules[Requirements, Module],
//      providedMerger: Merger[Provided, Module],
//      requirementMerger: Merger[Requirements, MReq]
//    ): Hierarchy[F, Environment, providedMerger.Out, requirementMerger.Out, Module] =
//      flatMapF(_ => module.map(Hierarchy.provide[F, Module](_)))(
//        evNoReprovided,
//        evNoPostProvidedModule,
//        implicitly[Merger.Aux[Environment, Any, Environment]],
//        providedMerger,
//        requirementMerger
//      )
//
//    override protected[this] def __provide_[Module: ModuleKey, MReq](module: Module)(implicit
//      R: RequirementExtractor.Aux[F, Module, MReq],
//      evNoReprovided: NoReProvidedModules[Provided, Module],
//      evNoPostProvidedModule: NoPostProvidedModules[Requirements, Module],
//      providedMerger: Merger[Provided, Module],
//      requirementMerger: Merger[Requirements, MReq]
//    ): Hierarchy[F, Environment, providedMerger.Out, requirementMerger.Out, Unit] =
//      product_(Hierarchy.provide[F, Module](module))(
//        evNoReprovided,
//        evNoPostProvidedModule,
//        implicitly[Merger.Aux[Environment, Any, Environment]],
//        providedMerger,
//        requirementMerger
//      )
//
//    override protected[this] def __provideF_[Module: ModuleKey, MReq](module: F[Module])(implicit
//      R: RequirementExtractor.Aux[F, Module, MReq],
//      evNoReprovided: NoReProvidedModules[Provided, Module],
//      evNoPostProvidedModule: NoPostProvidedModules[Requirements, Module],
//      providedMerger: Merger[Provided, Module],
//      requirementMerger: Merger[Requirements, MReq]
//    ): Hierarchy[F, Environment, providedMerger.Out, requirementMerger.Out, Unit] =
//      flatMapF(_ => module.map(Hierarchy.provide[F, Module](_)))(
//        evNoReprovided,
//        evNoPostProvidedModule,
//        implicitly[Merger.Aux[Environment, Any, Environment]],
//        providedMerger,
//        requirementMerger
//      ).as(())
//
//    override protected[this] def __provideTap[Module: ModuleKey, MReq](module: Module)(implicit
//      R: RequirementExtractor.Aux[F, Module, MReq],
//      evNoReprovided: NoReProvidedModules[Provided, Module],
//      evNoPostProvidedModule: NoPostProvidedModules[Requirements, Module],
//      providedMerger: Merger[Provided, Module],
//      requirementMerger: Merger[Requirements, MReq]
//    ): Hierarchy[F, Environment, providedMerger.Out, requirementMerger.Out, A] =
//      productL(Hierarchy.provide[F, Module](module))(
//        evNoReprovided,
//        evNoPostProvidedModule,
//        implicitly[Merger.Aux[Environment, Any, Environment]],
//        providedMerger,
//        requirementMerger
//      )
//
//    override protected[this] def __provideFTap[Module: ModuleKey, MReq](module: F[Module])(implicit
//      R: RequirementExtractor.Aux[F, Module, MReq],
//      evNoReprovided: NoReProvidedModules[Provided, Module],
//      evNoPostProvidedModule: NoPostProvidedModules[Requirements, Module],
//      providedMerger: Merger[Provided, Module],
//      requirementMerger: Merger[Requirements, MReq]
//    ): Hierarchy[F, Environment, providedMerger.Out, requirementMerger.Out, A] =
//      flatTapF(_ => module.map(Hierarchy.provide[F, Module](_)))(
//        evNoReprovided,
//        evNoPostProvidedModule,
//        implicitly[Merger.Aux[Environment, Any, Environment]],
//        providedMerger,
//        requirementMerger
//      )

    override def swap[ModuleT: Key](module: ModuleT)(implicit
      requirementMerger: Merger[Requirements, ModuleT]
    ): Hierarchy[F, Environment, Provided, requirementMerger.Out, ModuleT] =
      productR(Hierarchy.swap[F, ModuleT](module))(
        implicitly[NoReProvidedModules[Provided, Any]],
        implicitly[NoPostProvidedModules[Requirements, Any]],
        implicitly[Merger.Aux[Environment, Any, Environment]],
        implicitly[Merger.Aux[Provided, Any, Provided]],
        requirementMerger
      )

    override def swapF[ModuleT: Key](module: F[ModuleT])(implicit
      requirementMerger: Merger[Requirements, ModuleT]
    ): Hierarchy[F, Environment, Provided, requirementMerger.Out, ModuleT] =
      flatMapF(_ => module.map(Hierarchy.swap[F, ModuleT](_)))(
        implicitly[NoReProvidedModules[Provided, Any]],
        implicitly[NoPostProvidedModules[Requirements, Any]],
        implicitly[Merger.Aux[Environment, Any, Environment]],
        implicitly[Merger.Aux[Provided, Any, Provided]],
        requirementMerger
      )

    override def swap_[ModuleT: Key](module: ModuleT)(implicit
      requirementMerger: Merger[Requirements, ModuleT]
    ): Hierarchy[F, Environment, Provided, requirementMerger.Out, Unit] =
      product_(Hierarchy.swap[F, ModuleT](module))(
        implicitly[NoReProvidedModules[Provided, Any]],
        implicitly[NoPostProvidedModules[Requirements, Any]],
        implicitly[Merger.Aux[Environment, Any, Environment]],
        implicitly[Merger.Aux[Provided, Any, Provided]],
        requirementMerger
      )

    override def swapF_[ModuleT: Key](module: F[ModuleT])(implicit
      requirementMerger: Merger[Requirements, ModuleT]
    ): Hierarchy[F, Environment, Provided, requirementMerger.Out, Unit] =
      flatMapF(_ => module.map(Hierarchy.swap[F, ModuleT](_)))(
        implicitly[NoReProvidedModules[Provided, Any]],
        implicitly[NoPostProvidedModules[Requirements, Any]],
        implicitly[Merger.Aux[Environment, Any, Environment]],
        implicitly[Merger.Aux[Provided, Any, Provided]],
        requirementMerger
      ).as(())

    override def swapTap[ModuleT: Key](module: ModuleT)(implicit
      requirementMerger: Merger[Requirements, ModuleT]
    ): Hierarchy[F, Environment, Provided, requirementMerger.Out, A] =
      productL(Hierarchy.swap[F, ModuleT](module))(
        implicitly[NoReProvidedModules[Provided, Any]],
        implicitly[NoPostProvidedModules[Requirements, Any]],
        implicitly[Merger.Aux[Environment, Any, Environment]],
        implicitly[Merger.Aux[Provided, Any, Provided]],
        requirementMerger
      )

    override def swapFTap[ModuleT: Key](module: F[ModuleT])(implicit
      requirementMerger: Merger[Requirements, ModuleT]
    ): Hierarchy[F, Environment, Provided, requirementMerger.Out, A] =
      flatTapF(_ => module.map(Hierarchy.swap[F, ModuleT](_)))(
        implicitly[NoReProvidedModules[Provided, Any]],
        implicitly[NoPostProvidedModules[Requirements, Any]],
        implicitly[Merger.Aux[Environment, Any, Environment]],
        implicitly[Merger.Aux[Provided, Any, Provided]],
        requirementMerger
      )
  }

  final case class Value[F[_]: Monad, A](inner: F[A]) extends Impl[F, Any, Any, Any, A] {
    override def evalMap[B](f: A => F[B]): Impl[F, Any, Any, Any, B] =
      copy(inner = inner.flatMap(f))
    override def widen[B >: A]: Hierarchy[F, Any, Any, Any, B]       =
      copy[F, B](inner = inner.widen[B])
    override protected[hierarchy] def collect(
      requirements: Map[Key[_], Any],
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, A)] =
      inner.map(collector -> _)
  }

  abstract class Consumer[F[_]: Monad, Environment, Provided, Requirements, Elt, A]
      extends Impl[F, Environment, Provided, Requirements, A] {
    def inner: Elt => F[A]
    def extract: (Map[Key[_], Any], ModuleMap) => Elt
    protected[this] def withInner[B](newInner: Elt => F[B]): Impl[F, Environment, Provided, Requirements, B]
    override final def widen[B >: A]: Impl[F, Environment, Provided, Requirements, B] =
      withInner(inner(_).widen[B])

    override final def evalMap[B](f: A => F[B]): Impl[F, Environment, Provided, Requirements, B] =
      withInner(inner(_).flatMap(f))

    override final protected[hierarchy] def collect(
      environment: Map[Key[_], Any],
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, A)] =
      inner(extract(environment, collector.modules)).map(collector -> _)
  }

  final case class ModuleConsumer[F[_]: Monad, ModuleT: Key, A](
    inner: ModuleT => F[A]
  ) extends Consumer[F, Any, Any, ModuleT, ModuleT, A] {
    override def extract: (Map[Key[_], Any], ModuleMap) => ModuleT = (_, modules) =>
      modules(Key[ModuleT]).asInstanceOf[ModuleT]

    override protected[this] def withInner[B](
      newInner: ModuleT => F[B]
    ): Impl[F, Any, Any, ModuleT, B] =
      copy[F, ModuleT, B](inner = newInner)
  }

  final case class EnvironmentConsumer[F[_]: Monad, Env: Key, A](
    inner: Env => F[A]
  ) extends Consumer[F, Env, Any, Any, Env, A] {
    override def extract: (Map[Key[_], Any], ModuleMap) => Env = (env, _) => env(Key[Env]).asInstanceOf[Env]

    override protected[this] def withInner[B](newInner: Env => F[B]): Impl[F, Env, Any, Any, B] =
      copy[F, Env, B](inner = newInner)
  }

  final case class Producer[F[_]: Monad, Provided, Requirements, M: Key, A](inner: F[(M, A)])
      extends Impl[F, Any, Provided, Requirements, A] {

    override def widen[B >: A]: Impl[F, Any, Provided, Requirements, B] =
      copy[F, Provided, Requirements, M, B](inner = inner.widen[(M, B)])

    override def evalMap[B](f: A => F[B]): Impl[F, Any, Provided, Requirements, B] =
      copy[F, Provided, Requirements, M, B](inner = inner.flatMap { case (module, a) =>
        f(a).map(module -> _)
      })

    override protected[hierarchy] def collect(
      requirements: Map[Key[_], Any],
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, A)] =
      inner.map { case (module, a) => collector.addModule[M](module) -> a }
  }

  final case class Updater[F[_]: Monad, M: Key, A](inner: M => F[(M, A)]) extends Impl[F, Any, Any, M, A] {
    override def widen[B >: A]: Impl[F, Any, Any, M, B] =
      copy[F, M, B](inner = inner(_).widen[(M, B)])

    override def evalMap[B](f: A => F[B]): Impl[F, Any, Any, M, B] =
      copy[F, M, B](inner = inner(_).flatMap { case (module, a) =>
        f(a).map(module -> _)
      })

    override protected[hierarchy] def collect(
      requirements: Map[Key[_], Any],
      collector: HierarchyModuleCollector
    ): F[(HierarchyModuleCollector, A)] =
      inner(collector.modules(Key[M]).asInstanceOf[M]).map { case (m, a) => collector.addModule[M](m) -> a }

  }

  final case class FlatMap[
    F[_],
    SelfEnvironment,
    SelfProvided,
    SelfRequirements,
    NextEnvironment,
    NextProvided,
    NextRequirements,
    NewEnvironment,
    NewProvided,
    NewRequirements,
    A,
    NextA
  ](
    self: Impl[F, SelfEnvironment, SelfProvided, SelfRequirements, A],
    next: A => F[Hierarchy[F, NextEnvironment, NextProvided, NextRequirements, NextA]]
  )(implicit
    @unused envMerger: Merger.Aux[SelfEnvironment, NextEnvironment, NewEnvironment],
    @unused providedMerger: Merger.Aux[SelfProvided, NextProvided, NewProvided],
    @unused requirementMerger: Merger.Aux[SelfRequirements, NextRequirements, NewRequirements],
    M: Monad[F]
  ) extends Impl[
        F,
        NewEnvironment,
        NewProvided,
        NewRequirements,
        NextA
      ] {
    override def widen[B >: NextA]: Impl[
      F,
      NewEnvironment,
      NewProvided,
      NewRequirements,
      B
    ] =
      copy[
        F,
        SelfEnvironment,
        SelfProvided,
        SelfRequirements,
        NextEnvironment,
        NextProvided,
        NextRequirements,
        NewEnvironment,
        NewProvided,
        NewRequirements,
        A,
        B
      ](
        next = next(_).map(_.widen[B])
      )

    override def evalMap[B](
      f: NextA => F[B]
    ): Impl[
      F,
      NewEnvironment,
      NewProvided,
      NewRequirements,
      B
    ] =
      copy[
        F,
        SelfEnvironment,
        SelfProvided,
        SelfRequirements,
        NextEnvironment,
        NextProvided,
        NextRequirements,
        NewEnvironment,
        NewProvided,
        NewRequirements,
        A,
        B
      ](
        next = next(_).map(_.evalMap(f))
      )

    override protected[hierarchy] def collect(
      requirements: Map[Key[_], Any],
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
    order: List[Key[_]]
  ) extends Product {
    def addModule[ModuleT: Key](
      module: ModuleT
    ): HierarchyModuleCollector =
      HierarchyModuleCollector(
        modules = modules + (Key[ModuleT] -> module),
        order =
          if (order.contains(Key[ModuleT])) order // Swap of module
          else Key[ModuleT] :: order
      )

    def toCollection[F[_]: Sync]: HierarchyModuleCollection[F, Any] =
      HierarchyModuleCollection(modules.pure[F], order)
  }

  final case class HierarchyModuleCollection[F[_]: Sync, Provided](
    modules: F[ModuleMap],
    order: List[Key[_]]
  ) extends Product {

    private[hierarchy] val beforeRun: F[ModuleMap] =
      logger[F].flatMap { implicit logger =>
        def beforeRun(updatedModules: ModuleMap, remaining: List[Key[_]]): F[ModuleMap] = remaining match {
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

    def update[ModuleT: Key](modifyFn: ModuleT => ModuleT)(implicit
      hasModule: Provided <:< ModuleT
    ): HierarchyModuleCollection[F, Provided] =
      updateF[ModuleT](modifyFn(_).pure[F])

    def updateF[ModuleT: Key](
      modifyFn: ModuleT => F[ModuleT]
    )(implicit hasModule: Provided <:< ModuleT): HierarchyModuleCollection[F, Provided] =
      copy[F, Provided](modules = modules.flatMap { modules =>
        val oldModule = modules(Key[ModuleT]).asInstanceOf[ModuleT]

        modifyFn(oldModule).map(modules.updated(Key[ModuleT], _))
      })

    def useF[ModuleT: Key](useF: ModuleT => F[Unit])(implicit
      hasModule: Provided <:< ModuleT
    ): HierarchyModuleCollection[F, Provided] =
      updateF[ModuleT](module => useF(module).as(module))
  }
}

abstract class HierarchyRunner[F[_], Environment] private[hierarchy] (envs: Map[Key[_], Any]) {
  final def give[Elt: Key](elt: Elt)(implicit
    @unused envNotEmpty: Environment =:!= Any,
    remover: EnvRemover[Elt, Environment]
  ): HierarchyRunner[F, remover.Out] =
    withEnv[remover.Out](envs + (Key[Elt] -> elt))

  private[hierarchy] def withEnv[NEnv](newEnvs: Map[Key[_], Any]): HierarchyRunner[F, NEnv]

  def run(implicit emptyEnv: Environment =:= Any, S: Sync[F], P: Parallel[F]): F[Unit]
}
