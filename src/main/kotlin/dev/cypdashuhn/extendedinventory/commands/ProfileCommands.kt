package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.actions.ProfileActions
import dev.cypdashuhn.extendedinventory.actions.RESOURCE_NAME_REGEX
import dev.cypdashuhn.extendedinventory.db.PlayerProfileStatus
import dev.cypdashuhn.extendedinventory.db.ProfileManager
import dev.cypdashuhn.extendedinventory.db.ProfileOpenness
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.util.msg
import dev.cypdashuhn.extendedinventory.util.errorNotFound
import dev.rooster.commands.*
import dev.rooster.commands.types.*
import dev.rooster.commands.types.wrappers.*
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

fun <T : CanSuggest> T.suggestProfileNames(): T = suggestStrings {
    transaction {
        ProfileManager.all().map { it.name }
    }
}

fun ChildrenScope.profiles() = literal("profiles") {
    literal("switch") {
        string("name")
            .suggestProfileNames()
            .matches(RESOURCE_NAME_REGEX) { raw -> player.msg("${T.red}Invalid profile name '${T.white}$raw${T.red}'.") }
            .onExecute {
                val name = arg<String>("name")
                val profile = ProfileManager.findByName(name)
                if (profile != null) {
                    ProfileActions.switchProfile(player, profile.id.value)
                    HotbarManager.getState(player).profileId = profile.id.value
                    HotbarManager.mirrorToHotbar(player)
                    player.msg("${T.green}Switched to profile '${T.white}$name${T.green}'.")
                } else {
                    val id = ProfileActions.createProfile(player, name)
                    HotbarManager.getState(player).profileId = id
                    HotbarManager.mirrorToHotbar(player)
                    player.msg("${T.green}Created and switched to profile '${T.white}$name${T.green}'.")
                }
            }
    }
    literal("set-default") {
        string("name").suggestProfileNames().onExecute {
            val name = arg<String>("name")
            val profile = ProfileManager.findByName(name)
            if (profile != null) {
                ProfileActions.setDefault(player, profile.id.value)
                player.msg("${T.green}Set '${T.white}$name${T.green}' as default profile.")
            } else {
                player.msg(errorNotFound("Profile", name))
            }
        }
    }
    literal("subscribe") {
        string("name").suggestProfileNames().onExecute {
            val name = arg<String>("name")
            val profile = ProfileManager.findByName(name)
            if (profile != null) {
                ProfileActions.subscribe(player, profile.id.value)
                player.msg("${T.green}Subscribed to '${T.white}$name${T.green}'.")
            } else {
                player.msg(errorNotFound("Profile", name))
            }
        }
    }
    literal("invite") {
        string("profileName") {
            player("targetPlayer") {
                literal("read-only").onExecute { handleInvite(PlayerProfileStatus.READ_ONLY) }
                literal("full").onExecute { handleInvite(PlayerProfileStatus.WRITE_READ) }
            }
        }.suggestProfileNames()
    }
    literal("settings") {
        string("name") {
            literal("turn-private").onExecute { handleSettings(ProfileOpenness.PRIVATE) }
            literal("turn-public-read").onExecute { handleSettings(ProfileOpenness.PUBLIC_READ) }
            literal("turn-public-write").onExecute { handleSettings(ProfileOpenness.PUBLIC_WRITE) }
        }.suggestProfileNames()
    }
}.onExecute {
    player.msg("${T.yellow}Usage: /ex profiles <switch|set-default|subscribe|invite|settings>")
}

private fun Context.handleInvite(status: PlayerProfileStatus) {
    val profileName = arg<String>("profileName")
    val targetPlayer = arg<Player>("targetPlayer")
    val profile = ProfileManager.findByName(profileName) ?: run {
        player.msg("${T.red}Profile not found.")
        return
    }
    ProfileActions.invite(player, profile.id.value, targetPlayer, status)
    val accessLabel = if (status == PlayerProfileStatus.READ_ONLY) "read-only" else "full"
    player.msg("${T.green}Invited ${targetPlayer.name} to '${T.white}$profileName${T.green}' with $accessLabel access.")
}

private fun Context.handleSettings(openness: ProfileOpenness) {
    val name = arg<String>("name")
    val profile = ProfileManager.findByName(name) ?: run {
        player.msg("${T.red}Profile not found.")
        return
    }
    ProfileActions.setOpenness(profile.id.value, openness)
    val label = when (openness) {
        ProfileOpenness.PRIVATE -> "private"
        ProfileOpenness.PUBLIC_READ -> "public (read-only)"
        ProfileOpenness.PUBLIC_WRITE -> "public (write access)"
    }
    player.msg("${T.green}Profile '${T.white}$name${T.green}' is now $label.")
}
