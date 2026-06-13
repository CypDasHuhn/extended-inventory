package dev.cypdashuhn.extendedinventory.commands

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions

internal fun <T> Argument<T>.suggestPlayerNames(): Argument<T> =
    replaceSuggestions(ArgumentSuggestions.strings { _ ->
        org.bukkit.Bukkit.getOnlinePlayers().map { it.name }.toTypedArray()
    })
