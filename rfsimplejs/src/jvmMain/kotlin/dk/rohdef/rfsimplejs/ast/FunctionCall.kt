package dk.rohdef.rfsimplejs.ast

// TODO change parameters to list
data class FunctionCall(val name: Name, val parameters: Expression) : Expression