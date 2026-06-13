package dev.cypdashuhn.extendedinventory.db

import com.google.gson.Gson
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object ItemManager {
    object Items : IntIdTable("ei_items") {
        val serializedItem = text("serialized_item")
        val materialName = varchar("material_name", 128)
    }

    class ItemEntry(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<ItemEntry>(Items)
        val serializedItem by Items.serializedItem
        val materialName by Items.materialName
    }

    private val gson = Gson()

    fun store(itemStack: ItemStack): Int {
        val serialized = gson.toJson(itemStack.serialize())
        val material = itemStack.type.name

        return transaction {
            val existing = Items.selectAll().where {
                Items.serializedItem eq serialized
            }.firstOrNull()
            if (existing != null) {
                existing[Items.id].value
            } else {
                Items.insert {
                    it[serializedItem] = serialized
                    it[materialName] = material
                }[Items.id].value
            }
        }
    }

    fun getItem(itemId: Int): ItemStack? = transaction {
        val row = Items.selectAll().where { Items.id eq itemId }.firstOrNull() ?: return@transaction null
        val map: Map<String, Any> = gson.fromJson(row[Items.serializedItem], Map::class.java)
        @Suppress("UNCHECKED_CAST")
        ItemStack.deserialize(map as Map<String, Any>)
    }

    fun getMaterialName(itemId: Int): String? = transaction {
        Items.selectAll().where { Items.id eq itemId }.firstOrNull()?.get(Items.materialName)
    }

    fun findByMaterial(materialName: String): List<Int> = transaction {
        Items.selectAll().where { Items.materialName eq materialName }
            .map { it[Items.id].value }
    }

    fun updateItem(itemId: Int, itemStack: ItemStack) {
        val serialized = gson.toJson(itemStack.serialize())
        transaction {
            Items.update({ Items.id eq itemId }) {
                it[serializedItem] = serialized
                it[materialName] = itemStack.type.name
            }
        }
    }

    fun deleteIfUnused(itemId: Int) {
        transaction {
            val used = InventoryManager.InventorySlots.selectAll()
                .where { InventoryManager.InventorySlots.itemId eq itemId }
                .count() > 0
            if (!used) {
                Items.deleteWhere { Items.id eq itemId }
            }
        }
    }
}
