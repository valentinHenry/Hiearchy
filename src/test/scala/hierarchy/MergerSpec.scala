package fr.valentinhenry
package hierarchy

object MergerSpec {
  def checkValid[P, R, Res](implicit ev: Merger.Aux[P, R, Res]): Unit = ()

  type L1 = Any
  type R1 = Any
  checkValid[L1, R1, Any]

  type L2 = Any
  type R2 = String
  checkValid[L2, R2, String]

  type L3 = String
  type R3 = Any
  checkValid[L3, R3, String]

  type L4 = String
  type R4 = Int
  checkValid[L4, R4, String &: Int]

  type L5 = String &: Float
  type R5 = Int
  checkValid[L5, R5, String &: Float &: Int]

  type L6 = String
  type R6 = Int &: Float
  checkValid[L6, R6, String &: Int &: Float]

  type L7 = String &: Double
  type R7 = Int &: Float
  checkValid[L7, R7, String &: Double &: Int &: Float]

  type L8 = String &: Double &: Byte
  type R8 = Int &: Float &: Object
  checkValid[L8, R8, String &: Double &: Byte &: Int &: Float &: Object]

  type L9 = String
  type R9 = String
  checkValid[L9, R9, String]

  type L10 = String
  type R10 = String &: Int
  checkValid[L10, R10, String &: Int]

  type L11 = String &: Int
  type R11 = String &: Int
  checkValid[L11, R11, String &: Int]

  type L12 = String &: Int &: Float
  type R12 = Double &: Int &: String &: Byte
  checkValid[L12, R12, String &: Int &: Float &: Double &: Byte]
}
