package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.db.SlotCache
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.ui.ChatInputManager
import dev.cypdashuhn.extendedinventory.util.mm
import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material

internal fun anchorActionItems(): List<InterfaceItem<IIC>> =
    listOf(
        inventoryItem()
            .atSlot(6, 5)
            .usedWhen { context.isIdle }
            .displayAs(createItem(Material.ENDER_PEARL, mm("<white>Set Anchor"), listOf(mm("<gray>Click a slot to create an anchor."))))
            .onClick {
                context.mode = InterfaceMode.SETTING_ANCHOR
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 6)
            .usedWhen { context.isIdle }
            .displayAs(createItem(Material.ITEM_FRAME, mm("<white>Materialize Anchor"), listOf(mm("<gray>Get a materialized anchor item."))))
            .onClick {
                context.mode = InterfaceMode.MATERIALIZING_ANCHOR
                InventoryInterface.openInventory(click.player, context)
            },
    )

internal fun ClickInfo<IIC>.setAnchorOnContent(data: GridSlotData) {
    InventoryInterface.openInventory(click.player, context)
    ChatInputManager.awaitInput(click.player, "<gray>Type the anchor <white>name<gray>:") { name ->
        if (name.isBlank()) return@awaitInput
        val trimmed = name.trim()
        val anchorId = AnchorManager.create(context.profileId, trimmed, data.x, data.y)
        SlotCache.setAnchor(context.profileId, data.x, data.y, anchorId)
        context.mode = InterfaceMode.NORMAL
        InventoryInterface.openInventory(click.player, context)
    }
}

internal fun ClickInfo<IIC>.materializeAnchorOnContent(data: GridSlotData) {
    val anchorItem = HotbarManager.createAnchorItem("anchor_${data.x}_${data.y}", data.x, data.y)
    click.player.inventory.addItem(anchorItem)
    context.mode = InterfaceMode.NORMAL
    InventoryInterface.openInventory(click.player, context)
}
