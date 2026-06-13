package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.actions.AnchorActions
import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.util.msg
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

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

internal fun buildAnchorNodes() = la("anchor").apply {
    then(la("add").apply {
        then(StringArgument("name").apply {
            then(IntegerArgument("x").apply {
                then(IntegerArgument("y").apply {
                    executesPlayer(PlayerCommandExecutor { sender, args ->
                        val name = args.get("name") as String
                        val x = args.get("x") as Int
                        val y = args.get("y") as Int
                        val profileId = HotbarManager.ensureProfile(sender)
                        AnchorActions.addAnchor(profileId, name, x, y)
                        sender.msg("${T.green}Anchor '${T.white}$name${T.green}' added at ($x, $y).")
                    })
                })
            })
        })
    })
    then(la("delete").apply {
        then(StringArgument("name").suggestAnchorNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profileId = HotbarManager.ensureProfile(sender)
                val success = AnchorActions.deleteAnchor(sender, profileId, name)
                if (success) {
                    sender.msg("${T.green}Anchor '${T.white}$name${T.green}' deleted.")
                } else {
                    sender.msg("${T.red}Cannot delete anchor '${T.white}$name${T.red}'.")
                }
            })
        })
    })
    then(la("rename").apply {
        then(StringArgument("oldName").suggestAnchorNames().apply {
            then(StringArgument("newName").apply {
                executesPlayer(PlayerCommandExecutor { sender, args ->
                    val oldName = args.get("oldName") as String
                    val newName = args.get("newName") as String
                    val profileId = HotbarManager.ensureProfile(sender)
                    val success = AnchorActions.renameAnchor(sender, profileId, oldName, newName)
                    if (success) {
                        sender.msg("${T.green}Anchor renamed to '${T.white}$newName${T.green}'.")
                    } else {
                        sender.msg("${T.red}Cannot rename anchor.")
                    }
                })
            })
        })
    })
    then(la("info").apply {
        then(StringArgument("name").suggestAnchorNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profileId = HotbarManager.ensureProfile(sender)
                val anchor = AnchorActions.getAnchorInfo(profileId, name)
                if (anchor != null) {
                    sender.msg("${T.green}Anchor '${T.white}${anchor.name}${T.green}': position (${anchor.x}, ${anchor.y})")
                } else {
                    sender.msg("${T.red}Anchor '${T.white}$name${T.red}' not found.")
                }
            })
        })
    })
}
