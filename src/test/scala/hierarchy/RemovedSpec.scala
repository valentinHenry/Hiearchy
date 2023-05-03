package fr.valentinhenry
package hierarchy

object RemovedSpec {
  def check[Elt, R, Res](implicit removed: Removed.Aux[Elt, R, Res]): Unit = ()

  type E1 = Int
  type R1 = Any
  check[E1, R1, R1]

  type E2 = Int
  type R2 = String
  check[E2, R2, R2]

  type E3 = String
  type R3 = String
  check[E3, R3, Any]

  type E4 = String
  type R4 = String &: Int
  check[E4, R4, Int]

  type E5 = String
  type R5 = Int &: String
  check[E5, R5, Int]

  type E6 = String
  type R6 = Float &: String &: Int
  check[E6, R6, Float &: Int]

  type E7 = String
  type R7 = Float &: Int
  check[E7, R7, R7]
}
