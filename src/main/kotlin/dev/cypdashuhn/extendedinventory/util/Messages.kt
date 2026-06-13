package dev.cypdashuhn.extendedinventory.util

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

private val miniMessage = MiniMessage.miniMessage()

fun mm(s: String) = miniMessage.deserialize(s) as TextComponent

fun Player.msg(text: String) = sendMessage(miniMessage.deserialize(text))
