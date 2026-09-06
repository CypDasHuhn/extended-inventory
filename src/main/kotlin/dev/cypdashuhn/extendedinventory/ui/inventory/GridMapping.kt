package dev.cypdashuhn.extendedinventory.ui.inventory

internal const val BOTTOM_BAR_START = 45

private const val GRID_COLUMNS = 9
private const val GRID_CENTER_COLUMN = 4
private const val GRID_CENTER_ROW = 2

internal fun InventoryInterfaceContext.worldAt(col: Int, row: Int): Pair<Int, Int> =
    centerX + (col - GRID_CENTER_COLUMN) to centerY + (row - GRID_CENTER_ROW)

internal fun InventoryInterfaceContext.slotToGrid(slot: Int): Pair<Int, Int> =
    worldAt(slot % GRID_COLUMNS, slot / GRID_COLUMNS)

internal fun InventoryInterfaceContext.contentSlotToGrid(slot: Int): Pair<Int, Int> =
    worldAt(slot % GRID_COLUMNS, slot / GRID_COLUMNS + position)

internal fun InventoryInterfaceContext.contentIdToGrid(id: Int): Pair<Int, Int> =
    worldAt(id % GRID_COLUMNS, id / GRID_COLUMNS)
