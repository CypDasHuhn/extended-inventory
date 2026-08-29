package dev.cypdashuhn.extendedinventory.db

import dev.cypdashuhn.extendedinventory.TestDatabase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AnchorManagerTest {

    @BeforeEach
    fun setUp() {
        TestDatabase.clear()
    }

    @Test
    fun `create and findById`() {
        val id = AnchorManager.create(1, "flowers", 3, 4)
        val anchor = AnchorManager.findById(id)

        assertNotNull(anchor)
        assertEquals("flowers", anchor!!.name)
        assertEquals(3, anchor.x)
        assertEquals(4, anchor.y)
        assertEquals(1, anchor.profileId)
    }

    @Test
    fun `findByName scopes to profile`() {
        AnchorManager.create(1, "shared", 0, 0)
        AnchorManager.create(2, "shared", 9, 9)

        val anchor = AnchorManager.findByName(1, "shared")
        assertEquals(0, anchor!!.x)
        assertEquals(0, anchor.y)
    }

    @Test
    fun `findByName returns null when missing`() {
        assertNull(AnchorManager.findByName(1, "nope"))
    }

    @Test
    fun `findAtPosition finds anchor at coordinate`() {
        AnchorManager.create(1, "a", 5, 6)
        assertNull(AnchorManager.findAtPosition(1, 0, 0))

        val anchor = AnchorManager.findAtPosition(1, 5, 6)
        assertNotNull(anchor)
        assertEquals("a", anchor!!.name)
    }

    @Test
    fun `allForProfile returns only that profile's anchors`() {
        AnchorManager.create(1, "a", 0, 0)
        AnchorManager.create(1, "b", 1, 1)
        AnchorManager.create(2, "c", 0, 0)

        assertEquals(2, AnchorManager.allForProfile(1).size)
    }

    @Test
    fun `rename updates name`() {
        val id = AnchorManager.create(1, "old", 0, 0)
        AnchorManager.rename(id, "new")

        assertEquals("new", AnchorManager.findById(id)!!.name)
    }

    @Test
    fun `delete removes anchor and clears slot references`() {
        val id = AnchorManager.create(1, "a", 0, 0)
        InventoryManager.setAnchor(1, 0, 0, id)

        AnchorManager.delete(id)

        assertNull(AnchorManager.findById(id))
        val slot = InventoryManager.getSlot(1, 0, 0)
        assertNotNull(slot)
        assertNull(slot!!.anchorId)
    }

    @Test
    fun `deleteAllForProfile removes anchors`() {
        AnchorManager.create(1, "a", 0, 0)
        AnchorManager.create(1, "b", 1, 1)
        AnchorManager.create(2, "c", 0, 0)

        AnchorManager.deleteAllForProfile(1)

        assertTrue(AnchorManager.allForProfile(1).isEmpty())
        assertEquals(1, AnchorManager.allForProfile(2).size)
    }
}
