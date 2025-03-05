@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning

import kotlinx.uuid.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
fun Uuid.toKotlinxUUID(): UUID = this.toJavaUuid().toKotlinUUID()

@OptIn(ExperimentalUuidApi::class)
fun UUID.toKotlinUuid(): Uuid = this.toJavaUUID().toKotlinUuid()

@OptIn(ExperimentalUuidApi::class)
fun Uuid.Companion.generateUuid(namespace: Uuid, idText: String): Uuid {
    return UUID.generateUUID(namespace.toKotlinxUUID(), idText)
        .toKotlinUuid()
}