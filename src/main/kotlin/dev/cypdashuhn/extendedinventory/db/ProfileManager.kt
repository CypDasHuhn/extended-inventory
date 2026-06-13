package dev.cypdashuhn.extendedinventory.db

import org.bukkit.entity.Player
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

enum class ProfileOpenness { PUBLIC_READ, PUBLIC_WRITE, PRIVATE }

object ProfileManager {
    object Profiles : IntIdTable("ei_profiles") {
        val name = varchar("name", 64)
        val createdByPlayerId = integer("created_by_player_id")
        val openness = enumerationByName<ProfileOpenness>("openness", 16).default(ProfileOpenness.PRIVATE)
    }

    class ProfileEntry(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<ProfileEntry>(Profiles)
        val name by Profiles.name
        val createdByPlayerId by Profiles.createdByPlayerId
        val openness by Profiles.openness
    }

    private fun playerId(player: Player): Int {
        val pm = dev.cypdashuhn.extendedinventory.ExtendedInventoryPlugin.playerManager
        return pm.playerByUUID(player.uniqueId.toString())?.id?.value
            ?: throw IllegalStateException("Player ${player.name} not registered in PlayerManager")
    }

    fun create(name: String, player: Player): Int = transaction {
        Profiles.insert {
            it[Profiles.name] = name
            it[createdByPlayerId] = playerId(player)
        }[Profiles.id].value
    }

    fun findById(id: Int): ProfileEntry? = transaction {
        ProfileEntry.findById(id)
    }

    fun findByName(name: String): ProfileEntry? = transaction {
        ProfileEntry.find { Profiles.name eq name }.firstOrNull()
    }

    fun all(): List<ProfileEntry> = transaction {
        ProfileEntry.all().toList()
    }

    fun allAccessible(player: Player): List<ProfileEntry> = transaction {
        val pid = playerId(player)
        val accessibleIds = PlayerProfileManager.playerProfileIds(pid)
        ProfileEntry.find { Profiles.id inList accessibleIds }.toList()
    }

    fun rename(id: Int, newName: String) = transaction {
        Profiles.update({ Profiles.id eq id }) { it[name] = newName }
    }

    fun setOpenness(id: Int, openness: ProfileOpenness) = transaction {
        Profiles.update({ Profiles.id eq id }) { it[Profiles.openness] = openness }
    }

    fun delete(id: Int) = transaction {
        InventoryManager.deleteAllForProfile(id)
        AnchorManager.deleteAllForProfile(id)
        PlayerProfileManager.deleteAllForProfile(id)
        ProfileEntry.findById(id)?.delete()
    }
}
