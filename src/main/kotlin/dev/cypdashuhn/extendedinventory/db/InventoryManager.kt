package dev.cypdashuhn.extendedinventory.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.between
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
        }.firstOrNull()?.let {
            SlotData(it[InventorySlots.profileId], it[InventorySlots.x], it[InventorySlots.y], it[InventorySlots.itemId], it[InventorySlots.anchorId])
        }
    }

    fun removeSlot(profileId: Int, x: Int, y: Int) = transaction {
        InventorySlots.deleteWhere {
            (InventorySlots.profileId eq profileId) and (InventorySlots.x eq x) and (InventorySlots.y eq y)
        }
    }

    fun getRow(profileId: Int, y: Int): List<SlotData> = transaction {
        InventorySlots.selectAll().where {
            (InventorySlots.profileId eq profileId) and (InventorySlots.y eq y)
        }.map { SlotData(it[InventorySlots.profileId], it[InventorySlots.x], it[InventorySlots.y], it[InventorySlots.itemId], it[InventorySlots.anchorId]) }
    }

    fun getRegion(profileId: Int, x1: Int, y1: Int, x2: Int, y2: Int): List<SlotData> = transaction {
        val minX = minOf(x1, x2)
        val maxX = maxOf(x1, x2)
        val minY = minOf(y1, y2)
        val maxY = maxOf(y1, y2)
        InventorySlots.selectAll().where {
            (InventorySlots.profileId eq profileId) and
                (InventorySlots.x.between(minX, maxX)) and
                (InventorySlots.y.between(minY, maxY))
        }.map { SlotData(it[InventorySlots.profileId], it[InventorySlots.x], it[InventorySlots.y], it[InventorySlots.itemId], it[InventorySlots.anchorId]) }
    }

    fun deleteRegion(profileId: Int, x1: Int, y1: Int, x2: Int, y2: Int) = transaction {
        val minX = minOf(x1, x2)
        val maxX = maxOf(x1, x2)
        val minY = minOf(y1, y2)
        val maxY = maxOf(y1, y2)
        InventorySlots.deleteWhere {
            (InventorySlots.profileId eq profileId) and
                (InventorySlots.x.between(minX, maxX)) and
                (InventorySlots.y.between(minY, maxY))
        }
    }

    fun allSlots(profileId: Int): List<SlotData> = transaction {
        InventorySlots.selectAll().where { InventorySlots.profileId eq profileId }
            .map { SlotData(it[InventorySlots.profileId], it[InventorySlots.x], it[InventorySlots.y], it[InventorySlots.itemId], it[InventorySlots.anchorId]) }
    }

    fun allSlotsWithItem(itemId: Int): List<SlotData> = transaction {
        InventorySlots.selectAll().where { InventorySlots.itemId eq itemId }
            .map { SlotData(it[InventorySlots.profileId], it[InventorySlots.x], it[InventorySlots.y], it[InventorySlots.itemId], it[InventorySlots.anchorId]) }
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
            }.map { SlotData(it[InventorySlots.profileId], it[InventorySlots.x], it[InventorySlots.y], it[InventorySlots.itemId], it[InventorySlots.anchorId]) }
        }
    }
}
