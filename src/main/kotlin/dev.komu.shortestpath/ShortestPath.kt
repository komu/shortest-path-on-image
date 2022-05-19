package dev.komu.shortestpath

import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun main() {
    val image = ImageIO.read(File("input/input.png"))

    val g = image.graphics

    val start = Point(570, 100)
    val end = Point(320, 300)

    g.color = Color.BLACK

    val path = findPath(image, start, end)
    if (path == null) {
        println("no path")
        return
    }

    for (p in path)
        g.fillOval(p.x, p.y, 5, 5)

    ImageIO.write(image, "png", File("output/output.png"))
}

fun findPath(image: BufferedImage, from: Point, target: Point): List<Point>? =
    shortestPathWithCost(from, { it == target }) { p ->
        directions.map { d -> Pair(Point(p.x + d.dx, p.y + d.dy), d.cost) }
            .filter { (p, _) -> isValidPoint(image, p) }
    }?.let { (p, _) -> p }

fun isValidPoint(image: BufferedImage, point: Point) =
    point.x in (0 until image.width) && point.y in (0 until image.height) && !isRed(image.getRGB(point.x, point.y))

fun isRed(color: Int): Boolean {
    val r = ((color shr 16) and 0xff)
    val g = ((color shr 8) and 0xff)
    val b = (color and 0xff)
    return r > 0xe0 && b <= 0x40 && g <= 0x40
}

class Direction(val dx: Int, val dy: Int) {
    val cost = (sqrt((dx * dx + dy * dy).toDouble()) * 100).toInt()
}

val directions = listOf(
    Direction(-1, 0),
    Direction(1, 0),
    Direction(0, -1),
    Direction(0, 1),
    Direction(1, -1),
    Direction(1, 1),
    Direction(-1, 1),
    Direction(-1, -1),
    Direction(2, -1),
    Direction(2, 1),
    Direction(-2, 1),
    Direction(-2, -1),
    Direction(1, -2),
    Direction(1, 2),
    Direction(-1, 2),
    Direction(-1, -2),
)

fun <T> shortestPathWithCost(from: T, isTarget: (T) -> Boolean, edges: (T) -> List<Pair<T, Int>>): Pair<List<T>, Int>? {
    val initial = PathNode(from, null, 0)
    val nodes = mutableMapOf(from to initial)
    val queue = PriorityQueue(setOf(initial))
    val targets = mutableSetOf<T>()

    while (queue.isNotEmpty()) {
        val u = queue.remove()

        for ((v, cost) in edges(u.point)) {
            if (isTarget(v))
                targets += v

            val newDistance = u.distance + cost
            val previousNode = nodes[v]
            if (previousNode == null || newDistance < previousNode.distance) {
                val newNode = PathNode(v, u, newDistance)
                nodes[v] = newNode
                queue += newNode
            }
        }
    }

    val targetNode = targets.map { nodes[it]!! }.minByOrNull { it.distance }
    if (targetNode != null) {
        val result = mutableListOf<T>()
        var node: PathNode<T>? = targetNode
        while (node?.previous != null) {
            result += node.point
            node = node.previous
        }
        return result.asReversed() to targetNode.distance
    }
    return null
}

private class PathNode<T>(val point: T, val previous: PathNode<T>?, val distance: Int) : Comparable<PathNode<T>> {
    override fun compareTo(other: PathNode<T>) = distance.compareTo(other.distance)
}