package dev.cypdashuhn.extendedinventory.db

import dev.cypdashuhn.extendedinventory.util.region
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object InventoryManager {
    object InventorySlots : IntIdTable("ei_inventory") {
        val profileId = integer("profile_id")
        val x = integer("x")
        val y = integer("y")
        val itemId = integer("item_id").nullable()
        val anchorId = integer("anchor_id").nullable()
    }

    data class SlotData(
        val profileId: Int,
        val x: Int,
        val y: Int,
        val itemId: Int?,
        val anchorId: Int?,
    )

    fun setItem(profileId: Int, x: Int, y: Int, itemId: Int?) = transaction {
        val existing = InventorySlots.selectAll().where {
            (InventorySlots.profileId eq profileId) and (InventorySlots.x eq x) and (InventorySlots.y eq y)
        }.firstOrNull()
        if (existing != null) {
            if (itemId != null) {
                InventorySlots.update({
                    (InventorySlots.profileId eq profileId) and (InventorySlots.x eq x) and (InventorySlots.y eq y)
                }) {
                    it[InventorySlots.itemId] = itemId
                    it[anchorId] = null
                }
            } else {
                InventorySlots.deleteWhere {
                    (InventorySlots.profileId eq profileId) and (InventorySlots.x eq x) and (InventorySlots.y eq y)
                }
            }
        } else if (itemId != null) {
            InventorySlots.insert {
                it[InventorySlots.profileId] = profileId
                it[InventorySlots.x] = x
                it[InventorySlots.y] = y
                it[InventorySlots.itemId] = itemId
            }
        }
    }

    fun setAnchor(profileId: Int, x: Int, y: Int, anchorId: Int) = transaction {
        val existing = InventorySlots.selectAll().where {
            (InventorySlots.profileId eq profileId) and (InventorySlots.x eq x) and (InventorySlots.y eq y)
        }.firstOrNull()
        if (existing != null) {
            InventorySlots.update({
                (InventorySlots.profileId eq profileId) and (InventorySlots.x eq x) and (InventorySlots.y eq y)
            }) {
                it[InventorySlots.anchorId] = anchorId
                it[itemId] = null
            }
        } else {
            InventorySlots.insert {
                it[InventorySlots.profileId] = profileId
                it[InventorySlots.x] = x
                it[InventorySlots.y] = y
                it[InventorySlots.anchorId] = anchorId
            }
        }
    }

    fun getSlot(profileId: Int, x: Int, y: Int): SlotData? = transaction {
        InventorySlots.selectAll().where {
            (InventorySlots.profileId eq profileId) and (InventorySlots.x eq x) and (InventorySlots.y eq y)
        }.firstOrNull()?.let { rowToSlot(it) }
    }

    fun removeSlot(profileId: Int, x: Int, y: Int) = transaction {
        InventorySlots.deleteWhere {
            (InventorySlots.profileId eq profileId) and (InventorySlots.x eq x) and (InventorySlots.y eq y)
        }
    }

    fun getRow(profileId: Int, y: Int): List<SlotData> = transaction {
        InventorySlots.selectAll().where {
            (InventorySlots.profileId eq profileId) and (InventorySlots.y eq y)
        }.map { rowToSlot(it) }
    }

    fun getRegion(profileId: Int, x1: Int, y1: Int, x2: Int, y2: Int): List<SlotData> = transaction {
        val r = region(x1, y1, x2, y2)
        InventorySlots.selectAll().where {
            (InventorySlots.profileId eq profileId) and
                (InventorySlots.x.between(r.minX, r.maxX)) and
                (InventorySlots.y.between(r.minY, r.maxY))
        }.map { rowToSlot(it) }
    }

    fun deleteRegion(profileId: Int, x1: Int, y1: Int, x2: Int, y2: Int) = transaction {
        val r = region(x1, y1, x2, y2)
        InventorySlots.deleteWhere {
            (InventorySlots.profileId eq profileId) and
                (InventorySlots.x.between(r.minX, r.maxX)) and
                (InventorySlots.y.between(r.minY, r.maxY))
        }
    }

    fun batchRemove(profileId: Int, positions: Set<Pair<Int, Int>>) {
        if (positions.isEmpty()) return
        transaction {
            for (y in positions.map { it.second }.toSet()) {
                val xs = positions.filter { it.second == y }.map { it.first }.toSet()
                if (xs.isEmpty()) continue
                InventorySlots.deleteWhere {
                    (InventorySlots.profileId eq profileId) and
                        (InventorySlots.y eq y) and
                        (InventorySlots.x inList xs)
                }
            }
        }
    }

    fun batchSetItems(profileId: Int, entries: List<Triple<Int, Int, Int>>) {
        if (entries.isEmpty()) return
        transaction {
            for ((x, y, itemId) in entries) {
                val existing = InventorySlots.selectAll().where {
                    (InventorySlots.profileId eq profileId) and
                        (InventorySlots.x eq x) and
                        (InventorySlots.y eq y)
                }.firstOrNull()
                if (existing != null) {
                    InventorySlots.update({
                        (InventorySlots.profileId eq profileId) and
                            (InventorySlots.x eq x) and
                            (InventorySlots.y eq y)
                    }) {
                        it[InventorySlots.itemId] = itemId
                        it[InventorySlots.anchorId] = null
                    }
                } else {
                    InventorySlots.insert {
                        it[InventorySlots.profileId] = profileId
                        it[InventorySlots.x] = x
                        it[InventorySlots.y] = y
                        it[InventorySlots.itemId] = itemId
                    }
                }
            }
        }
    }

    fun allSlots(profileId: Int): List<SlotData> = transaction {
        InventorySlots.selectAll().where { InventorySlots.profileId eq profileId }
            .map { rowToSlot(it) }
    }

    fun allSlotsWithItem(itemId: Int): List<SlotData> = transaction {
        InventorySlots.selectAll().where { InventorySlots.itemId eq itemId }
            .map { rowToSlot(it) }
    }

    fun isOccupied(profileId: Int, x: Int, y: Int): Boolean =
        getSlot(profileId, x, y) != null

    fun deleteAllForProfile(profileId: Int) = transaction {
        InventorySlots.deleteWhere { InventorySlots.profileId eq profileId }
    }

    fun allSlotsForMaterial(profileId: Int, materialName: String): List<SlotData> {
        val itemIds = ItemManager.findByMaterial(materialName)
        if (itemIds.isEmpty()) return emptyList()
        return transaction {
            InventorySlots.selectAll().where {
                (InventorySlots.profileId eq profileId) and (InventorySlots.itemId inList itemIds)
            }.map { rowToSlot(it) }
        }
    }

    private fun rowToSlot(row: org.jetbrains.exposed.sql.ResultRow): SlotData =
        SlotData(row[InventorySlots.profileId], row[InventorySlots.x], row[InventorySlots.y], row[InventorySlots.itemId], row[InventorySlots.anchorId])
}
