package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.actions.AnchorActions
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.util.msg
import dev.cypdashuhn.extendedinventory.util.positionMsg
import dev.rooster.commands.*
import dev.rooster.commands.types.*
import org.bukkit.entity.Player

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
                player
                    .msg(
                        "${T.green}Jumped to anchor '${T.white}$target${T.green}' at (${anchor.x}, ${anchor.y})."
                    )
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
            player
                .msg(
                    "${T.green}Mode set to ${T.white}anchored${T.green}. Navigation locked, hotbar items locked."
                )
        }
        literal("free").onExecute {
            HotbarManager.setAnchored(player, false)
            player
                .msg(
                    "${T.green}Mode set to ${T.white}free${T.green}. Navigation and hotbar unlocked."
                )
        }
    }.onExecute {
        val state = HotbarManager.getState(player)
        player
            .msg(
                "${T.green}Anchored: ${T.white}${state.anchored}${T.green}, Mode: ${T.white}${state.mode}"
            )
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
    literal("cycle") {
        literal("next").onExecute { cycleTo(player, 1) }
        literal("prev").onExecute { cycleTo(player, -1) }
    }.onExecute { cycleTo(player, 1) }

private fun cycleTo(player: Player, direction: Int) {
    val held = player.inventory.itemInMainHand
    if (held.type.isAir) {
        player.msg("${T.red}No material to cycle: hold an item.")
        return
    }

    HotbarManager.ensureProfile(player)
    val target = HotbarManager.cycle(player, held.type.name, direction)
    if (target == null) {
        player.msg("${T.red}No other positions with this material.")
        return
    }

    val dir = if (direction >= 0) "next" else "previous"
    player
        .msg(
            "${T.green}Cycled to $dir position: (${T.white}${target.first}${T.green}, ${T.white}${target.second}${T.green})"
        )
}
