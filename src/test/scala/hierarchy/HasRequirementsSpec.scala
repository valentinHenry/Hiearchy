package fr.valentinhenry
package hierarchy

import scala.annotation.unused

object HasRequirementsSpec {
  // TODO find a way to make this really testable

  def checkValid[P, R](implicit ev: HasRequirements[P, R]): Unit               = ()
  def checkInvalid[P, R](implicit ev: InvalidHasRequirementsCheck[P, R]): Unit = ()

  type P1 = Int
  type R1 = Int
  checkValid[P1, R1]

  type P2 = Int &: Float
  type R2 = Int
  checkValid[P2, R2]

  type P3 = Int &: Float &: String
  type R3 = Int &: Float &: String
  checkValid[P3, R3]

  type P4 = Float &: Int
  type R4 = Float &: Int
  checkValid[P4, R4]

  type P5 = String &: Int &: Float
  type R5 = Float &: Int &: String
  checkValid[P5, R5]

  type P6 = String &: Int &: Float
  type R6 = Float &: String &: Int
  checkValid[P6, R6]

  type P7 = Float
  type R7 = Any
  checkValid[P7, R7]

  type P8 = Float
  type R8 = String
  checkInvalid[P8, R8]

  type P9 = Float
  type R9 = String &: Int
  checkInvalid[P9, R9]

  type P10 = Float
  type R10 = String &: Int &: Byte
  checkInvalid[P10, R10]

}

trait InvalidHasRequirementsCheck[P, R]

object InvalidHasRequirementsCheck {
  final val singleton = new InvalidHasRequirementsCheck[Any, Any] {}

  implicit def base[P, R]: InvalidHasRequirementsCheck[P, R] =
    singleton.asInstanceOf[InvalidHasRequirementsCheck[P, R]]

  implicit def instance1[P, R](implicit
    @unused ev: HasRequirements[P, R]
  ): InvalidHasRequirementsCheck[P, R] =
    singleton.asInstanceOf[InvalidHasRequirementsCheck[P, R]]
}
