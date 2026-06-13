package dev.cypdashuhn.extendedinventory.db

import dev.cypdashuhn.extendedinventory.util.playerId
import org.bukkit.entity.Player
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

enum class PlayerProfileStatus { PRIMARY, WRITE_READ, READ_ONLY }

object PlayerProfileManager {
    object PlayerProfiles : IntIdTable("ei_player_profiles") {
        val playerId = integer("player_id")
        val profileId = integer("profile_id")
        val status = enumerationByName<PlayerProfileStatus>("status", 16).default(PlayerProfileStatus.READ_ONLY)
    }

    data class PlayerProfileRow(
        val playerId: Int,
        val profileId: Int,
        val status: PlayerProfileStatus,
    )

    fun assign(player: Player, profileId: Int, status: PlayerProfileStatus = PlayerProfileStatus.READ_ONLY) {
        val pid = playerId(player)
        transaction {
            val existing = PlayerProfiles.selectAll().where {
                (PlayerProfiles.playerId eq pid) and (PlayerProfiles.profileId eq profileId)
            }.firstOrNull()
            if (existing != null) {
                PlayerProfiles.update({
                    (PlayerProfiles.playerId eq pid) and (PlayerProfiles.profileId eq profileId)
                }) { it[PlayerProfiles.status] = status }
            } else {
                PlayerProfiles.insert {
                    it[PlayerProfiles.playerId] = pid
                    it[PlayerProfiles.profileId] = profileId
                    it[PlayerProfiles.status] = status
                }
            }
        }
    }

    fun subscribe(player: Player, profileId: Int) {
        assign(player, profileId, PlayerProfileStatus.READ_ONLY)
    }

    fun setPrimary(player: Player, profileId: Int) {
        val pid = playerId(player)
        transaction {
            PlayerProfiles.update({ PlayerProfiles.playerId eq pid }) {
                it[status] = PlayerProfileStatus.READ_ONLY
            }
        }
        assign(player, profileId, PlayerProfileStatus.PRIMARY)
    }

    fun getPrimary(player: Player): Int? {
        val pid = playerId(player)
        return transaction {
            PlayerProfiles.selectAll().where {
                (PlayerProfiles.playerId eq pid) and (PlayerProfiles.status eq PlayerProfileStatus.PRIMARY)
            }.firstOrNull()?.get(PlayerProfiles.profileId)
        }
    }

    fun getStatus(player: Player, profileId: Int): PlayerProfileStatus? = transaction {
        val pid = playerId(player)
        PlayerProfiles.selectAll().where {
            (PlayerProfiles.playerId eq pid) and (PlayerProfiles.profileId eq profileId)
        }.firstOrNull()?.get(PlayerProfiles.status)
    }

    fun playerProfileIds(playerId: Int): List<Int> = transaction {
        PlayerProfiles.selectAll().where { PlayerProfiles.playerId eq playerId }
            .map { it[PlayerProfiles.profileId] }
    }

    fun profilePlayers(profileId: Int): List<PlayerProfileRow> = transaction {
        PlayerProfiles.selectAll().where { PlayerProfiles.profileId eq profileId }
            .map { PlayerProfileRow(it[PlayerProfiles.playerId], it[PlayerProfiles.profileId], it[PlayerProfiles.status]) }
    }

    fun remove(player: Player, profileId: Int) {
        val pid = playerId(player)
        transaction {
            PlayerProfiles.deleteWhere {
                (PlayerProfiles.playerId eq pid) and (PlayerProfiles.profileId eq profileId)
            }
        }
    }

    fun deleteAllForProfile(profileId: Int) = transaction {
        PlayerProfiles.deleteWhere { PlayerProfiles.profileId eq profileId }
    }
}
