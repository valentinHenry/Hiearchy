package fr.valentinhenry
package hierarchy

import shapeless.{<:!<, =:!=}

import scala.annotation.unused

trait EnvRemover[A, Environment] {
  type Out
}

trait LowPriorityEnvRemover {
  type Aux[A, Environment, Out_] = EnvRemover[A, Environment] { type Out = Out_ }

  protected[this] def instance[A, R, Out_]: Aux[A, R, Out_] =
    new EnvRemover[A, R] {
      override type Out = Out_
    }

  implicit def tail[A, R]: Aux[A, R, R] = instance
}

trait LowPriorityEnvRemover2 extends LowPriorityEnvRemover  {
  implicit def recNotFound1[A, EnvH, EnvTail](implicit
    @unused evNotFound: EnvH <:!< A,
    @unused r: EnvRemover[A, EnvTail]
  ): Aux[A, EnvH &: EnvTail, EnvH &: r.Out] = instance
}
trait LowPriorityEnvRemover3 extends LowPriorityEnvRemover2 {
  implicit def recFound[A, EnvH, EnvTail](implicit
    @unused evFound: A <:< EnvH,
    @unused r: EnvRemover[A, EnvTail]
  ): Aux[A, EnvH &: EnvTail, r.Out] = instance
}

object EnvRemover extends LowPriorityEnvRemover3 {
  implicit def same[A, R](implicit ev: A <:< R): Aux[A, R, Any] = instance

  implicit def recFound2[A, EnvH, EnvTail](implicit
    @unused evFound: A <:< EnvH,
    @unused r: Aux[A, EnvTail, Any]
  ): Aux[A, EnvH &: EnvTail, Any] = instance

  implicit def recNotFound2[A, EnvH, EnvTail](implicit
    @unused evNotFound: EnvH =:!= A,
    @unused r: EnvRemover.Aux[A, EnvTail, Any]
  ): Aux[A, EnvH &: EnvTail, EnvH] = instance
}
