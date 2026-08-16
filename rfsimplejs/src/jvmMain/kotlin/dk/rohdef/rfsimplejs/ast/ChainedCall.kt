package dk.rohdef.rfsimplejs.ast

// TODO add properties (and other?)
data class ChainedCall(val exp: Exp, val functionCall: FunctionCall) : Exp