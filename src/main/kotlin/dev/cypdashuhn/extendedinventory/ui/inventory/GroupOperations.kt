package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.actions.InventoryActions
import dev.cypdashuhn.extendedinventory.db.SlotCache
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.util.mm
import dev.cypdashuhn.extendedinventory.util.region
import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material

internal fun groupOperationItems(): List<InterfaceItem<IIC>> =
    listOf(
        inventoryItem()
            .atSlot(6, 2)
            .usedWhen { context.isIdle }
            .displayAs(createItem(Material.LAVA_BUCKET, mm("<red>Group Delete"), listOf(
                mm("<gray>Select two corners to delete"),
                mm("<gray>all items in the region."),
            )))
            .onClick {
                context.clearGroupSelection()
                context.mode = InterfaceMode.GROUP_DELETE_A
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 3)
            .usedWhen { context.isIdle }
            .displayAs(createItem(Material.PISTON, mm("<green>Group Move"), listOf(
                mm("<gray>Select two corners for source,"),
                mm("<gray>then a third for target."),
            )))
            .onClick {
                context.clearGroupSelection()
                context.mode = InterfaceMode.GROUP_MOVE_A
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 5)
            .usedWhen { context.isGroupMode }
            .displayAs(createItem(Material.BARRIER, mm("<red>Cancel"), listOf(mm("<gray>Exit group mode."))))
            .onClick {
                context.clearGroupSelection()
                context.mode = InterfaceMode.NORMAL
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 6)
            .priority(10)
            .usedWhen { context.cornersSet && !context.targetSet && !context.groupDeleteConfirmed }
            .displayAs(createItem(Material.LAVA_BUCKET, mm("<red><bold>DELETE REGION"), listOf(
                mm("<gray>Delete all items between corners."),
                mm("<red>Click again to confirm."),
            )))
            .onClick {
                context.groupDeleteConfirmed = true
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 6)
            .priority(11)
            .usedWhen { context.cornersSet && !context.targetSet && context.groupDeleteConfirmed }
            .displayAs(createItem(Material.LAVA_BUCKET, mm("<red><bold>CONFIRM DELETE"), listOf(
                mm("<gray>This cannot be undone!"),
                mm("<red><bold>Click to execute."),
            )))
            .onClick {
                val a = context.cornerA!!
                val b = context.cornerB!!
                InventoryActions.groupDelete(context.profileId, a.first, a.second, b.first, b.second)
                SlotCache.invalidateProfile(context.profileId)
                context.clearGroupSelection()
                context.mode = InterfaceMode.NORMAL
                HotbarManager.mirrorToHotbar(click.player)
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 6)
            .priority(10)
            .usedWhen { context.targetSet && !context.groupMoveConfirmed }
            .displayAs(createItem(Material.PISTON, mm("<green><bold>MOVE REGION"), listOf(
                mm("<gray>Move items to target region."),
                mm("<green>Click again to confirm."),
            )))
            .onClick {
                context.groupMoveConfirmed = true
                InventoryInterface.openInventory(click.player, context)
            },
        inventoryItem()
            .atSlot(6, 6)
            .priority(11)
            .usedWhen { context.targetSet && context.groupMoveConfirmed }
            .displayAs(createItem(Material.PISTON, mm("<green><bold>CONFIRM MOVE"), listOf(
                mm("<gray>Move items to target."),
                mm("<green><bold>Click to execute."),
            )))
            .onClick {
                val a = context.cornerA!!
                val b = context.cornerB!!
                val t = context.targetCorner!!
                InventoryActions.groupMove(context.profileId, a.first, a.second, b.first, b.second, t.first, t.second)
                SlotCache.invalidateProfile(context.profileId)
                context.clearGroupSelection()
                context.mode = InterfaceMode.NORMAL
                HotbarManager.mirrorToHotbar(click.player)
                InventoryInterface.openInventory(click.player, context)
            },
    )

internal fun groupSelectionOverlayItems(): List<InterfaceItem<IIC>> =
    listOf(
        inventoryItem()
            .atSlots(InventoryInterface.contentArea.allValidSlots())
            .priority(10)
            .usedWhen {
                val a = context.cornerA ?: return@usedWhen false
                val (gx, gy) = context.slotToGrid(slot)
                gx == a.first && gy == a.second
            }.displayAs {
                val a = context.cornerA!!
                createItem(Material.RED_STAINED_GLASS_PANE, mm("<red>Corner A"), listOf(mm("<gray>(${a.first}, ${a.second})")))
            },
        inventoryItem()
            .atSlots(InventoryInterface.contentArea.allValidSlots())
            .priority(10)
            .usedWhen {
                val b = context.cornerB ?: return@usedWhen false
                val (gx, gy) = context.slotToGrid(slot)
                gx == b.first && gy == b.second
            }.displayAs {
                val b = context.cornerB!!
                createItem(Material.BLUE_STAINED_GLASS_PANE, mm("<blue>Corner B"), listOf(mm("<gray>(${b.first}, ${b.second})")))
            },
        inventoryItem()
            .atSlots(InventoryInterface.contentArea.allValidSlots())
            .priority(9)
            .usedWhen {
                if (context.targetPreviewPositions.isEmpty()) return@usedWhen false
                val (gx, gy) = context.slotToGrid(slot)
                (gx to gy) in context.targetPreviewPositions
            }.displayAs {
                val (gx, gy) = context.slotToGrid(slot)
                createItem(Material.GREEN_STAINED_GLASS_PANE, mm("<green>Target"), listOf(mm("<gray>($gx, $gy)")))
            },
    )

internal fun IIC.clearGroupSelection() {
    cornerA = null
    cornerB = null
    targetCorner = null
    targetPreviewPositions = emptySet()
    groupDeleteConfirmed = false
    groupMoveConfirmed = false
}

internal val IIC.cornersSet: Boolean
    get() = cornerA != null && cornerB != null && !isGroupMode

internal val IIC.targetSet: Boolean
    get() = cornersSet && targetCorner != null

internal fun IIC.computeTargetPreview(): Set<Pair<Int, Int>> {
    val a = cornerA ?: return emptySet()
    val b = cornerB ?: return emptySet()
    val t = targetCorner ?: return emptySet()
    val r = region(a.first, a.second, b.first, b.second)
    return r.positions.map { (x, y) -> t.first + (x - r.minX) to t.second + (y - r.minY) }.toSet()
}

internal fun ClickInfo<IIC>.pickDeleteCorner(data: GridSlotData) {
    if (context.cornerA == null) {
        context.cornerA = data.x to data.y
        context.mode = InterfaceMode.GROUP_DELETE_B
    } else {
        context.cornerB = data.x to data.y
        context.mode = InterfaceMode.NORMAL
    }
    InventoryInterface.openInventory(click.player, context)
}

internal fun ClickInfo<IIC>.pickMoveCorner(data: GridSlotData) {
    if (context.cornerA == null) {
        context.cornerA = data.x to data.y
        context.mode = InterfaceMode.GROUP_MOVE_B
    } else {
        context.cornerB = data.x to data.y
        context.mode = InterfaceMode.GROUP_MOVE_TARGET
    }
    InventoryInterface.openInventory(click.player, context)
}

internal fun ClickInfo<IIC>.pickMoveTarget(data: GridSlotData) {
    context.targetCorner = data.x to data.y
    context.targetPreviewPositions = context.computeTargetPreview()
    context.mode = InterfaceMode.NORMAL
    InventoryInterface.openInventory(click.player, context)
}
