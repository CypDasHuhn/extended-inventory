package dev.cypdashuhn.extendedinventory.db

import dev.cypdashuhn.extendedinventory.TestDatabase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InventoryManagerTest {
    @BeforeEach
    fun setUp() {
        TestDatabase.clear()
    }

    @Test
    fun `setItem inserts slot`() {
        InventoryManager.setItem(1, 2, 3, 42)
        val slot = InventoryManager.getSlot(1, 2, 3)
        assertNotNull(slot)
        assertEquals(42, slot!!.itemId)
        assertNull(slot.anchorId)
    }

    @Test
    fun `setItem updates existing slot item`() {
        InventoryManager.setItem(1, 2, 3, 42)
        InventoryManager.setItem(1, 2, 3, 99)

        assertEquals(99, InventoryManager.getSlot(1, 2, 3)!!.itemId)
        assertEquals(1, InventoryManager.allSlots(1).size)
    }

    @Test
    fun `setItem with null itemId deletes slot`() {
        InventoryManager.setItem(1, 2, 3, 42)
        InventoryManager.setItem(1, 2, 3, null)

        assertNull(InventoryManager.getSlot(1, 2, 3))
    }

    @Test
    fun `setAnchor replaces item with anchor`() {
        InventoryManager.setItem(1, 2, 3, 42)
        InventoryManager.setAnchor(1, 2, 3, 7)

        val slot = InventoryManager.getSlot(1, 2, 3)!!
        assertNull(slot.itemId)
        assertEquals(7, slot.anchorId)
    }

    @Test
    fun `getRow returns all slots in a row`() {
        InventoryManager.setItem(1, 0, 5, 1)
        InventoryManager.setItem(1, 3, 5, 2)
        InventoryManager.setItem(1, 0, 6, 3)

        val row = InventoryManager.getRow(1, 5)
        assertEquals(2, row.size)
        assertEquals(setOf(0, 3), row.map { it.x }.toSet())
    }

    @Test
    fun `getRegion normalizes swapped corners`() {
        InventoryManager.setItem(1, 0, 0, 1)
        InventoryManager.setItem(1, 2, 2, 2)
        InventoryManager.setItem(1, 5, 5, 3)

        val region = InventoryManager.getRegion(1, 2, 2, 0, 0)
        assertEquals(2, region.size)
    }

    @Test
    fun `deleteRegion removes only slots in region`() {
        InventoryManager.setItem(1, 0, 0, 1)
        InventoryManager.setItem(1, 1, 1, 2)
        InventoryManager.setItem(1, 5, 5, 3)

        InventoryManager.deleteRegion(1, 0, 0, 1, 1)

        assertNull(InventoryManager.getSlot(1, 0, 0))
        assertNull(InventoryManager.getSlot(1, 1, 1))
        assertNotNull(InventoryManager.getSlot(1, 5, 5))
    }

    @Test
    fun `batchRemove removes given positions`() {
        InventoryManager.setItem(1, 0, 0, 1)
        InventoryManager.setItem(1, 1, 0, 2)
        InventoryManager.setItem(1, 2, 2, 3)

        InventoryManager.batchRemove(1, setOf(0 to 0, 1 to 0))

        assertNull(InventoryManager.getSlot(1, 0, 0))
        assertNull(InventoryManager.getSlot(1, 1, 0))
        assertNotNull(InventoryManager.getSlot(1, 2, 2))
    }

    @Test
    fun `batchSetItems inserts and updates`() {
        InventoryManager.setItem(1, 0, 0, 1)

        InventoryManager.batchSetItems(1, listOf(Triple(0, 0, 99), Triple(1, 1, 2)))

        assertEquals(99, InventoryManager.getSlot(1, 0, 0)!!.itemId)
        assertEquals(2, InventoryManager.getSlot(1, 1, 1)!!.itemId)
    }

    @Test
    fun `isOccupied reflects slot presence`() {
        assertFalse(InventoryManager.isOccupied(1, 0, 0))
        InventoryManager.setItem(1, 0, 0, 1)
        assertTrue(InventoryManager.isOccupied(1, 0, 0))
    }

    @Test
    fun `deleteAllForProfile removes all slots`() {
        InventoryManager.setItem(1, 0, 0, 1)
        InventoryManager.setItem(1, 5, 5, 2)
        InventoryManager.setItem(2, 0, 0, 3)

        InventoryManager.deleteAllForProfile(1)

        assertTrue(InventoryManager.allSlots(1).isEmpty())
        assertEquals(1, InventoryManager.allSlots(2).size)
    }

    @Test
    fun `allSlotsForMaterial returns matching slots`() {
        val id1 = TestDatabase.insertRawItem("STONE")
        val id2 = TestDatabase.insertRawItem("DIRT")
        InventoryManager.setItem(1, 0, 0, id1)
        InventoryManager.setItem(1, 1, 1, id1)
        InventoryManager.setItem(1, 2, 2, id2)

        val slots = InventoryManager.allSlotsForMaterial(1, "STONE")
        assertEquals(2, slots.size)
    }
}
