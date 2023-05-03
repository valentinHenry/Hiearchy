package fr.valentinhenry
package hierarchy

object IntersectSpec {
  def checkValidNoIntersect[L, R](implicit ev: NoIntersect[L, R]): Unit        = ()
  def checkValidIntersect[L, R](implicit ev: Intersect[L, R]): Unit            = ()
  def checkInvalidNoIntersect[L, R](implicit ev: Not[NoIntersect[L, R]]): Unit = ()
  def checkInvalidIntersect[L, R](implicit ev: Not[Intersect[L, R]]): Unit     = ()

  type L21 = Any
  type R21 = Any
  checkValidIntersect[L21, R21]

  type L22 = Int
  type R22 = Any
  checkInvalidIntersect[L22, R22]

  type L222 = Any
  type R222 = Int
  checkInvalidIntersect[L222, R222]

  type L23 = Int
  type R23 = Float
  checkInvalidIntersect[L23, R23]

  type L24 = Int &: Float
  type R24 = String
  checkInvalidIntersect[L24, R24]

  type L25 = Int
  type R25 = String &: Float
  checkInvalidIntersect[L25, R25]

  type L26 = Int &: Boolean
  type R26 = String &: Float
  checkInvalidIntersect[L26, R26]

  type L27 = Int
  type R27 = Int
  checkValidIntersect[L27, R27]

  type L28 = Int &: Float
  type R28 = Int
  checkValidIntersect[L28, R28]

  type L29 = Int
  type R29 = Int &: Float
  checkValidIntersect[L29, R29]

  type L210 = Int
  type R210 = Float &: Int
  checkValidIntersect[L210, R210]

  type L211 = Float &: Int
  type R211 = Float &: Int
  checkValidIntersect[L211, R211]

  type L212 = Float &: Int &: String
  type R212 = Boolean &: Int &: Byte
  checkValidIntersect[L212, R212]

  type L213 = Int &: Float &: String
  type R213 = Boolean &: Int &: Byte
  checkValidIntersect[L213, R213]

  type L214 = String &: Float &: Int
  type R214 = Boolean &: Int &: Byte
  checkValidIntersect[L214, R214]

  // No Intersect
  type L1 = Any
  type R1 = Any
  checkValidNoIntersect[L1, R1]

  type L2 = Int
  type R2 = Any
  checkValidNoIntersect[L2, R2]

  type Lx22 = Any
  type Rx22 = Int
  checkValidNoIntersect[Lx22, Rx22]

  type L3 = Int
  type R3 = Float
  checkValidNoIntersect[L3, R3]

  type L4 = Int &: Float
  type R4 = String
  checkValidNoIntersect[L4, R4]

  type L5 = Int
  type R5 = String &: Float
  checkValidNoIntersect[L5, R5]

  type L6 = Int &: Boolean
  type R6 = String &: Float
  checkValidNoIntersect[L6, R6]

  type L7 = Int
  type R7 = Int
  checkInvalidNoIntersect[L7, R7]

  type L8 = Int &: Float
  type R8 = Int
  checkInvalidNoIntersect[L8, R8]

  type L9 = Int
  type R9 = Int &: Float
  checkInvalidNoIntersect[L9, R9]

  type L10 = Int
  type R10 = Float &: Int
  checkInvalidNoIntersect[L10, R10]

  type L11 = Float &: Int
  type R11 = Float &: Int
  checkInvalidNoIntersect[L11, R11]

  type L12 = Float &: Int &: String
  type R12 = Boolean &: Int &: Byte
  checkInvalidNoIntersect[L12, R12]

  type L13 = Int &: Float &: String
  type R13 = Boolean &: Int &: Byte
  checkInvalidNoIntersect[L13, R13]

  type L14 = String &: Float &: Int
  type R14 = Boolean &: Int &: Byte
  checkInvalidNoIntersect[L14, R14]
}
