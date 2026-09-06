package dev.cypdashuhn.extendedinventory.util

data class Region(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int
) {
    val positions: Set<Pair<Int, Int>> by lazy {
        (minX..maxX).flatMap { x -> (minY..maxY).map { y -> x to y } }.toSet()
    }
}

fun region(x1: Int, y1: Int, x2: Int, y2: Int): Region = Region(minOf(x1, x2), minOf(y1, y2), maxOf(x1, x2), maxOf(y1, y2))
