package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.db.BufferManager
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.util.msg
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.entity.Player

internal fun <T> Argument<T>.suggestBufferNames(): Argument<T> =
    replaceSuggestions(ArgumentSuggestions.strings { info ->
        val player = info.sender() as? Player ?: return@strings emptyArray()
        BufferManager.list(player).map { BufferManager.formatTimestamp(it.timestamp) }.toTypedArray()
    })

internal fun buildBufferNode() = la("buffer").apply {
    then(la("load").apply {
        then(StringArgument("name").setOptional(true).suggestBufferNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as? String
                val success = HotbarManager.loadBuffer(sender, name)
                if (success) {
                    sender.msg("${T.green}Buffer loaded.")
                } else {
                    sender.msg("${T.red}No buffer found.")
                }
            })
        })
    })
}
