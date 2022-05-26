package dev.komu.shortestpath

import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.geom.Line2D
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt
import kotlin.random.Random

fun square(x: Double) = x * x

data class Vec(val x: Double, val y: Double) {

    constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())

    operator fun plus(v: Vec) = Vec(x + v.x, y + v.y)
    operator fun minus(v: Vec) = Vec(x - v.x, y - v.y)

    fun dot(v: Vec) = x * v.x + y * v.y

    fun squaredDistance(p: Vec) = square(x - p.x) + square(y - p.y)
    fun distance(p: Vec) = sqrt(squaredDistance(p))

    companion object {
        fun random() = Vec(Random.nextDouble() * 2 - 1, Random.nextDouble() * 2 - 1)
    }
}

/** Multiply vector by a scalar */
operator fun Double.times(v: Vec) = Vec(this * v.x, this * v.y)

class LineSegment(val a: Vec, val b: Vec) {

    val length: Double
        get() = sqrt(a.squaredDistance(b))

    /** Allow calling the segment as a function mapping 0..1 to points in segment. */
    operator fun invoke(t: Double) =
        ((1 - t) * a) + (t * b)

    /**
     * Returns point on this line segment that is closest to given point.
     *
     * Basically we project the point `p` onto the vector `end - start`. If the projection
     * is within the line segment, then closest point is the projection itself. If not,
     * then the closest point is either start or end.
     */
    fun closestPoint(p: Vec): Vec {
        val v = b - a
        val u = a - p
        val vu = v.dot(u)
        val vv = v.dot(v)
        val t = -vu / vv

        return if (t in 0.0..1.0)
            this(t)
        else
            if (a.squaredDistance(p) <= b.squaredDistance(p)) a else b
    }

    fun squaredDistanceTo(p: Vec) = p.squaredDistance(closestPoint(p))

}

class Road(vararg points: Vec) {

    val segments = points.asList().zipWithNext { a, b -> LineSegment(a, b) }

    fun distanceFromStart(p: Vec): Double {
        val bestSegment = segments.minByOrNull { it.squaredDistanceTo(p) } ?: error("no segments")

        val segmentsBefore = segments.takeWhile { it != bestSegment }

        return segmentsBefore.sumOf { it.length } + bestSegment.a.distance(p)
    }
}

fun main() {

    val road = Road(
        Vec(320, 300),
        Vec(480, 425),
        Vec(520, 420),
        Vec(595, 290),
        Vec(570, 180),
        Vec(570, 100),
    )

    val image = ImageIO.read(File("input/original.jpg"))

    val g = image.graphics as Graphics2D

    // draw road
    g.color = Color.GREEN
    for (s in road.segments)
        g.draw(Line2D.Double(s.a.x, s.a.y, s.b.x, s.b.y))

    // pick random points near road and draw their distances
    for (s in road.segments) {
        repeat(3) {
            val v = s(Random.nextDouble()) + 10.0 * Vec.random()

            val distance = road.distanceFromStart(v)

            g.drawString(distance.toInt().toString(), v.x.toFloat(), v.y.toFloat())
        }
    }

    ImageIO.write(image, "png", File("output/output2.png"))
}


