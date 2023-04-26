package fr.valentinhenry
package hierarchy

import hierarchy.Hierarchy._
import hierarchy.capabilities.NoPostProvidedModules

class NoPostProvidedModulesSpec {
    // TODO find a way to make this really testable

    def checkValid[LP, LR, RP](implicit ev: NoPostProvidedModules[LP, LR, RP]): Unit     = ()
    def checkInvalid[LP, LR, RP](implicit ev: InvalidRequirementCheck[LP, LR, RP]): Unit = ()

    // Test 1 -- no requirements / provided etc.
    type LP1 = NothingProvided
    type LR1 = NoRequirement
    type RP1 = NothingProvided

    val ev1 = checkValid[LP1, LR1, RP1]

    // Test2 -- Provided but no requirements
    type LP2 = Int
    type LR2 = NoRequirement
    type RP2 = NothingProvided

    val ev2 = checkValid[LP2, LR2, RP2]

    // Test3 -- Provided both side
    type LP3 = Int
    type LR3 = NoRequirement
    type RP3 = Float

    val ev3 = checkValid[LP3, LR3, RP3]

    // Test4 -- Provided both side + requirement
    type LP4 = Int
    type LR4 = String
    type RP4 = Float

    val ev4 = checkValid[LP4, LR4, RP4]

    // Test5 -- Multiple provided both side + requirement
    type LP5 = Int with Double
    type LR5 = String
    type RP5 = Float with Boolean

    val ev5 = checkValid[LP5, LR5, RP5]

    // Test6 -- Multiple provided both side + multiple requirement
    type LP6 = Int with Double
    type LR6 = String with Boolean
    type RP6 = Float with Byte

    val ev6 = checkValid[LP6, LR6, RP6]

    // Test7 -- type re-provided
    type LP7 = Int
    type LR7 = NoRequirement
    type RP7 = Int
    val ev7 = checkInvalid[LP7, LR7, RP7]

    // Test8 -- type re-provided
    type LP8 = Int
    type LR8 = NoRequirement
    type RP8 = Int with Float
    val ev8 = checkInvalid[LP8, LR8, RP8]

    // Test9 -- type re-provided
    type LP9 = Int with Float
    type LR9 = NoRequirement
    type RP9 = Int
    val ev9 = checkInvalid[LP9, LR9, RP9]

    // FIXME this test does not work
    //    // Test10 -- type re-provided and required
    //    type LP10 = Int with String
    //    type LR10 = NoRequirement
    //    type RP10 = Int with Float
    //    val ev10 = checkInvalid[LP10, LR10, RP10]

    // Test11 -- type required but provided afterward
    type LP11 = NothingProvided
    type LR11 = Float with Double
    type RP11 = Float
    val ev11 = checkInvalid[LP11, LR11, RP11]

    // Test12 -- type required but provided afterward
    type LP12 = NothingProvided
    type LR12 = Int
    type RP12 = Int with Float
    val ev12 = checkInvalid[LP12, LR12, RP12]

    // FIXME this test does not work
    //    // Test13 -- type required but provided afterward multiple modules
    //    type LP13 = NothingProvided
    //    type LR13 = Int with String
    //    type RP13 = Float with Double
    //    val ev13 = checkInvalid[LP13, LR13, RP13]
}
