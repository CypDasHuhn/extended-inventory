package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.cypdashuhn.extendedinventory.actions.InventoryActions
import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.db.ItemManager
import dev.cypdashuhn.extendedinventory.db.SlotCache
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.ui.ChatInputManager
import dev.cypdashuhn.extendedinventory.ui.anchor.AnchorListContext
import dev.cypdashuhn.extendedinventory.ui.anchor.AnchorListInterface
import dev.cypdashuhn.extendedinventory.util.mm
import dev.cypdashuhn.extendedinventory.util.T
import dev.cypdashuhn.extendedinventory.ui.profile.ProfileInterfaceContext
import dev.cypdashuhn.extendedinventory.ui.profile.ProfileInterface
import dev.cypdashuhn.extendedinventory.util.region
import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.interfaces.InterfaceInfo
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollContext
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollInterface
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollInterfaceOptions
import dev.rooster.ui.interfaces.constructors.indexed_content.sizeFromRows
import dev.rooster.ui.interfaces.handler
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

enum class InterfaceMode {
    NORMAL, EDITING, SETTING_ANCHOR, MATERIALIZING_ANCHOR,
    GROUP_DELETE_A, GROUP_DELETE_B,
    GROUP_MOVE_A, GROUP_MOVE_B, GROUP_MOVE_TARGET,
}

class InventoryInterfaceContext(
    var profileId: Int,
    var centerX: Int = 0,
    var centerY: Int = 0,
    var mode: InterfaceMode = InterfaceMode.NORMAL,
    var pendingChanges: MutableMap<Pair<Int, Int>, ItemStack?> = mutableMapOf(),
    var cornerA: Pair<Int, Int>? = null,
    var cornerB: Pair<Int, Int>? = null,
    var targetCorner: Pair<Int, Int>? = null,
    var targetPreviewPositions: Set<Pair<Int, Int>> = emptySet(),
    var groupDeleteConfirmed: Boolean = false,
    var groupMoveConfirmed: Boolean = false,
) : ScrollContext()

data class GridSlotData(
    val x: Int,
    val y: Int,
    val item: ItemStack?,
    val isAnchor: Boolean = false,
    val anchorName: String? = null,
)

object InventoryInterface : ScrollInterface<InventoryInterfaceContext, GridSlotData>(
    handler { InventoryInterfaceContext(0) },
    ScrollInterfaceOptions<InventoryInterfaceContext>().apply {
        inventoryTitle = { _, ctx ->
            val suffix = when (ctx.mode) {
                InterfaceMode.GROUP_DELETE_A -> " <dark_red>[Delete: pick corner A]"
                InterfaceMode.GROUP_DELETE_B -> " <dark_red>[Delete: pick corner B]"
                InterfaceMode.GROUP_MOVE_A -> " <green>[Move: pick corner A]"
                InterfaceMode.GROUP_MOVE_B -> " <green>[Move: pick corner B]"
                InterfaceMode.GROUP_MOVE_TARGET -> " <green>[Move: pick target]"
                InterfaceMode.EDITING -> " <yellow>[Editing]"
                InterfaceMode.SETTING_ANCHOR -> " <aqua>[Set Anchor]"
                InterfaceMode.MATERIALIZING_ANCHOR -> " <light_purple>[Materialize Anchor]"
                else -> ""
            }
            mm("<white><bold>Extended Inventory$suffix")
        }
        sizeFromRows(6)
    },
) {
    private fun slotToGrid(ctx: InventoryInterfaceContext, slot: Int): Pair<Int, Int> {
        val col = slot % 9
        val row = slot / 9
        return ctx.centerX + (col - 4) to ctx.centerY + (row - 2)
    }

    private fun isGroupPickMode(mode: InterfaceMode): Boolean = mode in setOf(
        InterfaceMode.GROUP_DELETE_A, InterfaceMode.GROUP_DELETE_B,
        InterfaceMode.GROUP_MOVE_A, InterfaceMode.GROUP_MOVE_B,
        InterfaceMode.GROUP_MOVE_TARGET,
    )

    private fun inGroupMode(ctx: InventoryInterfaceContext): Boolean = isGroupPickMode(ctx.mode)

    private fun cornersSet(ctx: InventoryInterfaceContext): Boolean =
        ctx.cornerA != null && ctx.cornerB != null && !isGroupPickMode(ctx.mode)

    private fun targetSet(ctx: InventoryInterfaceContext): Boolean =
        cornersSet(ctx) && ctx.targetCorner != null

    override fun contentProvider(id: Int, context: InventoryInterfaceContext): GridSlotData? {
        val (gridX, gridY) = slotToGrid(context, id)
        val pos = gridX to gridY

        if (context.pendingChanges.containsKey(pos)) {
            val pending = context.pendingChanges[pos]
            return GridSlotData(gridX, gridY, pending?.takeUnless { it.type.isAir })
        }

        val slot = SlotCache.getSlot(context.profileId, gridX, gridY)
        if (slot == null) {
            return GridSlotData(gridX, gridY, null)
        }

        if (slot.anchorId != null) {
            val anchor = AnchorManager.findById(slot.anchorId)
            val anchorItem = HotbarManager.createAnchorItem(anchor?.name ?: "???", anchor?.x ?: gridX, anchor?.y ?: gridY)
            return GridSlotData(gridX, gridY, anchorItem, isAnchor = true, anchorName = anchor?.name)
        }

        if (slot.itemId != null) {
            return GridSlotData(gridX, gridY, ItemManager.getItem(slot.itemId))
        }

        return GridSlotData(gridX, gridY, null)
    }

    override fun contentDisplay(data: GridSlotData, context: InventoryInterfaceContext): InterfaceInfo<InventoryInterfaceContext>.() -> ItemStack = {
        data.item ?: ItemStack(Material.AIR)
    }

    override fun contentClick(data: GridSlotData, context: InventoryInterfaceContext): ClickInfo<InventoryInterfaceContext>.() -> Unit = {
        when (context.mode) {
            InterfaceMode.NORMAL -> handleNormalClick(data, context)
            InterfaceMode.EDITING -> handleEditClick(data, context)
            InterfaceMode.SETTING_ANCHOR -> handleSetAnchorClick(data, context)
            InterfaceMode.MATERIALIZING_ANCHOR -> handleMaterializeClick(data, context)
            InterfaceMode.GROUP_DELETE_A, InterfaceMode.GROUP_DELETE_B -> handleDeleteCornerPick(data, context)
            InterfaceMode.GROUP_MOVE_A, InterfaceMode.GROUP_MOVE_B -> handleMoveCornerPick(data, context)
            InterfaceMode.GROUP_MOVE_TARGET -> handleMoveTargetPick(data, context)
        }
    }

    private fun ClickInfo<InventoryInterfaceContext>.handleNormalClick(data: GridSlotData, context: InventoryInterfaceContext) {
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

    private fun ClickInfo<InventoryInterfaceContext>.handleEditClick(data: GridSlotData, context: InventoryInterfaceContext) {
        val pos = data.x to data.y
        val cursorItem = click.player.itemOnCursor.takeUnless { it.type.isAir }
        val currentItem = data.item

        if (cursorItem != null) {
            context.pendingChanges[pos] = cursorItem.clone()
            click.player.setItemOnCursor(currentItem?.clone() ?: ItemStack.empty())
        } else if (currentItem != null) {
            context.pendingChanges[pos] = null
            click.player.setItemOnCursor(currentItem.clone())
        }

        openInventory(click.player, context)
    }

    private fun ClickInfo<InventoryInterfaceContext>.handleSetAnchorClick(data: GridSlotData, context: InventoryInterfaceContext) {
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

    private fun ClickInfo<InventoryInterfaceContext>.handleMaterializeClick(data: GridSlotData, context: InventoryInterfaceContext) {
        val anchorItem = HotbarManager.createAnchorItem("anchor_${data.x}_${data.y}", data.x, data.y)
        click.player.inventory.addItem(anchorItem)
        context.mode = InterfaceMode.NORMAL
        InventoryInterface.openInventory(click.player, context)
    }

    private fun ClickInfo<InventoryInterfaceContext>.handleDeleteCornerPick(data: GridSlotData, context: InventoryInterfaceContext) {
        if (context.cornerA == null) {
            context.cornerA = data.x to data.y
            context.mode = InterfaceMode.GROUP_DELETE_B
        } else {
            context.cornerB = data.x to data.y
            context.mode = InterfaceMode.NORMAL
        }
        InventoryInterface.openInventory(click.player, context)
    }

    private fun ClickInfo<InventoryInterfaceContext>.handleMoveCornerPick(data: GridSlotData, context: InventoryInterfaceContext) {
        if (context.cornerA == null) {
            context.cornerA = data.x to data.y
            context.mode = InterfaceMode.GROUP_MOVE_B
        } else {
            context.cornerB = data.x to data.y
            context.mode = InterfaceMode.GROUP_MOVE_TARGET
        }
        InventoryInterface.openInventory(click.player, context)
    }

    private fun ClickInfo<InventoryInterfaceContext>.handleMoveTargetPick(data: GridSlotData, context: InventoryInterfaceContext) {
        context.targetCorner = data.x to data.y
        context.targetPreviewPositions = computeTargetPreview(context)
        context.mode = InterfaceMode.NORMAL
        InventoryInterface.openInventory(click.player, context)
    }

    private fun computeTargetPreview(ctx: InventoryInterfaceContext): Set<Pair<Int, Int>> {
        val a = ctx.cornerA ?: return emptySet()
        val b = ctx.cornerB ?: return emptySet()
        val t = ctx.targetCorner ?: return emptySet()
        val r = region(a.first, a.second, b.first, b.second)
        return (r.minX..r.maxX).flatMap { sx -> (r.minY..r.maxY).map { sy -> t.first + (sx - r.minX) to t.second + (sy - r.minY) } }.toSet()
    }

    override fun getInterfaceItems(): List<InterfaceItem<InventoryInterfaceContext>> = listOf(
        backItem(),
        groupDeleteItem(),
        groupMoveItem(),
        editModeItem(),
        saveEditsItem(),
        discardEditsItem(),
        cancelGroupItem(),
        setAnchorItem(),
        materializeAnchorItem(),
        executeDeleteConfirmItem(),
        executeDeleteFinalItem(),
        executeMoveConfirmItem(),
        executeMoveFinalItem(),
        anchorListItem(),
        profileListItem(),
        cornerAItem(),
        cornerBItem(),
        targetPreviewItem(),
    )

    private fun backItem() = item()
        .atSlot(6, 1)
        .displayAs(createItem(Material.BARRIER, mm("<red>Back"), listOf(mm("<gray>Close the interface."))))
        .onClick { click.player.closeInventory() }

    private fun groupDeleteItem() = item()
        .atSlot(6, 2)
        .usedWhen { context.mode == InterfaceMode.NORMAL && !inGroupMode(context) && !cornersSet(context) }
        .displayAs(createItem(Material.LAVA_BUCKET, mm("<red>Group Delete"), listOf(
            mm("<gray>Select two corners to delete"),
            mm("<gray>all items in the region."),
        )))
        .onClick {
            clearGroup(context)
            context.mode = InterfaceMode.GROUP_DELETE_A
            InventoryInterface.openInventory(click.player, context)
        }

    private fun groupMoveItem() = item()
        .atSlot(6, 3)
        .usedWhen { context.mode == InterfaceMode.NORMAL && !inGroupMode(context) && !cornersSet(context) && !targetSet(context) }
        .displayAs(createItem(Material.PISTON, mm("<green>Group Move"), listOf(
            mm("<gray>Select two corners for source,"),
            mm("<gray>then a third for target."),
        )))
        .onClick {
            clearGroup(context)
            context.mode = InterfaceMode.GROUP_MOVE_A
            InventoryInterface.openInventory(click.player, context)
        }

    private fun editModeItem() = item()
        .atSlot(6, 4)
        .usedWhen { context.mode == InterfaceMode.NORMAL && !inGroupMode(context) && !cornersSet(context) && !targetSet(context) }
        .displayAs(createItem(Material.BOOK, mm("<white>Edit Mode"), listOf(mm("<gray>Click to edit inventory slots."))))
        .onClick {
            context.mode = InterfaceMode.EDITING
            InventoryInterface.openInventory(click.player, context)
        }

    private fun saveEditsItem() = item()
        .atSlot(6, 3)
        .usedWhen { context.mode == InterfaceMode.EDITING }
        .displayAs(createItem(Material.WRITABLE_BOOK, mm("<green>Save"), listOf(mm("<gray>Save changes and exit edit mode."))))
        .onClick {
            savePendingChanges(click.player, context)
            context.mode = InterfaceMode.NORMAL
            openInventory(click.player, context)
        }

    private fun discardEditsItem() = item()
        .atSlot(6, 4)
        .usedWhen { context.mode == InterfaceMode.EDITING }
        .displayAs(createItem(Material.BARRIER, mm("<red>Discard"), listOf(mm("<gray>Discard changes and exit edit mode."))))
        .onClick {
            context.pendingChanges.clear()
            context.mode = InterfaceMode.NORMAL
            InventoryInterface.openInventory(click.player, context)
        }

    private fun cancelGroupItem() = item()
        .atSlot(6, 5)
        .usedWhen { inGroupMode(context) }
        .displayAs(createItem(Material.BARRIER, mm("<red>Cancel"), listOf(mm("<gray>Exit group mode."))))
        .onClick {
            clearGroup(context)
            context.mode = InterfaceMode.NORMAL
            InventoryInterface.openInventory(click.player, context)
        }

    private fun setAnchorItem() = item()
        .atSlot(6, 5)
        .usedWhen { context.mode == InterfaceMode.NORMAL && !inGroupMode(context) && !cornersSet(context) && !targetSet(context) }
        .displayAs(createItem(Material.ENDER_PEARL, mm("<white>Set Anchor"), listOf(mm("<gray>Click a slot to create an anchor."))))
        .onClick {
            context.mode = InterfaceMode.SETTING_ANCHOR
            InventoryInterface.openInventory(click.player, context)
        }

    private fun materializeAnchorItem() = item()
        .atSlot(6, 6)
        .usedWhen { context.mode == InterfaceMode.NORMAL && !inGroupMode(context) && !cornersSet(context) && !targetSet(context) }
        .displayAs(createItem(Material.ITEM_FRAME, mm("<white>Materialize Anchor"), listOf(mm("<gray>Get a materialized anchor item."))))
        .onClick {
            context.mode = InterfaceMode.MATERIALIZING_ANCHOR
            InventoryInterface.openInventory(click.player, context)
        }

    private fun executeDeleteConfirmItem() = item()
        .atSlot(6, 6)
        .priority(10)
        .usedWhen { cornersSet(context) && !targetSet(context) && !context.groupDeleteConfirmed }
        .displayAs(createItem(Material.LAVA_BUCKET, mm("<red><bold>DELETE REGION"), listOf(
            mm("<gray>Delete all items between corners."),
            mm("<red>Click again to confirm."),
        )))
        .onClick {
            context.groupDeleteConfirmed = true
            InventoryInterface.openInventory(click.player, context)
        }

    private fun executeDeleteFinalItem() = item()
        .atSlot(6, 6)
        .priority(11)
        .usedWhen { cornersSet(context) && !targetSet(context) && context.groupDeleteConfirmed }
        .displayAs(createItem(Material.LAVA_BUCKET, mm("<red><bold>CONFIRM DELETE"), listOf(
            mm("<gray>This cannot be undone!"),
            mm("<red><bold>Click to execute."),
        )))
        .onClick {
            val a = context.cornerA!!
            val b = context.cornerB!!
            InventoryActions.groupDelete(context.profileId, a.first, a.second, b.first, b.second)
            SlotCache.invalidateProfile(context.profileId)
            clearGroup(context)
            context.mode = InterfaceMode.NORMAL
            HotbarManager.mirrorToHotbar(click.player)
            InventoryInterface.openInventory(click.player, context)
        }

    private fun executeMoveConfirmItem() = item()
        .atSlot(6, 6)
        .priority(10)
        .usedWhen { targetSet(context) && !context.groupMoveConfirmed }
        .displayAs(createItem(Material.PISTON, mm("<green><bold>MOVE REGION"), listOf(
            mm("<gray>Move items to target region."),
            mm("<green>Click again to confirm."),
        )))
        .onClick {
            context.groupMoveConfirmed = true
            InventoryInterface.openInventory(click.player, context)
        }

    private fun executeMoveFinalItem() = item()
        .atSlot(6, 6)
        .priority(11)
        .usedWhen { targetSet(context) && context.groupMoveConfirmed }
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
            clearGroup(context)
            context.mode = InterfaceMode.NORMAL
            HotbarManager.mirrorToHotbar(click.player)
            InventoryInterface.openInventory(click.player, context)
        }

    private fun anchorListItem() = item()
        .atSlot(6, 7)
        .displayAs(createItem(Material.NAME_TAG, mm("<white>Anchors"), listOf(mm("<gray>View all anchors."))))
        .routeTo(AnchorListInterface) { AnchorListContext(context.profileId) }

    private fun profileListItem() = item()
        .atSlot(6, 0)
        .displayAs(createItem(Material.PLAYER_HEAD, mm("<white>Profiles"), listOf(mm("<gray>Manage profiles."))))
        .onClick { ProfileInterface.openRefreshed(click.player, ProfileInterfaceContext()) }

    private fun cornerAItem() = item()
        .atSlots(contentArea.allValidSlots())
        .priority(10)
        .usedWhen {
            val a = context.cornerA ?: return@usedWhen false
            val (gx, gy) = slotToGrid(context, slot)
            gx == a.first && gy == a.second
        }
        .displayAs {
            val a = context.cornerA!!
            createItem(Material.RED_STAINED_GLASS_PANE, mm("<red>Corner A"), listOf(mm("<gray>(${a.first}, ${a.second})")))
        }

    private fun cornerBItem() = item()
        .atSlots(contentArea.allValidSlots())
        .priority(10)
        .usedWhen {
            val b = context.cornerB ?: return@usedWhen false
            val (gx, gy) = slotToGrid(context, slot)
            gx == b.first && gy == b.second
        }
        .displayAs {
            val b = context.cornerB!!
            createItem(Material.BLUE_STAINED_GLASS_PANE, mm("<blue>Corner B"), listOf(mm("<gray>(${b.first}, ${b.second})")))
        }

    private fun targetPreviewItem() = item()
        .atSlots(contentArea.allValidSlots())
        .priority(9)
        .usedWhen {
            if (context.targetPreviewPositions.isEmpty()) return@usedWhen false
            val (gx, gy) = slotToGrid(context, slot)
            (gx to gy) in context.targetPreviewPositions
        }
        .displayAs {
            val (gx, gy) = slotToGrid(context, slot)
            createItem(Material.GREEN_STAINED_GLASS_PANE, mm("<green>Target"), listOf(mm("<gray>($gx, $gy)")))
        }

    private fun clearGroup(ctx: InventoryInterfaceContext) {
        ctx.cornerA = null
        ctx.cornerB = null
        ctx.targetCorner = null
        ctx.targetPreviewPositions = emptySet()
        ctx.groupDeleteConfirmed = false
        ctx.groupMoveConfirmed = false
    }

    private fun savePendingChanges(player: Player, context: InventoryInterfaceContext) {
        context.pendingChanges.forEach { (pos, item) ->
            InventoryActions.setItem(context.profileId, pos.first, pos.second, item)
        }
        context.pendingChanges.clear()
        SlotCache.invalidateProfile(context.profileId)
        HotbarManager.mirrorToHotbar(player)
    }
}
