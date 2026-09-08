package dev.cypdashuhn.extendedinventory.ui.inventory

internal const val BOTTOM_BAR_START = 45

private const val GRID_COLUMNS = 9
private const val GRID_CENTER_COLUMN = 4
private const val GRID_CENTER_ROW = 2

internal fun IIC.worldAt(col: Int, row: Int): Pair<Int, Int> =
    centerX + (col - GRID_CENTER_COLUMN) to centerY + (row - GRID_CENTER_ROW)

internal fun IIC.slotToGrid(slot: Int): Pair<Int, Int> =
    worldAt(slot % GRID_COLUMNS, slot / GRID_COLUMNS)

internal fun IIC.contentSlotToGrid(slot: Int): Pair<Int, Int> =
    worldAt(slot % GRID_COLUMNS, slot / GRID_COLUMNS + position)

internal fun IIC.contentIdToGrid(id: Int): Pair<Int, Int> =
    worldAt(id.mod(GRID_COLUMNS), id.floorDiv(GRID_COLUMNS))
