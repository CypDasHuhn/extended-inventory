package dev.cypdashuhn.extendedinventory.actions

import dev.cypdashuhn.extendedinventory.TestDatabase
import dev.cypdashuhn.extendedinventory.db.InventoryManager
import dev.cypdashuhn.extendedinventory.db.SlotCache
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InventoryActionsTest {
    private val profileId = 1

    @BeforeEach
    fun setUp() {
        TestDatabase.clear()
    }

    @Test
    fun `getItem returns null for empty slot`() {
        assertNull(InventoryActions.getItem(profileId, 0, 0))
    }

    @Test
    fun `setItem null removes slot`() {
        val id = TestDatabase.insertRawItem()
        InventoryManager.setItem(profileId, 2, 3, id)

        InventoryActions.setItem(profileId, 2, 3, null)

        assertNull(SlotCache.getSlot(profileId, 2, 3))
        assertNull(InventoryManager.getSlot(profileId, 2, 3))
    }

    @Test
    fun `removeItemAt removes slot`() {
        val id = TestDatabase.insertRawItem()
        InventoryManager.setItem(profileId, 2, 3, id)

        InventoryActions.removeItemAt(profileId, 2, 3)

        assertNull(SlotCache.getSlot(profileId, 2, 3))
    }

    @Test
    fun `removeItemAt deletes now-unreferenced item`() {
        val id = TestDatabase.insertRawItem()
        InventoryManager.setItem(profileId, 5, 5, id)

        InventoryActions.removeItemAt(profileId, 5, 5)

        assertFalse(TestDatabase.itemExists(id))
    }

    @Test
    fun `removeItemAt keeps item shared by another slot`() {
        val id = TestDatabase.insertRawItem()
        InventoryManager.setItem(profileId, 1, 1, id)
        InventoryManager.setItem(profileId, 2, 2, id)

        InventoryActions.removeItemAt(profileId, 1, 1)

        assertTrue(TestDatabase.itemExists(id))
        assertNotNull(InventoryManager.getSlot(profileId, 2, 2))
    }

    @Test
    fun `setItemId stores item at position`() {
        val id = TestDatabase.insertRawItem()
        InventoryActions.setItemId(profileId, 3, 4, id)

        val slot = SlotCache.getSlot(profileId, 3, 4)
        assertNotNull(slot)
        assertEquals(id, slot!!.itemId)
    }

    @Test
    fun `setItemId replacing item deletes orphaned old item`() {
        val oldId = TestDatabase.insertRawItem()
        val newId = TestDatabase.insertRawItem("DIRT")
        InventoryManager.setItem(profileId, 1, 1, oldId)

        InventoryActions.setItemId(profileId, 1, 1, newId)

        assertFalse(TestDatabase.itemExists(oldId))
        assertEquals(newId, SlotCache.getSlot(profileId, 1, 1)!!.itemId)
    }

    @Test
    fun `setItemId with same id does not delete item`() {
        val id = TestDatabase.insertRawItem()
        InventoryManager.setItem(profileId, 1, 1, id)

        InventoryActions.setItemId(profileId, 1, 1, id)

        assertTrue(TestDatabase.itemExists(id))
    }

    @Test
    fun `groupDelete removes items only within region`() {
        InventoryManager.setItem(profileId, 0, 0, TestDatabase.insertRawItem())
        InventoryManager.setItem(profileId, 1, 1, TestDatabase.insertRawItem())
        InventoryManager.setItem(profileId, 5, 5, TestDatabase.insertRawItem())

        InventoryActions.groupDelete(profileId, 0, 0, 1, 1)

        assertNull(InventoryManager.getSlot(profileId, 0, 0))
        assertNull(InventoryManager.getSlot(profileId, 1, 1))
        assertNotNull(InventoryManager.getSlot(profileId, 5, 5))
    }

    @Test
    fun `groupMove relocates items to target region`() {
        val stone = TestDatabase.insertRawItem()
        val dirt = TestDatabase.insertRawItem("DIRT")
        InventoryManager.setItem(profileId, 0, 0, stone)
        InventoryManager.setItem(profileId, 1, 0, dirt)

        val moved = InventoryActions.groupMove(profileId, 0, 0, 1, 0, 10, 20)

        assertTrue(moved)
        assertNull(InventoryManager.getSlot(profileId, 0, 0))
        assertNull(InventoryManager.getSlot(profileId, 1, 0))
        assertEquals(stone, InventoryManager.getSlot(profileId, 10, 20)!!.itemId)
        assertEquals(dirt, InventoryManager.getSlot(profileId, 11, 20)!!.itemId)
    }

    @Test
    fun `groupMove with no items returns false`() {
        val moved = InventoryActions.groupMove(profileId, 0, 0, 5, 5, 50, 50)
        assertFalse(moved)
    }

    @Test
    fun `getRegionSlots returns slots in region`() {
        InventoryManager.setItem(profileId, 0, 0, TestDatabase.insertRawItem())
        InventoryManager.setItem(profileId, 3, 3, TestDatabase.insertRawItem())
        InventoryManager.setItem(profileId, 10, 10, TestDatabase.insertRawItem())

        val slots = InventoryActions.getRegionSlots(profileId, 0, 0, 4, 4)
        assertEquals(2, slots.size)
    }
}
