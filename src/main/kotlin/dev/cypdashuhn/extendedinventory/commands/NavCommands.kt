package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.actions.AnchorActions
import dev.cypdashuhn.extendedinventory.actions.CycleActions
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.hotbar.HotbarMode
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.util.msg
import dev.cypdashuhn.extendedinventory.util.positionMsg
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor

internal fun buildJumpToNode() = la("jumpto").apply {
    then(IntegerArgument("x").apply {
        then(IntegerArgument("y").apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val x = args.get("x") as Int
                val y = args.get("y") as Int
                HotbarManager.jumpTo(sender, x, y)
                sender.msg("${T.green}Jumped to ($x, $y).")
            })
        })
    })
    then(StringArgument("anchorName").suggestAnchorNames().apply {
        executesPlayer(PlayerCommandExecutor { sender, args ->
            val target = args.get("anchorName") as String
            val profileId = HotbarManager.ensureProfile(sender)
            val anchor = AnchorActions.getAnchorInfo(profileId, target)
            if (anchor != null) {
                HotbarManager.jumpTo(sender, anchor.x, anchor.y)
                sender.msg("${T.green}Jumped to anchor '${T.white}$target${T.green}' at (${anchor.x}, ${anchor.y}).")
            } else {
                sender.msg("${T.red}Anchor '${T.white}$target${T.red}' not found.")
            }
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        sender.msg("${T.yellow}Usage: /ex jumpto <x> <y> or /ex jumpto <anchor-name>")
    })
}

internal fun buildCurrentPositionNode() = la("current-position").apply {
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        val state = HotbarManager.getState(sender)
        sender.msg(positionMsg(state.x, state.y))
    })
}

internal fun buildModeNode() = la("mode").apply {
    then(la("anchored").apply {
        executesPlayer(PlayerCommandExecutor { sender, _ ->
            val state = HotbarManager.getState(sender)
            state.anchored = true
            state.mode = HotbarMode.LOCKED
            sender.msg("${T.green}Mode set to ${T.white}anchored${T.green}. Navigation locked, hotbar items locked.")
        })
    })
    then(la("free").apply {
        executesPlayer(PlayerCommandExecutor { sender, _ ->
            val state = HotbarManager.getState(sender)
            state.anchored = false
            state.mode = HotbarMode.FREE
            sender.msg("${T.green}Mode set to ${T.white}free${T.green}. Navigation and hotbar unlocked.")
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        val state = HotbarManager.getState(sender)
        sender.msg("${T.green}Anchored: ${T.white}${state.anchored}${T.green}, Mode: ${T.white}${state.mode}")
    })
}

internal fun buildDirectionNode(name: String, dx: Int, dy: Int) = la(name).apply {
    then(IntegerArgument("amount").setOptional(true).apply {
        executesPlayer(PlayerCommandExecutor { sender, args ->
            val amount = (args.get("amount") as? Int) ?: 1
            repeat(amount) { HotbarManager.navigate(sender, dx, dy) }
            val state = HotbarManager.getState(sender)
            sender.msg(positionMsg(state.x, state.y))
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        HotbarManager.navigate(sender, dx, dy)
        val state = HotbarManager.getState(sender)
        sender.msg(positionMsg(state.x, state.y))
    })
}

internal fun buildCycleNode() = la("cycle").apply {
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        val state = HotbarManager.getState(sender)
        val profileId = state.profileId ?: return@PlayerCommandExecutor
        val positions = CycleActions.getCyclePositions(profileId, state.x, state.y)
        if (positions.isEmpty()) {
            sender.msg("${T.red}No other positions with this material.")
            return@PlayerCommandExecutor
        }
        val next = positions.first()
        HotbarManager.jumpTo(sender, next.first, next.second)
        sender.msg("${T.green}Cycled to next position: (${T.white}${next.first}${T.green}, ${T.white}${next.second}${T.green})")
    })
}
