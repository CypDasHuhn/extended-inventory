package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.actions.AnchorActions
import dev.cypdashuhn.extendedinventory.actions.CycleActions
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.util.msg
import dev.cypdashuhn.extendedinventory.util.positionMsg
import dev.rooster.commands.*
import dev.rooster.commands.types.*

fun ChildrenScope.jumpTo() =
    literal("jumpto") {
        integer("x") {
            integer("y").onExecute {
                val x = arg<Int>("x")
                val y = arg<Int>("y")
                HotbarManager.jumpTo(player, x, y)
                player.msg("${T.green}Jumped to ($x, $y).")
            }
        }
        string("anchorName").suggestAnchorNames().onExecute {
            val target = arg<String>("anchorName")
            val profileId = HotbarManager.ensureProfile(player)
            val anchor = AnchorActions.getAnchorInfo(profileId, target)
            if (anchor != null) {
                HotbarManager.jumpTo(player, anchor.x, anchor.y)
                player.msg("${T.green}Jumped to anchor '${T.white}$target${T.green}' at (${anchor.x}, ${anchor.y}).")
            } else {
                player.msg("${T.red}Anchor '${T.white}$target${T.red}' not found.")
            }
        }
    }.onExecute {
        player.msg("${T.yellow}Usage: /ex jumpto <x> <y> or /ex jumpto <anchor-name>")
    }

fun ChildrenScope.currentPosition() =
    literal("current-position").onExecute {
        val state = HotbarManager.getState(player)
        player.msg(positionMsg(state.x, state.y))
    }

fun ChildrenScope.mode() =
    literal("mode") {
        literal("anchored").onExecute {
            HotbarManager.setAnchored(player, true)
            player.msg("${T.green}Mode set to ${T.white}anchored${T.green}. Navigation locked, hotbar items locked.")
        }
        literal("free").onExecute {
            HotbarManager.setAnchored(player, false)
            player.msg("${T.green}Mode set to ${T.white}free${T.green}. Navigation and hotbar unlocked.")
        }
    }.onExecute {
        val state = HotbarManager.getState(player)
        player.msg("${T.green}Anchored: ${T.white}${state.anchored}${T.green}, Mode: ${T.white}${state.mode}")
    }

fun ChildrenScope.direction(name: String, dx: Int, dy: Int) =
    literal(name) {
        integer("amount", min = 1).optional().onExecute {
            val amount = argOrNull<Int>("amount") ?: 1
            repeat(amount) { HotbarManager.navigate(player, dx, dy) }
            val state = HotbarManager.getState(player)
            player.msg(positionMsg(state.x, state.y))
        }
    }.onExecute {
        HotbarManager.navigate(player, dx, dy)
        val state = HotbarManager.getState(player)
        player.msg(positionMsg(state.x, state.y))
    }

fun ChildrenScope.cycle() =
    literal("cycle").onExecute {
        val state = HotbarManager.getState(player)
        val profileId = state.profileId ?: return@onExecute

        val inventory = player.inventory
        val held = inventory.itemInMainHand
        val excludePos: Pair<Int, Int>
        val materialName: String?

        if (!held.type.isAir) {
            val heldCellX = state.x + inventory.heldItemSlot - HotbarManager.CENTER_SLOT
            excludePos = heldCellX to state.y
            materialName = held.type.name
        } else {
            excludePos = state.x to state.y
            materialName = CycleActions.materialAt(profileId, state.x, state.y)
        }

        if (materialName == null) {
            player.msg("${T.red}No material to cycle: hold an item or stand on a stored cell.")
            return@onExecute
        }

        val positions = CycleActions.positionsForMaterial(profileId, materialName, excludePos)
        if (positions.isEmpty()) {
            player.msg("${T.red}No other positions with this material.")
            return@onExecute
        }
        val next = positions.first()
        HotbarManager.jumpTo(player, next.first, next.second)
        player.msg("${T.green}Cycled to next position: (${T.white}${next.first}${T.green}, ${T.white}${next.second}${T.green})")
    }
