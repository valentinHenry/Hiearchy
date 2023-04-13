package fr.valentinhenry
package runtime4s

import scala.annotation.implicitNotFound

object capabilities {
  @implicitNotFound("The provided modules ${Provided} misses a required module ${Requirements}")
  type HasRequirements[-Provided, Requirements] = Provided <:< Requirements
}
