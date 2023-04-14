package fr.valentinhenry
package runtime4s

import runtime4s.Runtime.{EmptyProvided, EmptyRequirement}

import shapeless.{<:!<, =:!=}

import scala.annotation.{implicitAmbiguous, implicitNotFound, unused}

object capabilities {
  @implicitNotFound("The provided modules ${Provided} misses a required module ${Requirements}")
  type HasRequirements[-Provided, Requirements] = Provided <:< Requirements

  trait NoPostProvidedModules[LeftProvided, LeftRequirements, RightProvided]
  object NoPostProvidedModules {
    final val singleton = new NoPostProvidedModules[Any, Any, Any] {}

    @implicitAmbiguous("One of the modules ${RP} is provided after being required by ${LR}")
    implicit def base[LP, LR, RP]: NoPostProvidedModules[LP, LR, RP] =
      singleton.asInstanceOf[NoPostProvidedModules[LP, LR, RP]]

    @implicitAmbiguous("One of the modules ${RP} is provided after being required by ${LR}")
    implicit def instance1[LP, LR, RP](implicit
      @unused evLeftNotEmptyRequirement: LR =:!= EmptyRequirement,
      @unused evRightNotEmptyProvided: RP =:!= EmptyProvided,
      @unused ev1: RP <:!< LP,
      @unused ev2: LR <:< RP
    ): NoPostProvidedModules[LP, LR, RP] =
      singleton.asInstanceOf[NoPostProvidedModules[LP, LR, RP]]
  }

  trait RequirementExtractor[F[_], A] {
    type Requirements
  }

  object RequirementExtractor {
    type Aux[F[_], A, Req] = RequirementExtractor[F, A] { type Requirements = Req }

    implicit final def modifierRequirements[F[_], A, Req](implicit
      @unused isModifier: A <:< ModifierRuntimeModule.Aux[F, Req]
    ): RequirementExtractor.Aux[F, A, Req] =
      new RequirementExtractor[F, A] {
        override type Requirements = Req
      }

    implicit final def nonModifierRequirements[F[_], A](implicit
      @unused isNotAModifier: A <:!< ModifierRuntimeModule[F]
    ): RequirementExtractor.Aux[F, A, EmptyRequirement] =
      new RequirementExtractor[F, A] {
        override type Requirements = EmptyRequirement
      }
  }
}
