package dev.cypdashuhn.extendedinventory.ui.inventory

import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollContext
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.inventory.ItemStack

typealias IIC = InventoryInterfaceContext

enum class InterfaceMode(
    val suffix: String = "",
    val isAnchorMode: Boolean = false,
    val isGroupMode: Boolean = false,
) {
    NORMAL,
    EDITING(suffix = " <yellow>[Editing]"),
    SETTING_ANCHOR(suffix = " <aqua>[Set Anchor]", isAnchorMode = true),
    MATERIALIZING_ANCHOR(suffix = " <light_purple>[Materialize Anchor]", isAnchorMode = true),
    GROUP_DELETE_A(suffix = " <dark_red>[Delete: pick corner A]", isGroupMode = true),
    GROUP_DELETE_B(suffix = " <dark_red>[Delete: pick corner B]", isGroupMode = true),
    GROUP_MOVE_A(suffix = " <green>[Move: pick corner A]", isGroupMode = true),
    GROUP_MOVE_B(suffix = " <green>[Move: pick corner B]", isGroupMode = true),
    GROUP_MOVE_TARGET(suffix = " <green>[Move: pick target]", isGroupMode = true),
}

enum class BarSection {
    DEFAULT,
    GROUPS,
    ANCHORS,
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
    var section: BarSection = BarSection.DEFAULT,
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

internal fun inventoryItem(): InterfaceItem<IIC> = InterfaceItem(IIC::class)
