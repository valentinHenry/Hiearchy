package fr.valentinhenry
package runtime4s

import fr.valentinhenry.runtime4s.Runtime.{EmptyProvided, EmptyRequirement}
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
      @unused ev: LR =:!= EmptyRequirement,
      @unused ev1: RP <:!< LP,
      @unused ev2: LR <:< RP
    ): NoPostProvidedModules[LP, LR, RP] =
      singleton.asInstanceOf[NoPostProvidedModules[LP, LR, RP]]
  }
}
