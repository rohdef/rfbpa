package dk.rohdef.rfsimplejs.ast

// TODO add properties (and other?)
data class ChainedCall(val expression: Expression, val functionCall: FunctionCall) : Expression