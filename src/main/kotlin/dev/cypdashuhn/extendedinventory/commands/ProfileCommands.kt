package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.actions.ProfileActions
import dev.cypdashuhn.extendedinventory.actions.isValidResourceName
import dev.cypdashuhn.extendedinventory.db.PlayerProfileStatus
import dev.cypdashuhn.extendedinventory.db.ProfileManager
import dev.cypdashuhn.extendedinventory.db.ProfileOpenness
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.util.msg
import dev.cypdashuhn.extendedinventory.util.errorNotFound
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.EntitySelectorArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

internal fun <T> Argument<T>.suggestProfileNames(): Argument<T> =
    replaceSuggestions(ArgumentSuggestions.strings { _ ->
        transaction {
            ProfileManager.all().map { it.name }.toTypedArray()
        }
    })

internal fun buildProfileNodes() = la("profiles").apply {
    then(la("switch").apply {
        then(StringArgument("name").suggestProfileNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profile = ProfileManager.findByName(name)
                if (profile != null) {
                    ProfileActions.switchProfile(sender, profile.id.value)
                    HotbarManager.getState(sender).profileId = profile.id.value
                    HotbarManager.mirrorToHotbar(sender)
                    sender.msg("${T.green}Switched to profile '${T.white}$name${T.green}'.")
                } else {
                    if (isValidResourceName(name)) {
                        val id = ProfileActions.createProfile(sender, name)
                        HotbarManager.getState(sender).profileId = id
                        HotbarManager.mirrorToHotbar(sender)
                        sender.msg("${T.green}Created and switched to profile '${T.white}$name${T.green}'.")
                    } else {
                        sender.msg("${T.red}Invalid profile name.")
                    }
                }
            })
        })
    })
    then(la("set-default").apply {
        then(StringArgument("name").suggestProfileNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profile = ProfileManager.findByName(name)
                if (profile != null) {
                    ProfileActions.setDefault(sender, profile.id.value)
                    sender.msg("${T.green}Set '${T.white}$name${T.green}' as default profile.")
                } else {
                    sender.msg(errorNotFound("Profile", name))
                }
            })
        })
    })
    then(la("subscribe").apply {
        then(StringArgument("name").suggestProfileNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profile = ProfileManager.findByName(name)
                if (profile != null) {
                    ProfileActions.subscribe(sender, profile.id.value)
                    sender.msg("${T.green}Subscribed to '${T.white}$name${T.green}'.")
                } else {
                    sender.msg(errorNotFound("Profile", name))
                }
            })
        })
    })
    then(la("invite").apply {
        then(StringArgument("profileName").suggestProfileNames().apply {
            then(EntitySelectorArgument.OnePlayer("targetPlayer").apply {
                then(la("read-only").apply {
                    executesPlayer(PlayerCommandExecutor { sender, args ->
                        handleInvite(sender, args, PlayerProfileStatus.READ_ONLY)
                    })
                })
                then(la("full").apply {
                    executesPlayer(PlayerCommandExecutor { sender, args ->
                        handleInvite(sender, args, PlayerProfileStatus.WRITE_READ)
                    })
                })
            })
        })
    })
    then(la("settings").apply {
        then(StringArgument("name").suggestProfileNames().apply {
            then(la("turn-private").apply {
                executesPlayer(PlayerCommandExecutor { sender, args ->
                    handleSettings(sender, args, ProfileOpenness.PRIVATE)
                })
            })
            then(la("turn-public-read").apply {
                executesPlayer(PlayerCommandExecutor { sender, args ->
                    handleSettings(sender, args, ProfileOpenness.PUBLIC_READ)
                })
            })
            then(la("turn-public-write").apply {
                executesPlayer(PlayerCommandExecutor { sender, args ->
                    handleSettings(sender, args, ProfileOpenness.PUBLIC_WRITE)
                })
            })
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        sender.msg("${T.yellow}Usage: /ex profiles <switch|set-default|subscribe|invite|settings>")
    })
}

private fun handleInvite(sender: Player, args: CommandArguments, status: PlayerProfileStatus) {
    val profileName = args.get("profileName") as String
    val targetPlayer = args.get("targetPlayer") as Player
    val profile = ProfileManager.findByName(profileName) ?: run {
        sender.msg("${T.red}Profile not found.")
        return
    }
    ProfileActions.invite(sender, profile.id.value, targetPlayer, status)
    val accessLabel = if (status == PlayerProfileStatus.READ_ONLY) "read-only" else "full"
    sender.msg("${T.green}Invited ${targetPlayer.name} to '${T.white}$profileName${T.green}' with $accessLabel access.")
}

private fun handleSettings(sender: Player, args: CommandArguments, openness: ProfileOpenness) {
    val name = args.get("name") as String
    val profile = ProfileManager.findByName(name) ?: run {
        sender.msg("${T.red}Profile not found.")
        return
    }
    ProfileActions.setOpenness(profile.id.value, openness)
    val label = when (openness) {
        ProfileOpenness.PRIVATE -> "private"
        ProfileOpenness.PUBLIC_READ -> "public (read-only)"
        ProfileOpenness.PUBLIC_WRITE -> "public (write access)"
    }
    sender.msg("${T.green}Profile '${T.white}$name${T.green}' is now $label.")
}
