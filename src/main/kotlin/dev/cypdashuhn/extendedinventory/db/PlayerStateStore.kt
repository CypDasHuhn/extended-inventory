package dev.cypdashuhn.extendedinventory.db

import dev.cypdashuhn.extendedinventory.hotbar.HotbarMode
import dev.cypdashuhn.extendedinventory.hotbar.PlayerState
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PlayerStateStore {
    object PlayerStates : Table("ei_player_states") {
        val playerUuid = varchar("player_uuid", 36)
        val profileId = integer("profile_id").nullable()
        val x = integer("x")
        val y = integer("y")
        val anchored = bool("anchored")
        val mode = varchar("mode", 16)
    }

    fun load(uuid: String): PlayerState? =
        transaction {
            PlayerStates
                .selectAll()
                .where { PlayerStates.playerUuid eq uuid }
                .firstOrNull()
                ?.let {
                    PlayerState(
                        profileId = it[PlayerStates.profileId],
                        x = it[PlayerStates.x],
                        y = it[PlayerStates.y],
                        anchored = it[PlayerStates.anchored],
                        mode = runCatching { HotbarMode.valueOf(it[PlayerStates.mode]) }.getOrDefault(HotbarMode.FREE),
                    )
                }
        }

    fun save(uuid: String, state: PlayerState) {
        transaction {
            val existing = PlayerStates
                .selectAll()
                .where { PlayerStates.playerUuid eq uuid }
                .firstOrNull()
            if (existing != null) {
                PlayerStates.update({ PlayerStates.playerUuid eq uuid }) {
                    it[profileId] = state.profileId
                    it[x] = state.x
                    it[y] = state.y
                    it[anchored] = state.anchored
                    it[mode] = state.mode.name
                }
            } else {
                PlayerStates.insert {
                    it[playerUuid] = uuid
                    it[profileId] = state.profileId
                    it[x] = state.x
                    it[y] = state.y
                    it[anchored] = state.anchored
                    it[mode] = state.mode.name
                }
            }
        }
    }

    fun delete(uuid: String) {
        transaction {
            PlayerStates.deleteWhere { PlayerStates.playerUuid eq uuid }
        }
    }
}
