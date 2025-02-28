@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class)

package dk.rohdef.helperplanning

import kotlinx.uuid.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

fun Uuid.toKotlinxUUID(): UUID = this.toJavaUuid().toKotlinUUID()

fun UUID.toKotlinUuid(): Uuid = this.toJavaUUID().toKotlinUuid()

fun Uuid.generateUuid(namespace: Uuid, idText: String): Uuid {
    return UUID.generateUUID(namespace.toKotlinxUUID(), idText).toKotlinUuid()
}