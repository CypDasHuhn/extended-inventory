package dev.cypdashuhn.extendedinventory.db

import dev.cypdashuhn.extendedinventory.TestPlayers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class BufferManagerTest {
    @Test
    fun `push and pop buffer entry preserves position and item count`() {
        val player = TestPlayers.player()
        val items = listOf(null, null, null, null)
        val position = 5 to 10

        BufferManager.push(player, items, position)
        val entry = BufferManager.pop(player)

        assertNotNull(entry)
        assertEquals(position, entry!!.position)
        assertEquals(4, entry.items.size)
    }

    @Test
    fun `pop empty buffer returns null`() {
        val entry = BufferManager.pop(TestPlayers.player())
        assertNull(entry)
    }

    @Test
    fun `peek does not remove entry`() {
        val player = TestPlayers.player()
        BufferManager.push(player, listOf(null), 0 to 0)

        assertNotNull(BufferManager.peek(player))
        assertNotNull(BufferManager.pop(player))
        assertNull(BufferManager.pop(player))
    }

    @Test
    fun `buffer entries are stacked LIFO`() {
        val player = TestPlayers.player()
        BufferManager.push(player, listOf(null), 1 to 1)
        BufferManager.push(player, listOf(null), 2 to 2)

        assertEquals(2 to 2, BufferManager.pop(player)!!.position)
        assertEquals(1 to 1, BufferManager.pop(player)!!.position)
    }

    @Test
    fun `buffer TTL expires old entries`() {
        val player = TestPlayers.player()
        BufferManager.setTtl(1)
        BufferManager.push(player, listOf(null), 0 to 0)
        Thread.sleep(5)
        assertNull(BufferManager.pop(player))
        BufferManager.setTtl(5 * 60 * 1000) // restore default
    }

    @Test
    fun `list returns all entries in LIFO order`() {
        val player = TestPlayers.player()
        BufferManager.push(player, listOf(null), 1 to 1)
        BufferManager.push(player, listOf(null), 2 to 2)

        val list = BufferManager.list(player)
        assertEquals(2, list.size)
        assertEquals(2 to 2, list[0].position)
        assertEquals(1 to 1, list[1].position)
    }

    @Test
    fun `loadByName finds buffer by formatted timestamp`() {
        val player = TestPlayers.player()
        BufferManager.push(player, listOf(null), 3 to 3)

        val entries = BufferManager.list(player)
        assertTrue(entries.isNotEmpty())
        val name = BufferManager.formatTimestamp(entries[0].timestamp)

        val loaded = BufferManager.loadByName(player, name)
        assertNotNull(loaded)
        assertEquals(3 to 3, loaded!!.position)
    }

    @Test
    fun `clear removes all buffers`() {
        val player = TestPlayers.player()
        BufferManager.push(player, listOf(null), 0 to 0)
        BufferManager.clear(player)
        assertTrue(BufferManager.list(player).isEmpty())
    }

    @Test
    fun `buffer isolates per player`() {
        val p1 = TestPlayers.player(UUID.randomUUID())
        val p2 = TestPlayers.player(UUID.randomUUID())

        BufferManager.push(p1, listOf(null), 0 to 0)

        assertNull(BufferManager.pop(p2))
        assertNotNull(BufferManager.pop(p1))
    }
}
