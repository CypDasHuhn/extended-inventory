package dev.cypdashuhn.extendedinventory.actions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CycleActionsTest {
    @Test
    fun `empty cells produce an empty chain`() {
        assertEquals(emptyList<Pair<Int, Int>>(), CycleActions.buildCycleChain(emptyList(), 0 to 0))
    }

    @Test
    fun `single cell produces a one-element chain`() {
        assertEquals(listOf(4 to 4), CycleActions.buildCycleChain(listOf(4 to 4), 4 to 4))
    }

    @Test
    fun `chain starts at the start cell when present`() {
        val chain = CycleActions.buildCycleChain(listOf(0 to 0, 5 to 0, 10 to 0), 0 to 0)
        assertEquals(0 to 0, chain.first())
    }

    @Test
    fun `chain orders cells by nearest neighbour`() {
        val chain = CycleActions.buildCycleChain(listOf(0 to 0, 10 to 0, 3 to 0), 0 to 0)
        assertEquals(listOf(0 to 0, 3 to 0, 10 to 0), chain)
    }

    @Test
    fun `chain visits every cell exactly once`() {
        val cells = listOf(0 to 0, 1 to 1, 5 to 5, -3 to 2, 7 to -4)
        val chain = CycleActions.buildCycleChain(cells, 0 to 0)
        assertEquals(cells.toSet(), chain.toSet())
        assertEquals(cells.size, chain.size)
    }

    @Test
    fun `start outside cells anchors to the nearest cell`() {
        val chain = CycleActions.buildCycleChain(listOf(10 to 0, 20 to 0), 0 to 0)
        assertEquals(10 to 0, chain.first())
    }

    @Test
    fun `equal distances are broken by coordinate`() {
        val chain = CycleActions.buildCycleChain(listOf(0 to 0, 1 to 0, 0 to 1, 2 to 2), 0 to 0)
        assertEquals(listOf(0 to 0, 0 to 1, 1 to 0, 2 to 2), chain)
    }
}
