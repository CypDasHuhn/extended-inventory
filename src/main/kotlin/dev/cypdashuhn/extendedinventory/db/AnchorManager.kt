package dev.cypdashuhn.extendedinventory.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object AnchorManager {
    object Anchors : IntIdTable("ei_anchors") {
        val profileId = integer("profile_id")
        val x = integer("x")
        val y = integer("y")
        val name = varchar("name", 64)
    }

    class AnchorEntry(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<AnchorEntry>(Anchors)
        val profileId by Anchors.profileId
        val x by Anchors.x
        val y by Anchors.y
        val name by Anchors.name
    }

    data class AnchorData(
        val id: Int,
        val profileId: Int,
        val x: Int,
        val y: Int,
        val name: String,
    )

    fun create(profileId: Int, name: String, x: Int, y: Int): Int = transaction {
        Anchors.insert {
            it[Anchors.profileId] = profileId
            it[Anchors.x] = x
            it[Anchors.y] = y
            it[Anchors.name] = name
        }[Anchors.id].value
    }

    fun findById(id: Int): AnchorData? = transaction {
        AnchorEntry.findById(id)?.let {
            AnchorData(it.id.value, it.profileId, it.x, it.y, it.name)
        }
    }

    fun findByName(profileId: Int, name: String): AnchorData? = transaction {
        Anchors.selectAll().where {
            (Anchors.profileId eq profileId) and (Anchors.name eq name)
        }.firstOrNull()?.let {
            AnchorData(it[Anchors.id].value, it[Anchors.profileId], it[Anchors.x], it[Anchors.y], it[Anchors.name])
        }
    }

    fun findAtPosition(profileId: Int, x: Int, y: Int): AnchorData? = transaction {
        Anchors.selectAll().where {
            (Anchors.profileId eq profileId) and (Anchors.x eq x) and (Anchors.y eq y)
        }.firstOrNull()?.let {
            AnchorData(it[Anchors.id].value, it[Anchors.profileId], it[Anchors.x], it[Anchors.y], it[Anchors.name])
        }
    }

    fun allForProfile(profileId: Int): List<AnchorData> = transaction {
        Anchors.selectAll().where { Anchors.profileId eq profileId }
            .map { AnchorData(it[Anchors.id].value, it[Anchors.profileId], it[Anchors.x], it[Anchors.y], it[Anchors.name]) }
    }

    fun rename(id: Int, newName: String) = transaction {
        Anchors.update({ Anchors.id eq id }) { it[name] = newName }
    }

    fun delete(id: Int) = transaction {
        val anchor = AnchorEntry.findById(id) ?: return@transaction
        InventorySlotsRemoveAnchorRefs(anchor.profileId, id)
        anchor.delete()
    }

    private fun InventorySlotsRemoveAnchorRefs(profileId: Int, anchorId: Int) = transaction {
        InventoryManager.InventorySlots.update({
            (InventoryManager.InventorySlots.profileId eq profileId) and
                (InventoryManager.InventorySlots.anchorId eq anchorId)
        }) { it[InventoryManager.InventorySlots.anchorId] = null }
    }

    fun deleteAllForProfile(profileId: Int) = transaction {
        Anchors.deleteWhere { Anchors.profileId eq profileId }
    }
}
