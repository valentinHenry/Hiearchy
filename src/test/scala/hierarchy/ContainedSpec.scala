package fr.valentinhenry
package hierarchy

object ContainedSpec {
  // TODO find a way to make this really testable

  def checkValid[S, P](implicit ev: Contained[S, P]): Unit        = ()
  def checkInvalid[S, P](implicit ev: Not[Contained[S, P]]): Unit = ()

  type S0 = Any
  type P0 = Any
  checkValid[S0, P0]

  type S1 = Int
  type P1 = Int
  checkValid[S1, P1]

  type S2 = Int
  type P2 = Int &: Float
  checkValid[S2, P2]

  type S3 = Int
  type P3 = Int &: Float &: String
  checkValid[S3, P3]

  type S4 = Int
  type P4 = Float &: Int
  checkValid[S4, P4]

  type S5 = Int
  type P5 = Float &: Int &: String
  checkValid[S5, P5]

  type S6 = Int
  type P6 = Float &: String &: Int
  checkValid[S6, P6]

  type S7 = Float
  type P7 = Any
  checkInvalid[S7, P7]

  type S8 = Float
  type P8 = String
  checkInvalid[S8, P8]

  type S9 = Float
  type P9 = String &: Int
  checkInvalid[S9, P9]

  type S10 = Float
  type P10 = String &: Int &: Byte
  checkInvalid[S10, P10]

}
