package dev.cypdashuhn.extendedinventory.db

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

private typealias RowCache = MutableMap<Int, InventoryManager.SlotData?>
private typealias ProfileCache = MutableMap<Int, RowCache>
private typealias CacheStore = MutableMap<Int, ProfileCache>

object SlotCache {
    private val rows: CacheStore = mutableMapOf()

    fun getRow(profileId: Int, y: Int): Map<Int, InventoryManager.SlotData?> {
        val profileCache = rows.getOrPut(profileId) { mutableMapOf() }
        if (profileCache.containsKey(y)) {
            return profileCache[y]!!.toMap()
        }
        val dbSlots = InventoryManager.getRow(profileId, y)
        val rowMap: RowCache = dbSlots.associate { it.x to it }.toMutableMap()
        profileCache[y] = rowMap
        return rowMap.toMap()
    }

    fun getSlot(profileId: Int, x: Int, y: Int): InventoryManager.SlotData? =
        getRow(profileId, y)[x]

    fun setItem(profileId: Int, x: Int, y: Int, itemId: Int?) {
        InventoryManager.setItem(profileId, x, y, itemId)
        val row = rows.getOrPut(profileId) { mutableMapOf() }.getOrPut(y) { mutableMapOf() }
        if (itemId != null) {
            row[x] = InventoryManager.SlotData(profileId, x, y, itemId, null)
        } else {
            row.remove(x)
        }
    }

    fun removeSlot(profileId: Int, x: Int, y: Int) {
        InventoryManager.removeSlot(profileId, x, y)
        rows[profileId]?.get(y)?.remove(x)
    }

    fun setAnchor(profileId: Int, x: Int, y: Int, anchorId: Int) {
        InventoryManager.setAnchor(profileId, x, y, anchorId)
        val row = rows.getOrPut(profileId) { mutableMapOf() }.getOrPut(y) { mutableMapOf() }
        row[x] = InventoryManager.SlotData(profileId, x, y, null, anchorId)
    }

    fun batchRemove(profileId: Int, positions: Set<Pair<Int, Int>>) {
        if (positions.isEmpty()) return
        InventoryManager.batchRemove(profileId, positions)
        val profileCache = rows.getOrPut(profileId) { mutableMapOf() }
        for ((x, y) in positions) {
            profileCache[y]?.remove(x)
        }
    }

    fun batchSetItems(profileId: Int, entries: List<Triple<Int, Int, Int>>) {
        if (entries.isEmpty()) return
        InventoryManager.batchSetItems(profileId, entries)
        val profileCache = rows.getOrPut(profileId) { mutableMapOf() }
        for ((x, y, itemId) in entries) {
            val row = profileCache.getOrPut(y) { mutableMapOf() }
            row[x] = InventoryManager.SlotData(profileId, x, y, itemId, null)
        }
    }

    fun invalidateProfile(profileId: Int) {
        rows.remove(profileId)
    }

    fun invalidateRow(profileId: Int, y: Int) {
        rows[profileId]?.remove(y)
    }

    fun clearAll() {
        rows.clear()
    }
}
