package dev.cypdashuhn.extendedinventory.db

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class BufferEntry(
    val items: List<ItemStack?>,
    val timestamp: Long = System.currentTimeMillis(),
    val position: Pair<Int, Int>,
)

object BufferManager {
    private val buffers: MutableMap<String, MutableList<BufferEntry>> = mutableMapOf()
    private var ttlMillis: Long = 5 * 60 * 1000

    fun setTtl(millis: Long) {
        ttlMillis = millis
    }

    fun push(player: Player, hotbarItems: List<ItemStack?>, position: Pair<Int, Int>) {
        val key = player.uniqueId.toString()
        val entry = BufferEntry(
            items = hotbarItems.map { it?.clone() },
            position = position,
        )
        buffers.getOrPut(key) { mutableListOf() }.add(0, entry)
        expireOld(player)
    }

    fun pop(player: Player): BufferEntry? {
        val key = player.uniqueId.toString()
        expireOld(player)
        val stack = buffers[key] ?: return null
        if (stack.isEmpty()) return null
        return stack.removeAt(0)
    }

    fun peek(player: Player): BufferEntry? {
        val key = player.uniqueId.toString()
        expireOld(player)
        return buffers[key]?.firstOrNull()
    }

    fun list(player: Player): List<BufferEntry> {
        val key = player.uniqueId.toString()
        expireOld(player)
        return buffers[key]?.toList() ?: emptyList()
    }

    fun loadByName(player: Player, name: String): BufferEntry? = list(player).firstOrNull { formatTimestamp(it.timestamp) == name }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.ENGLISH)
        return sdf.format(java.util.Date(timestamp))
    }

    private fun expireOld(player: Player) {
        val key = player.uniqueId.toString()
        val stack = buffers[key] ?: return
        val cutoff = System.currentTimeMillis() - ttlMillis
        stack.removeAll { it.timestamp < cutoff }
    }

    fun clear(player: Player) {
        buffers.remove(player.uniqueId.toString())
    }
}
