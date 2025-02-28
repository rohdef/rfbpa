package dk.rohdef.rfbpa.web

import arrow.core.singleOrNone
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

@Serializable
sealed interface ErrorType {
    fun serialize(): String {
        return serialize(this::class)
    }

    private fun <T : ErrorType> serialize(clazz: KClass<T>): String {
        val parentSubtypedErrorType = clazz.supertypes
            .filter { it.isSubtypeOf(typeOf<ErrorType>()) }
            .singleOrNone()
            .getOrNull()

        if (parentSubtypedErrorType == null) {
            throw IllegalStateException("Type must be a subtype of ${ErrorType::class.simpleName}, but isn't. This case should be impossible")
        }

        return if (parentSubtypedErrorType != ErrorType::class.createType()) {
            val subClass = parentSubtypedErrorType.classifier as KClass<out ErrorType>
            "${serialize(subClass)}.${clazz.simpleName}"
        } else {
            clazz.simpleName!!
        }
    }

    companion object {
        fun deserialize(serialized: String): ErrorType {
            val pathComponents = serialized.split(".")

            var currentType: KClass<*> = ErrorType::class
            for (pathComponent in pathComponents) {
                currentType = currentType.sealedSubclasses
                    .associateBy { it.simpleName!! }
                    .getOrElse(pathComponent) { throw IllegalArgumentException("No path component matches $pathComponent in the direct sealed subclasses of  ${ErrorType            ::class.qualifiedName}") }
            }

            // TODO error checking very much needed
            return (currentType.objectInstance as ErrorType)
        }
    }
}

sealed interface Blah : ErrorType

sealed interface Bluh : Blah

@Serializable
object UnknownError : Bluh