package dev.cypdashuhn.extendedinventory.db

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object SlotCache {
    private val rows: MutableMap<Int, MutableMap<Int, MutableMap<Int, InventoryManager.SlotData?>>> = mutableMapOf()

    fun getRow(profileId: Int, y: Int): Map<Int, InventoryManager.SlotData?> {
        val profileCache = rows.getOrPut(profileId) { mutableMapOf() }
        if (profileCache.containsKey(y)) {
            return profileCache[y]!!.toMap()
        }
        val dbRow: Map<Int, InventoryManager.SlotData?> = transaction {
            InventoryManager.InventorySlots.selectAll().where {
                (InventoryManager.InventorySlots.profileId eq profileId) and
                    (InventoryManager.InventorySlots.y eq y)
            }.associate {
                val x = it[InventoryManager.InventorySlots.x]
                x to InventoryManager.SlotData(
                    it[InventoryManager.InventorySlots.profileId],
                    x,
                    y,
                    it[InventoryManager.InventorySlots.itemId],
                    it[InventoryManager.InventorySlots.anchorId],
                )
            }
        }
        profileCache[y] = dbRow.toMutableMap()
        return dbRow
    }

    fun getSlot(profileId: Int, x: Int, y: Int): InventoryManager.SlotData? {
        return getRow(profileId, y)[x]
    }

    fun setItem(profileId: Int, x: Int, y: Int, itemId: Int?) {
        transaction {
            val existing = InventoryManager.InventorySlots.selectAll().where {
                (InventoryManager.InventorySlots.profileId eq profileId) and
                    (InventoryManager.InventorySlots.x eq x) and
                    (InventoryManager.InventorySlots.y eq y)
            }.firstOrNull()
            if (existing != null) {
                if (itemId != null) {
                    InventoryManager.InventorySlots.update({
                        (InventoryManager.InventorySlots.profileId eq profileId) and
                            (InventoryManager.InventorySlots.x eq x) and
                            (InventoryManager.InventorySlots.y eq y)
                    }) {
                        it[InventoryManager.InventorySlots.itemId] = itemId
                        it[InventoryManager.InventorySlots.anchorId] = null
                    }
                } else {
                    InventoryManager.InventorySlots.deleteWhere {
                        (InventoryManager.InventorySlots.profileId eq profileId) and
                            (InventoryManager.InventorySlots.x eq x) and
                            (InventoryManager.InventorySlots.y eq y)
                    }
                }
            } else if (itemId != null) {
                InventoryManager.InventorySlots.insert {
                    it[InventoryManager.InventorySlots.profileId] = profileId
                    it[InventoryManager.InventorySlots.x] = x
                    it[InventoryManager.InventorySlots.y] = y
                    it[InventoryManager.InventorySlots.itemId] = itemId
                }
            }
        }
        val profileCache = rows.getOrPut(profileId) { mutableMapOf() }
        val row = profileCache.getOrPut(y) { mutableMapOf() }
        if (itemId != null) {
            row[x] = InventoryManager.SlotData(profileId, x, y, itemId, null)
        } else {
            row.remove(x)
        }
    }

    fun removeSlot(profileId: Int, x: Int, y: Int) {
        transaction {
            InventoryManager.InventorySlots.deleteWhere {
                (InventoryManager.InventorySlots.profileId eq profileId) and
                    (InventoryManager.InventorySlots.x eq x) and
                    (InventoryManager.InventorySlots.y eq y)
            }
        }
        rows[profileId]?.get(y)?.remove(x)
    }

    fun setAnchor(profileId: Int, x: Int, y: Int, anchorId: Int) {
        transaction {
            val existing = InventoryManager.InventorySlots.selectAll().where {
                (InventoryManager.InventorySlots.profileId eq profileId) and
                    (InventoryManager.InventorySlots.x eq x) and
                    (InventoryManager.InventorySlots.y eq y)
            }.firstOrNull()
            if (existing != null) {
                InventoryManager.InventorySlots.update({
                    (InventoryManager.InventorySlots.profileId eq profileId) and
                        (InventoryManager.InventorySlots.x eq x) and
                        (InventoryManager.InventorySlots.y eq y)
                }) {
                    it[InventoryManager.InventorySlots.anchorId] = anchorId
                    it[InventoryManager.InventorySlots.itemId] = null
                }
            } else {
                InventoryManager.InventorySlots.insert {
                    it[InventoryManager.InventorySlots.profileId] = profileId
                    it[InventoryManager.InventorySlots.x] = x
                    it[InventoryManager.InventorySlots.y] = y
                    it[InventoryManager.InventorySlots.anchorId] = anchorId
                }
            }
        }
        val profileCache = rows.getOrPut(profileId) { mutableMapOf() }
        val row = profileCache.getOrPut(y) { mutableMapOf() }
        row[x] = InventoryManager.SlotData(profileId, x, y, null, anchorId)
    }

    fun batchRemove(profileId: Int, positions: Set<Pair<Int, Int>>) {
        if (positions.isEmpty()) return
        val ys = positions.map { it.second }.toSet()
        transaction {
            for (y in ys) {
                val xs = positions.filter { it.second == y }.map { it.first }.toSet()
                if (xs.isEmpty()) continue
                InventoryManager.InventorySlots.deleteWhere {
                    (InventoryManager.InventorySlots.profileId eq profileId) and
                        (InventoryManager.InventorySlots.y eq y) and
                        (InventoryManager.InventorySlots.x inList xs)
                }
            }
        }
        val profileCache = rows.getOrPut(profileId) { mutableMapOf() }
        for ((x, y) in positions) {
            profileCache.get(y)?.remove(x)
        }
    }

    fun batchSetItems(profileId: Int, entries: List<Triple<Int, Int, Int>>) {
        if (entries.isEmpty()) return
        transaction {
            for ((x, y, itemId) in entries) {
                val existing = InventoryManager.InventorySlots.selectAll().where {
                    (InventoryManager.InventorySlots.profileId eq profileId) and
                        (InventoryManager.InventorySlots.x eq x) and
                        (InventoryManager.InventorySlots.y eq y)
                }.firstOrNull()
                if (existing != null) {
                    InventoryManager.InventorySlots.update({
                        (InventoryManager.InventorySlots.profileId eq profileId) and
                            (InventoryManager.InventorySlots.x eq x) and
                            (InventoryManager.InventorySlots.y eq y)
                    }) {
                        it[InventoryManager.InventorySlots.itemId] = itemId
                        it[InventoryManager.InventorySlots.anchorId] = null
                    }
                } else {
                    InventoryManager.InventorySlots.insert {
                        it[InventoryManager.InventorySlots.profileId] = profileId
                        it[InventoryManager.InventorySlots.x] = x
                        it[InventoryManager.InventorySlots.y] = y
                        it[InventoryManager.InventorySlots.itemId] = itemId
                    }
                }
            }
        }
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
}
