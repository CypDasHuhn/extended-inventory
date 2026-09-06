package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollContext
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.inventory.ItemStack

typealias IIC = InventoryInterfaceContext

enum class InterfaceMode {
    NORMAL,
    EDITING,
    SETTING_ANCHOR,
    MATERIALIZING_ANCHOR,
    GROUP_DELETE_A,
    GROUP_DELETE_B,
    GROUP_MOVE_A,
    GROUP_MOVE_B,
    GROUP_MOVE_TARGET,
}

class InventoryInterfaceContext(
    var profileId: Int,
    var centerX: Int = 0,
    var centerY: Int = 0,
    var mode: InterfaceMode = InterfaceMode.NORMAL,
    var pendingChanges: MutableMap<String, String> = mutableMapOf(),
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

internal val IIC.isIdle: Boolean
    get() = mode == InterfaceMode.NORMAL && cornerA == null && cornerB == null

internal val IIC.isGroupMode: Boolean
    get() = mode in GROUP_SELECT_MODES

private val GROUP_SELECT_MODES = setOf(
    InterfaceMode.GROUP_DELETE_A,
    InterfaceMode.GROUP_DELETE_B,
    InterfaceMode.GROUP_MOVE_A,
    InterfaceMode.GROUP_MOVE_B,
    InterfaceMode.GROUP_MOVE_TARGET,
)

internal fun inventoryItem(): InterfaceItem<IIC> =
    InterfaceItem(IIC::class)
