package dev.cypdashuhn.extendedinventory.actions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GroupOperationLogicTest {
    @Test
    fun `corner normalization works regardless of order`() {
        val x1 = 5
        val y1 = 10
        val x2 = -3
        val y2 = 7
        val minX = minOf(x1, x2)
        val maxX = maxOf(x1, x2)
        val minY = minOf(y1, y2)
        val maxY = maxOf(y1, y2)

        assertEquals(-3, minX)
        assertEquals(5, maxX)
        assertEquals(7, minY)
        assertEquals(10, maxY)
    }

    @Test
    fun `target preview computes correct region`() {
        val ax = 0
        val ay = 0
        val bx = 2
        val by = 1
        val tx = 10
        val ty = 20

        val minX = minOf(ax, bx)
        val maxX = maxOf(ax, bx)
        val minY = minOf(ay, by)
        val maxY = maxOf(ay, by)

        val positions = (minX..maxX)
            .flatMap { sx ->
                (minY..maxY).map { sy ->
                    (tx + (sx - minX)) to (ty + (sy - minY))
                }
            }.toSet()

        val expected = setOf(
            10 to 20, 11 to 20, 12 to 20,
            10 to 21, 11 to 21, 12 to 21,
        )

        assertEquals(expected, positions)
        assertEquals(6, positions.size)
    }

    @Test
    fun `target preview with negative source handles correctly`() {
        val ax = -2
        val ay = -1
        val bx = 0
        val by = 1
        val tx = 5
        val ty = 5

        val minX = minOf(ax, bx)
        val maxX = maxOf(ax, bx)
        val minY = minOf(ay, by)
        val maxY = maxOf(ay, by)

        val positions = (minX..maxX)
            .flatMap { sx ->
                (minY..maxY).map { sy ->
                    (tx + (sx - minX)) to (ty + (sy - minY))
                }
            }.toSet()

        val expected = setOf(
            5 to 5, 6 to 5, 7 to 5,
            5 to 6, 6 to 6, 7 to 6,
            5 to 7, 6 to 7, 7 to 7,
        )

        assertEquals(expected, positions)
        assertEquals(9, positions.size)
    }

    @Test
    fun `single slot region has one position`() {
        val ax = 3
        val ay = 3
        val positions = (ax..ax).flatMap { sx -> (ay..ay).map { sy -> sx to sy } }.toSet()

        assertEquals(setOf(3 to 3), positions)
    }

    @Test
    fun `swapped corners produce same region`() {
        fun region(x1: Int, y1: Int, x2: Int, y2: Int): Set<Pair<Int, Int>> {
            val minX = minOf(x1, x2)
            val maxX = maxOf(x1, x2)
            val minY = minOf(y1, y2)
            val maxY = maxOf(y1, y2)
            return (minX..maxX).flatMap { x -> (minY..maxY).map { y -> x to y } }.toSet()
        }

        assertEquals(region(0, 0, 3, 2), region(3, 2, 0, 0))
        assertEquals(region(-1, -1, 1, 1), region(1, -1, -1, 1))
    }
}
