package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterface
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterfaceContext
import dev.rooster.commands.*

fun CommandsScope.registerCommands() {
    command("ex") {
        onExecute {
            val state = HotbarManager.getState(player)
            val profileId = HotbarManager.ensureProfile(player)
            InventoryInterface.openInventory(player, InventoryInterfaceContext(profileId, state.x, state.y))
        }
        jumpTo()
        currentPosition()
        mode()
        direction("up", 0, -1)
        direction("down", 0, 1)
        direction("left", -1, 0)
        direction("right", 1, 0)
        cycle()
        profiles()
        buffer()
        anchors()
    }
}
