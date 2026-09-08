package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.db.SlotCache
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.ui.ChatInputManager
import dev.cypdashuhn.extendedinventory.ui.anchor.AnchorListContext
import dev.cypdashuhn.extendedinventory.ui.anchor.AnchorListInterface
import dev.cypdashuhn.extendedinventory.util.mm
import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material

internal fun anchorActionItems(): List<InterfaceItem<IIC>> =
    listOf(
        inventoryItem()
            .atSlot(6, 2)
            .usedWhen { context.isIdle && context.section == BarSection.ANCHORS }
            .displayAs(
                createItem(
                    Material.ENDER_PEARL,
                    mm("<white>Set Anchor"),
                    listOf(mm("<gray>Click a slot to create an anchor."))
                )
            ).onClick {
                context.mode = InterfaceMode.SETTING_ANCHOR
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 3)
            .usedWhen { context.isIdle && context.section == BarSection.ANCHORS }
            .displayAs(
                createItem(
                    Material.ITEM_FRAME,
                    mm("<white>Materialize Anchor"),
                    listOf(mm("<gray>Get a materialized anchor item."))
                )
            ).onClick {
                context.mode = InterfaceMode.MATERIALIZING_ANCHOR
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 7)
            .usedWhen { context.section == BarSection.ANCHORS }
            .displayAs(
                createItem(
                    Material.NAME_TAG,
                    mm("<white>Anchors Overview"),
                    listOf(mm("<gray>View all anchors."))
                )
            ).routeTo(AnchorListInterface) { AnchorListContext(context.profileId) },
        inventoryItem()
            .atSlot(6, 4)
            .usedWhen {
                context.section == BarSection.ANCHORS && (context.isIdle || context.isAnchorMode)
            }
            .displayAs(
                createItem(
                    Material.ARROW,
                    mm("<white>Back"),
                    listOf(
                        mm("<gray>Return to the default bar."),
                        mm("<gray>Cancels any pending anchor action."),
                    )
                )
            ).onClick {
                context.mode = InterfaceMode.NORMAL
                context.section = BarSection.DEFAULT
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
