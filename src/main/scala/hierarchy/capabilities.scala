package fr.valentinhenry
package hierarchy

import hierarchy.Hierarchy.{NothingProvided, NoRequirement}

import shapeless.{<:!<, =:!=}

import scala.annotation.{compileTimeOnly, implicitAmbiguous, implicitNotFound, unused}

object capabilities {
  @implicitNotFound("The provided modules ${Provided} misses a required module ${Requirements}")
  type HasRequirements[-Provided, Requirements] = Provided <:< Requirements

  trait NoPostProvidedModules[-LeftProvided, -LeftRequirements, -RightProvided]
  object NoPostProvidedModules {
    final val singleton = new NoPostProvidedModules[Any, Any, Any] {}

    implicit def evBase[LP, LR, RP]: NoPostProvidedModules[LP, LR, RP] =
      singleton.asInstanceOf[NoPostProvidedModules[LP, LR, RP]]

    @implicitAmbiguous("One of the modules ${RP} is provided after being required by ${LR}")
    implicit def evAfterProvided1[LP, LR, RP](implicit
      @unused evRightNotEmptyProvided: RP =:!= NothingProvided,
      @unused ev2: LR <:< RP
    ): NoPostProvidedModules[LP, LR, RP] =
      singleton.asInstanceOf[NoPostProvidedModules[LP, LR, RP]]

    @implicitAmbiguous("One of the modules ${RP} is provided after being required by ${LR}")
    implicit def evAfterProvided2[LP, LR, RP, S](implicit
      @unused evRightNotEmptyProvided: RP =:!= NothingProvided,
      @unused ev1: LR <:< S,
      @unused ev2: RP <:< S,
      @unused ev3: S =:!= Any
    ): NoPostProvidedModules[LP, LR, RP] =
      singleton.asInstanceOf[NoPostProvidedModules[LP, LR, RP]]

    @implicitAmbiguous(
      "One of the modules ${RP} has been provided previously in ${LP}. Use `.swap` instead of `.provide`"
    )
    implicit def evProvidedPreviously1[LP, LR, RP](implicit
      @unused evRightNotEmptyProvided: RP =:!= NothingProvided,
      @unused ev1: LP <:< RP
    ): NoPostProvidedModules[LP, LR, RP] =
      singleton.asInstanceOf[NoPostProvidedModules[LP, LR, RP]]

    @implicitAmbiguous(
      "One of the modules ${RP} has been provided previously in ${LP}. Use `.swap` instead of `.provide`"
    )
    implicit def evProvidedPreviously2[LP, LR, RP, S](implicit
      @unused evRightNotEmptyProvided: RP =:!= NothingProvided,
      @unused ev1: LP <:< S,
      @unused ev2: RP <:< S,
      @unused ev3: S =:!= Any
    ): NoPostProvidedModules[LP, LR, RP] =
      singleton.asInstanceOf[NoPostProvidedModules[LP, LR, RP]]
  }

  trait RequirementExtractor[F[_], A] {
    type Requirements
  }

  object RequirementExtractor {
    type Aux[F[_], A, Req] = RequirementExtractor[F, A] { type Requirements = Req }

    implicit final def modifierRequirements[F[_], A, Req](implicit
      @unused isModifier: A <:< ModifierHierarchyModule.Aux[F, Req]
    ): RequirementExtractor.Aux[F, A, Req] =
      new RequirementExtractor[F, A] {
        override type Requirements = Req
      }

    implicit final def nonModifierRequirements[F[_], A](implicit
      @unused isNotAModifier: A <:!< ModifierHierarchyModule[F]
    ): RequirementExtractor.Aux[F, A, NoRequirement] =
      new RequirementExtractor[F, A] {
        override type Requirements = NoRequirement
      }
  }
}
