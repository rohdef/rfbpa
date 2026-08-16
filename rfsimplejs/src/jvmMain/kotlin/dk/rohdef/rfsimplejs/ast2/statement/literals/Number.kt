package dk.rohdef.rfsimplejs.ast2.statement.literals

import java.math.BigDecimal
import java.math.BigInteger

sealed interface Number : Literal {
    sealed interface SimpleNumber : Number {
        val number: kotlin.Number

        data class IntegerNumber(override val number: BigInteger) : SimpleNumber {
            override fun toCode() = number.toString()
        }

        data class DecimalNumber(override val number: BigDecimal) : SimpleNumber {
            override fun toCode() = number.toString()
        }
    }


    data class HexadecimalNumber(val hexadecimal: String) : Number {
        override fun toCode() = hexadecimal
    }

    data class BinaryNumber(val binary: String) : Number {
        override fun toCode() = binary
    }

    data class ScientificNumber(val coefficient: SimpleNumber, val exponent: SimpleNumber.IntegerNumber) : Number {
        override fun toCode() = "${coefficient}e${exponent}"
    }
}