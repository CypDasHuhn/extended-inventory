package dev.cypdashuhn.extendedinventory

import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.db.InventoryManager
import dev.cypdashuhn.extendedinventory.db.ItemManager
import dev.cypdashuhn.extendedinventory.db.PlayerProfileManager
import dev.cypdashuhn.extendedinventory.db.PlayerStateStore
import dev.cypdashuhn.extendedinventory.db.ProfileManager
import dev.cypdashuhn.extendedinventory.db.SlotCache
import dev.rooster.ui.sql.SqlInterfaceContextProvider
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object TestDatabase {
    private var connected = false
    private val dbFile: File by lazy {
        File.createTempFile("extended-inventory-test", ".db").apply { deleteOnExit() }
    }

    fun connect() {
        if (connected) return
        connected = true
        Database.connect("jdbc:sqlite:${dbFile.absolutePath}", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                ProfileManager.Profiles,
                InventoryManager.InventorySlots,
                ItemManager.Items,
                AnchorManager.Anchors,
                PlayerProfileManager.PlayerProfiles,
                PlayerStateStore.PlayerStates,
                SqlInterfaceContextProvider.InterfaceContexts,
            )
        }
    }

    fun clear() {
        connect()
        transaction {
            PlayerProfileManager.PlayerProfiles.deleteAll()
            AnchorManager.Anchors.deleteAll()
            InventoryManager.InventorySlots.deleteAll()
            ItemManager.Items.deleteAll()
            ProfileManager.Profiles.deleteAll()
            PlayerStateStore.PlayerStates.deleteAll()
        }
        SlotCache.clearAll()
    }

    fun insertRawItem(material: String = "STONE"): Int =
        transaction {
            ItemManager.Items
                .insert {
                    it[serializedItem] = "raw:$material:${System.nanoTime()}"
                    it[materialName] = material
                }[ItemManager.Items.id]
                .value
        }

    fun itemExists(itemId: Int): Boolean =
        transaction {
            ItemManager.Items
                .selectAll()
                .where { ItemManager.Items.id eq itemId }
                .count() > 0
        }
}
