package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.actions.InventoryActions
import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.db.ItemManager
import dev.cypdashuhn.extendedinventory.db.SlotCache
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.mm
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.interfaces.InterfaceInfo
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollInterface
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollInterfaceOptions
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollerObject
import dev.rooster.ui.interfaces.constructors.indexed_content.sizeFromRows
import dev.rooster.ui.interfaces.handler
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

typealias Item = InterfaceItem<IIC>

val options =
    ScrollInterfaceOptions<IIC>().apply {
        scrollerObject = ScrollerObject.None()
        inventoryTitle = { _, ctx ->
            val suffix = ctx.mode.suffix
            mm(
                "<white><bold>Extended Inventory$suffix <dark_gray><bold>(${ctx.centerX}, ${ctx.centerY + ctx.position})"
            )
        }
        sizeFromRows(6)
    }

object InventoryInterface : ScrollInterface<IIC, GridSlotData>(
    handler { IIC(0) },
    options
) {
    override fun contentItem(): Item = super.contentItem().unlockedWhenEditing()

    override fun clickInAreaItem(): Item =
        super
            .clickInAreaItem()
            .unlockedWhenEditing()

    override fun contentProvider(id: Int, context: IIC): GridSlotData {
        val (gridX, gridY) = context.contentIdToGrid(id)
        val key = pendingKey(gridX, gridY)

        if (context.pendingChanges.containsKey(key)) {
            val pending = decodePendingItem(context.pendingChanges[key]!!)
            return GridSlotData(gridX, gridY, pending)
        }

        val slot = SlotCache.getSlot(context.profileId, gridX, gridY)
        if (slot == null) {
            return GridSlotData(gridX, gridY, null)
        }

        if (slot.anchorId != null) {
            val anchor = AnchorManager.findById(slot.anchorId)
            val anchorItem =
                HotbarManager.createAnchorItem(
                    anchor?.name ?: "???",
                    anchor?.x ?: gridX,
                    anchor?.y ?: gridY
                )
            return GridSlotData(
                gridX,
                gridY,
                anchorItem,
                isAnchor = true,
                anchorName = anchor?.name
            )
        }

        if (slot.itemId != null) {
            return GridSlotData(gridX, gridY, ItemManager.getItem(slot.itemId))
        }

        return GridSlotData(gridX, gridY, null)
    }

    override fun contentDisplay(
        data: GridSlotData,
        context: IIC
    ): InterfaceInfo<IIC>.() -> ItemStack = { data.item ?: ItemStack(Material.AIR) }

    override fun contentClick(data: GridSlotData, context: IIC): ClickInfo<IIC>.() -> Unit =
        {
            when (context.mode) {
                InterfaceMode.EDITING -> Unit
                InterfaceMode.NORMAL -> handleNormalClick(data)
                InterfaceMode.SETTING_ANCHOR -> setAnchorOnContent(data)
                InterfaceMode.MATERIALIZING_ANCHOR -> materializeAnchorOnContent(data)
                InterfaceMode.GROUP_DELETE_A, InterfaceMode.GROUP_DELETE_B -> pickDeleteCorner(data)
                InterfaceMode.GROUP_MOVE_A, InterfaceMode.GROUP_MOVE_B -> pickMoveCorner(data)
                InterfaceMode.GROUP_MOVE_TARGET -> pickMoveTarget(data)
            }
        }

    override fun getInterfaceItems(): List<Item> =
        listOf(
            chromeItems(),
            editSessionItems(),
            anchorActionItems(),
            groupOperationItems(),
            groupSelectionOverlayItems()
        ).flatten()

    private fun ClickInfo<IIC>.handleNormalClick(data: GridSlotData) {
        if (data.isAnchor && data.anchorName != null) {
            val anchor = AnchorManager.findByName(context.profileId, data.anchorName)
            if (anchor != null) {
                HotbarManager.jumpTo(click.player, anchor.x, anchor.y)
                context.centerX = anchor.x
                context.centerY = anchor.y
                context.position = 0
                InventoryInterface.openInventory(click.player, context)
                return
            }
        }
        if (data.item != null && !data.isAnchor) {
            InventoryActions.copyToCursor(click.player, context.profileId, data.x, data.y)
        }
    }
}

private fun Item.unlockedWhenEditing(): Item =
    unlockedWhen {
        context.mode == InterfaceMode.EDITING
    }
