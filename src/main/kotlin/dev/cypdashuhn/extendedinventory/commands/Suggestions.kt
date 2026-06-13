package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.db.BufferManager
import dev.cypdashuhn.extendedinventory.db.ProfileManager
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

internal fun <T> Argument<T>.suggestProfileNames(): Argument<T> =
    replaceSuggestions(ArgumentSuggestions.strings { _ ->
        transaction {
            ProfileManager.all().map { it.name }.toTypedArray()
        }
    })

internal fun <T> Argument<T>.suggestAnchorNames(): Argument<T> =
    replaceSuggestions(ArgumentSuggestions.strings { info ->
        val player = info.sender() as? Player
        if (player != null) {
            val state = HotbarManager.getState(player)
            val profileId = state.profileId
            if (profileId != null) {
                transaction {
                    AnchorManager.allForProfile(profileId)
                        .map { it.name }.toTypedArray()
                }
            } else emptyArray()
        } else emptyArray()
    })

internal fun <T> Argument<T>.suggestBufferNames(): Argument<T> =
    replaceSuggestions(ArgumentSuggestions.strings { info ->
        val player = info.sender() as? Player ?: return@strings emptyArray()
        BufferManager.list(player).map { BufferManager.formatTimestamp(it.timestamp) }.toTypedArray()
    })

internal fun <T> Argument<T>.suggestPlayerNames(): Argument<T> =
    replaceSuggestions(ArgumentSuggestions.strings { _ ->
        org.bukkit.Bukkit.getOnlinePlayers().map { it.name }.toTypedArray()
    })
