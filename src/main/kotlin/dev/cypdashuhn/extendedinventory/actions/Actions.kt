package dev.cypdashuhn.extendedinventory.actions

import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.db.InventoryManager
import dev.cypdashuhn.extendedinventory.db.ItemManager
import dev.cypdashuhn.extendedinventory.db.PlayerProfileManager
import dev.cypdashuhn.extendedinventory.db.PlayerProfileStatus
import dev.cypdashuhn.extendedinventory.db.ProfileManager
import dev.cypdashuhn.extendedinventory.db.ProfileOpenness
import dev.cypdashuhn.extendedinventory.db.SlotCache
import dev.cypdashuhn.extendedinventory.util.region
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object InventoryActions {
    fun getItem(profileId: Int, x: Int, y: Int): ItemStack? {
        val slot = SlotCache.getSlot(profileId, x, y) ?: return null
        if (slot.itemId != null) return ItemManager.getItem(slot.itemId)
        return null
    }

    fun setItem(profileId: Int, x: Int, y: Int, item: ItemStack?) {
        if (item == null || item.type.isAir) {
            removeItemAt(profileId, x, y)
        } else {
            setItemId(profileId, x, y, ItemManager.store(item))
        }
    }

    internal fun removeItemAt(profileId: Int, x: Int, y: Int) {
        val previousItemId = SlotCache.getSlot(profileId, x, y)?.itemId
        SlotCache.removeSlot(profileId, x, y)
        if (previousItemId != null) {
            ItemManager.deleteIfUnused(previousItemId)
        }
    }

    internal fun setItemId(profileId: Int, x: Int, y: Int, itemId: Int) {
        val previousItemId = SlotCache.getSlot(profileId, x, y)?.itemId
        SlotCache.setItem(profileId, x, y, itemId)
        if (previousItemId != null && previousItemId != itemId) {
            ItemManager.deleteIfUnused(previousItemId)
        }
    }

    fun copyToCursor(player: Player, profileId: Int, x: Int, y: Int) {
        val item = getItem(profileId, x, y) ?: return
        player.setItemOnCursor(item.clone())
    }

    fun groupDelete(profileId: Int, x1: Int, y1: Int, x2: Int, y2: Int) {
        val r = region(x1, y1, x2, y2)
        SlotCache.batchRemove(profileId, r.positions)
    }

    fun groupMove(
        profileId: Int,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        targetX: Int,
        targetY: Int
    ): Boolean {
        val r = region(x1, y1, x2, y2)
        val sourceSlots = InventoryManager.getRegion(profileId, x1, y1, x2, y2)

        val moveEntries =
            sourceSlots.mapNotNull { slot ->
                val dx = slot.x - r.minX
                val dy = slot.y - r.minY
                val newX = targetX + dx
                val newY = targetY + dy
                if (slot.itemId != null) {
                    Triple(newX, newY, slot.itemId)
                } else {
                    null
                }
            }

        if (moveEntries.isEmpty()) return false

        SlotCache.batchRemove(profileId, r.positions)
        SlotCache.batchSetItems(profileId, moveEntries)
        return true
    }

    fun getRegionSlots(
        profileId: Int,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int
    ): List<InventoryManager.SlotData> =
        InventoryManager
            .getRegion(profileId, x1, y1, x2, y2)
}

object ProfileActions {
    fun createProfile(player: Player, name: String): Int {
        val profileId = ProfileManager.create(name, player)
        PlayerProfileManager.assign(player, profileId, PlayerProfileStatus.PRIMARY)
        return profileId
    }

    fun switchProfile(player: Player, profileId: Int) {
        val status = PlayerProfileManager.getStatus(player, profileId)
        if (status == null) {
            PlayerProfileManager.subscribe(player, profileId)
        }
        PlayerProfileManager.setPrimary(player, profileId)
    }

    fun setDefault(player: Player, profileId: Int) {
        PlayerProfileManager.setPrimary(player, profileId)
    }

    fun subscribe(player: Player, profileId: Int) {
        PlayerProfileManager.subscribe(player, profileId)
    }

    fun invite(player: Player, profileId: Int, targetPlayer: Player, status: PlayerProfileStatus) {
        val callerStatus = PlayerProfileManager.getStatus(player, profileId)
        if (callerStatus != PlayerProfileStatus.PRIMARY &&
            callerStatus != PlayerProfileStatus.WRITE_READ
        ) {
            return
        }
        PlayerProfileManager.assign(targetPlayer, profileId, status)
    }

    fun setOpenness(profileId: Int, openness: ProfileOpenness) {
        ProfileManager.setOpenness(profileId, openness)
    }
}

object AnchorActions {
    fun addAnchor(profileId: Int, name: String, x: Int, y: Int): Int =
        AnchorManager.create(profileId, name, x, y)

    fun deleteAnchor(player: Player, profileId: Int, name: String): Boolean {
        val status = PlayerProfileManager.getStatus(player, profileId)
        if (status != PlayerProfileStatus.PRIMARY &&
            status != PlayerProfileStatus.WRITE_READ
        ) {
            return false
        }
        val anchor = AnchorManager.findByName(profileId, name) ?: return false
        AnchorManager.delete(anchor.id)
        return true
    }

    fun renameAnchor(player: Player, profileId: Int, oldName: String, newName: String): Boolean {
        val status = PlayerProfileManager.getStatus(player, profileId)
        if (status != PlayerProfileStatus.PRIMARY &&
            status != PlayerProfileStatus.WRITE_READ
        ) {
            return false
        }
        val anchor = AnchorManager.findByName(profileId, oldName) ?: return false
        AnchorManager.rename(anchor.id, newName)
        return true
    }

    fun getAnchorInfo(profileId: Int, name: String): AnchorManager.AnchorData? =
        AnchorManager
            .findByName(profileId, name)
}

object CycleActions {
    fun buildCycleChain(cells: List<Pair<Int, Int>>, start: Pair<Int, Int>): List<Pair<Int, Int>> {
        if (cells.isEmpty()) return emptyList()

        val remaining = cells.toMutableSet()
        var current = nearest(remaining, start)
        remaining.remove(current)

        val chain = mutableListOf(current)
        while (remaining.isNotEmpty()) {
            current = nearest(remaining, current)
            remaining.remove(current)
            chain.add(current)
        }
        return chain
    }

    private fun nearest(remaining: Set<Pair<Int, Int>>, from: Pair<Int, Int>): Pair<Int, Int> =
        remaining.minWithOrNull(
            compareBy<Pair<Int, Int>> { distanceSq(from, it) }
                .thenBy { it.first }
                .thenBy { it.second },
        ) ?: from

    private fun distanceSq(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
        val dx = a.first - b.first
        val dy = a.second - b.second
        return dx * dx + dy * dy
    }
}
