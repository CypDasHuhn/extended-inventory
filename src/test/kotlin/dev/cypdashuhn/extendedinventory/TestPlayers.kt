package dev.cypdashuhn.extendedinventory

import org.bukkit.entity.Player
import java.lang.reflect.Proxy
import java.util.UUID

object TestPlayers {
    fun player(uuid: UUID = UUID.randomUUID()): Player {
        val handler =
            java.lang.reflect.InvocationHandler { _, method, _ ->
                when (method.name) {
                    "getUniqueId" -> uuid
                    "getName" -> "TestPlayer"
                    "toString" -> "MockPlayer($uuid)"
                    "hashCode" -> uuid.hashCode()
                    "equals" -> method.declaringClass == Any::class.java
                    else -> defaultFor(method.returnType)
                }
            }
        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java),
            handler,
        ) as Player
    }

    private fun defaultFor(type: Class<*>): Any? =
        when {
            !type.isPrimitive -> null
            type == Boolean::class.javaPrimitiveType -> false
            type == Char::class.javaPrimitiveType -> '\u0000'
            type == Byte::class.javaPrimitiveType -> 0.toByte()
            type == Short::class.javaPrimitiveType -> 0.toShort()
            type == Int::class.javaPrimitiveType -> 0
            type == Long::class.javaPrimitiveType -> 0L
            type == Float::class.javaPrimitiveType -> 0f
            type == Double::class.javaPrimitiveType -> 0.0
            else -> null
        }
}
