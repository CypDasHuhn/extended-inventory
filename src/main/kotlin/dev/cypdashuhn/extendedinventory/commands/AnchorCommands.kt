package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.actions.AnchorActions
import dev.cypdashuhn.extendedinventory.actions.RESOURCE_NAME_REGEX
import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.util.msg
import dev.rooster.commands.*
import dev.rooster.commands.types.*
import dev.rooster.commands.types.wrappers.*
import org.jetbrains.exposed.sql.transactions.transaction

fun <T : CanSuggest> T.suggestAnchorNames(): T =
    suggestStrings {
        val player = playerOrNull ?: return@suggestStrings emptyList()
        val profileId =
            HotbarManager.getState(player).profileId ?: return@suggestStrings emptyList()
        transaction {
            AnchorManager.allForProfile(profileId).map { it.name }
        }
    }

fun ChildrenScope.anchors() =
    literal("anchor") {
        literal("add") {
            string("name") {
                integer("x") {
                    integer("y").onExecute {
                        val name = arg<String>("name")
                        val x = arg<Int>("x")
                        val y = arg<Int>("y")
                        val profileId = HotbarManager.ensureProfile(player)
                        AnchorActions.addAnchor(profileId, name, x, y)
                        player
                            .msg("${T.green}Anchor '${T.white}$name${T.green}' added at ($x, $y).")
                    }
                }
            }.matches(RESOURCE_NAME_REGEX) { raw ->
                player
                    .msg("${T.red}Invalid anchor name '${T.white}$raw${T.red}'.")
            }
        }
        literal("delete") {
            string("name").suggestAnchorNames().onExecute {
                val name = arg<String>("name")
                val profileId = HotbarManager.ensureProfile(player)
                val success = AnchorActions.deleteAnchor(player, profileId, name)
                if (success) {
                    player.msg("${T.green}Anchor '${T.white}$name${T.green}' deleted.")
                } else {
                    player.msg("${T.red}Cannot delete anchor '${T.white}$name${T.red}'.")
                }
            }
        }
        literal("rename") {
            string("oldName") {
                string("newName")
                    .matches(RESOURCE_NAME_REGEX) { raw ->
                        player
                            .msg("${T.red}Invalid anchor name '${T.white}$raw${T.red}'.")
                    }.onExecute {
                        val oldName = arg<String>("oldName")
                        val newName = arg<String>("newName")
                        val profileId = HotbarManager.ensureProfile(player)
                        val success =
                            AnchorActions
                                .renameAnchor(player, profileId, oldName, newName)
                        if (success) {
                            player
                                .msg("${T.green}Anchor renamed to '${T.white}$newName${T.green}'.")
                        } else {
                            player.msg("${T.red}Cannot rename anchor.")
                        }
                    }
            }.suggestAnchorNames()
        }
        literal("info") {
            string("name").suggestAnchorNames().onExecute {
                val name = arg<String>("name")
                val profileId = HotbarManager.ensureProfile(player)
                val anchor = AnchorActions.getAnchorInfo(profileId, name)
                if (anchor != null) {
                    player
                        .msg(
                            "${T.green}Anchor '${T.white}${anchor.name}${T.green}': position (${anchor.x}, ${anchor.y})"
                        )
                } else {
                    player.msg("${T.red}Anchor '${T.white}$name${T.red}' not found.")
                }
            }
        }
    }
