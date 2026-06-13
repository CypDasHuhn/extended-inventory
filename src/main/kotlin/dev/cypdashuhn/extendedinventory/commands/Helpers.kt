package dev.cypdashuhn.extendedinventory.commands

import dev.jorel.commandapi.arguments.LiteralArgument
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

private val mm = MiniMessage.miniMessage()

internal fun Player.msg(text: String) = sendMessage(mm.deserialize(text))

internal fun la(name: String) = LiteralArgument(name)
