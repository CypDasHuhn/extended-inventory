package dev.cypdashuhn.extendedinventory.db

import dev.cypdashuhn.extendedinventory.TestDatabase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ItemManagerTest {

    @BeforeEach
    fun setUp() {
        TestDatabase.clear()
    }

    @Test
    fun `encode and decode round-trips bytes`() {
        val bytes = byteArrayOf(0, 1, 2, -1, 127, -128, 42)
        val encoded = ItemManager.encode(bytes)
        assertArrayEquals(bytes, ItemManager.decode(encoded))
    }

    @Test
    fun `getItem returns null for missing id`() {
        assertNull(ItemManager.getItem(Int.MAX_VALUE))
    }

    @Test
    fun `getMaterialName returns material of stored item`() {
        val id = TestDatabase.insertRawItem("BIRCH_PLANKS")
        assertEquals("BIRCH_PLANKS", ItemManager.getMaterialName(id))
    }

    @Test
    fun `findByMaterial returns stored item ids`() {
        val id1 = TestDatabase.insertRawItem("OAK_LOG")
        val id2 = TestDatabase.insertRawItem("OAK_LOG")
        TestDatabase.insertRawItem("STONE")

        val ids = ItemManager.findByMaterial("OAK_LOG")
        assertTrue(id1 in ids)
        assertTrue(id2 in ids)
        assertEquals(2, ids.size)
    }

    @Test
    fun `deleteIfUnused removes unreferenced item`() {
        val id = TestDatabase.insertRawItem()
        ItemManager.deleteIfUnused(id)
        assertFalse(TestDatabase.itemExists(id))
    }

    @Test
    fun `deleteIfUnused keeps item still referenced by a slot`() {
        val id = TestDatabase.insertRawItem()
        InventoryManager.setItem(1, 0, 0, id)

        ItemManager.deleteIfUnused(id)

        assertTrue(TestDatabase.itemExists(id))
    }
}
