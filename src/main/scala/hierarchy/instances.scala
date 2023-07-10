package fr.valentinhenry
package hierarchy

import shapeless.{<:!<, =:!=, Lazy}

import scala.annotation.{implicitAmbiguous, implicitNotFound, unused}

object instances      {
  implicit def AndSyntax[A](a: A): AndSyntaxOps[A] =
    new AndSyntaxOps[A](a)

  final class AndSyntaxOps[A](a: A) {
    def &:[B](b: B): A &: B = new &:[A, B] {
      override def a_ : A = a
      override def b_ : B = b
    }
  }
}
sealed trait &:[A, B] {
  def a_ : A
  def b_ : B
}

trait Merger[A, B] {
  type Out
}

trait LowPriorityMerger {
  type Aux[L, R, Out_] = Merger[L, R] { type Out = Out_ }
  protected[this] def instance[L, R, Out_]: Aux[L, R, Out_] = new Merger[L, R] {
    override type Out = Out_
  }

  implicit def singleMergerLeft[L, R, ROut](implicit
    @unused evLNotAny: L =:!= Any,
    //      @unused evLSingle: L <:!< +:[_, _] Done via low priority,
    @unused evNotEqual: L =:!= R,
    @unused rL: Removed.Aux[L, R, ROut]
  ): Aux[L, R, L &: ROut] = instance

  implicit def sameMerger[E]: Aux[E, E, E] = instance
}

object Merger extends LowPriorityMerger {
  implicit def emptyMergerLeft[L]: Aux[L, Any, L]                                          = instance
  implicit def emptyMergerRight[R](implicit @unused evRNotAny: R =:!= Any): Aux[Any, R, R] = instance

  implicit def merger1[Elt, L, R, ROut](implicit
    @unused rL: Removed.Aux[Elt, R, ROut],
    @unused m: Merger[L, ROut],
    @unused rNotAny: R =:!= Any
  ): Aux[Elt &: L, R, Elt &: m.Out] = instance
}

trait Removed[Elt, R] {
  type Out
}

trait LowPriorityRemoved {
  type Aux[Elt, R, Out_] = Removed[Elt, R] { type Out = Out_ }

  protected[this] def instance[Elt, R, Out_]: Aux[Elt, R, Out_] =
    new Removed[Elt, R] {
      override type Out = Out_
    }

  implicit def tail[Elt, R]: Aux[Elt, R, R] = instance
}
object Removed extends LowPriorityRemoved {
  implicit def emptyRemoved[Elt]: Aux[Elt, Any, Any] = instance

  implicit def same[Elt]: Aux[Elt, Elt, Any] = instance

  implicit def recFound[Elt, RElt, RTail](implicit
    @unused evFound: Elt =:= RElt,
    @unused r: Removed[Elt, RTail]
  ): Aux[Elt, RElt &: RTail, r.Out] = instance

  implicit def recFound2[Elt, RElt, RTail](implicit
    @unused evFound: Elt =:= RElt,
    @unused r: Aux[Elt, RTail, Any]
  ): Aux[Elt, RElt &: RTail, Any] = instance

  implicit def recNotFound1[Elt, RElt, RTail](implicit
    @unused evNotFound: Elt =:!= RElt,
    @unused r: Removed[Elt, RTail]
  ): Aux[Elt, RElt &: RTail, RElt &: r.Out] = instance

  implicit def recNotFound2[Elt, RElt, RTail](implicit
    @unused evNotFound: Elt =:!= RElt,
    @unused r: Removed.Aux[Elt, RTail, Any]
  ): Aux[Elt, RElt &: RTail, RElt] = instance
}

object requirements {
  @implicitAmbiguous("One of the modules ${RightProvided} is provided after being required by ${LeftRequirements}")
  type NoPostProvidedModules[LeftRequirements, RightProvided] = NoIntersect[LeftRequirements, RightProvided]

  @implicitAmbiguous(
    "One of the modules ${RightProvided} has been provided previously in ${LeftProvided}. Use `.swap` instead of `.provide`"
  ) type NoReProvidedModules[LeftProvided, RightProvided] = NoIntersect[LeftProvided, RightProvided]
}

sealed trait NoIntersect[L, R]
object NoIntersect {
  implicit val singleton: NoIntersect[Any, Any]         = new NoIntersect[Any, Any] {}
  protected[this] def instance[L, R]: NoIntersect[L, R] = singleton.asInstanceOf[NoIntersect[L, R]]
  implicit def endTail[L, R](implicit
    @unused ev3: Not[Contained[R, L]]
  ): NoIntersect[L, R] = instance

  implicit def rec[L, RElt, RTail](implicit
    @unused found: NoIntersect[L, RTail],
    @unused ev3: Not[Contained[RElt, L]]
  ): NoIntersect[L, RElt &: RTail] = instance
}

sealed trait Intersect[L, R]
trait IntersectLowPriority {
  protected[this] val singleton: Intersect[Any, Any] = new Intersect[Any, Any] {}

  protected[this] def instance[L, R]: Intersect[L, R] = singleton.asInstanceOf[Intersect[L, R]]

  implicit def notFound[L, RElt, RTail](implicit @unused rec: Intersect[L, RTail]): Intersect[L, RElt &: RTail] =
    instance
}
object Intersect extends IntersectLowPriority {

  implicit def foundTail[L, R](implicit
    @unused ev3: Contained[R, L]
  ): Intersect[L, R] = instance

  implicit def foundR[L, RElt, RTail](implicit
    @unused found: Contained[RElt, L]
  ): Intersect[L, RElt &: RTail] = instance
}

sealed trait RequirementExtractor[F[_], A] {
  type Requirements
}

object RequirementExtractor {
  type Aux[F[_], A, Req] = RequirementExtractor[F, A] { type Requirements = Req }
  private[this] def instance[F[_], A, Req]: Aux[F, A, Req] =
    new RequirementExtractor[F, A] {
      override type Requirements = Req
    }

  implicit final def modifierRequirements[F[_], A, Req](implicit
    @unused isModifier: A <:< ModifierHierarchyModule.Aux[F, Req]
  ): RequirementExtractor.Aux[F, A, Req] = instance

  implicit final def nonModifierRequirements[F[_], A](implicit
    @unused isNotAModifier: A <:!< ModifierHierarchyModule[F]
  ): RequirementExtractor.Aux[F, A, Any] = instance
}

@implicitNotFound("The provided modules ${Provided} misses a required module ${Requirements}")
sealed trait HasRequirements[Provided, Requirements]

object HasRequirements {
  private[this] val singleton: HasRequirements[Any, Any]  = new HasRequirements[Any, Any] {}
  private[this] def instance[A, B]: HasRequirements[A, B] = singleton.asInstanceOf[HasRequirements[A, B]]

  implicit def emptyInstance[P]: HasRequirements[P, Any] = instance

  implicit def simpleInstance[P, SingleR](implicit
    @unused evRSimple: SingleR <:!< &:[_, _],
    @unused found: Contained[SingleR, P]
  ): HasRequirements[P, SingleR] = instance

  implicit def traverseInstance[P, RElt, RTail](implicit
    @unused found: Contained[RElt, P],
    @unused tail: Lazy[HasRequirements[P, RTail]]
  ): HasRequirements[P, RElt &: RTail] = instance
}

sealed trait Contained[Searched, Provided]
object Contained {
  private[this] val singleton: Contained[Any, Any] = new Contained[Any, Any] {}

  private[this] def instance[A, B]: Contained[A, B] = singleton.asInstanceOf[Contained[A, B]]

  implicit def sameInstance[A]: Contained[A, A] = instance

  implicit def foundInstance[S, PElt, PTail](implicit
    @unused evFound: S =:= PElt
  ): Contained[S, PElt &: PTail] = instance

  implicit def searchInstance[S, PElt, PTail](implicit
    @unused evNotFound: S =:!= PElt,
    @unused search: Lazy[Contained[S, PTail]]
  ): Contained[S, PElt &: PTail] = instance
}

sealed trait Not[V]
object Not {
  implicit def instance[V]: Not[V]                         = new Not[V] {}
  implicit def instance2[V](implicit @unused v: V): Not[V] = sys.error("unexpected invocation")

}
