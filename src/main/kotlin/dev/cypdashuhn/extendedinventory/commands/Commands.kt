package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterface
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterfaceContext
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.executors.PlayerCommandExecutor

fun ex() {
    CommandTree("ex")
        .executesPlayer(PlayerCommandExecutor { sender, _ ->
            val state = HotbarManager.getState(sender)
            val profileId = HotbarManager.ensureProfile(sender)
            InventoryInterface.openInventory(sender, InventoryInterfaceContext(profileId, state.x, state.y))
        })
        .then(buildJumpToNode())
        .then(buildCurrentPositionNode())
        .then(buildModeNode())
        .then(buildDirectionNode("up", 0, -1))
        .then(buildDirectionNode("down", 0, 1))
        .then(buildDirectionNode("left", -1, 0))
        .then(buildDirectionNode("right", 1, 0))
        .then(buildCycleNode())
        .then(buildProfileNodes())
        .then(buildBufferNode())
        .then(buildAnchorNodes())
        .register()
}
