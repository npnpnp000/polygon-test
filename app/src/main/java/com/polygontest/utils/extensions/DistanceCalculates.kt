package com.polygontest.utils.extensions

import android.location.Location
import android.location.LocationManager
import androidx.core.math.MathUtils.clamp
import com.polygontest.model.ui_models.GpsLocation
import com.polygontest.model.ui_models.Line
import java.lang.Double.min
import java.util.*
import kotlin.collections.ArrayList


class DistanceCalculates {

    //A solution from the Internet
    // https://www.geeksforgeeks.org/how-to-check-if-a-given-point-lies-inside-a-polygon/?fbclid=IwAR2pxmZAgNGICdo-ZFlT-NWKYq6KBl-wVwp-VSoYO6EzWarIpLvhnakUIp0
    fun checkInside(poly: ArrayList<GpsLocation>, n: Int, p: GpsLocation): Int {

        if (n < 3) return 0

        // Create a point at infinity, y is same as point p
        val pt = GpsLocation(9999.0, p.longitude)
        val exline = Line(p, pt)
        var count = 0
        var i = 0
        do {

            // Forming a line from two consecutive points of
            // poly
            val side = Line(
                poly[i],
                poly[(i + 1) % n]
            )
            if (isIntersect(side, exline) == 1) {

                // If side is intersects exline
                if (direction(side.p1, p, side.p2) == 0) return onLine(side, p)
                count++
            }
            i = (i + 1) % n
        } while (i != 0)

        // When count is odd
        return count and 1
    }

    private fun onLine(l1: Line, p: GpsLocation): Int {
        // Check whether p is on the line or not
        return if (p.latitude <= Math.max(l1.p1.latitude, l1.p2.latitude) && p.latitude >= Math.min(
                l1.p1.latitude,
                l1.p2.latitude
            ) && (p.longitude <= Math.max(l1.p1.longitude, l1.p2.longitude)
                    && p.longitude >= Math.min(l1.p1.longitude, l1.p2.longitude))
        ) 1 else 0
    }

    private fun direction(a: GpsLocation, b: GpsLocation, c: GpsLocation): Int {
        val `val` = ((b.longitude - a.longitude) * (c.latitude - b.latitude)
                - (b.latitude - a.latitude) * (c.longitude - b.longitude)).toInt()
        if (`val` == 0) // Collinear
            return 0 else if (`val` < 0) // Anti-clockwise direction
            return 2

        // Clockwise direction
        return 1
    }

    private fun isIntersect(l1: Line, l2: Line): Int {
        // Four direction for two lines and points of other
        // line
        val dir1 = direction(l1.p1, l1.p2, l2.p1)
        val dir2 = direction(l1.p1, l1.p2, l2.p2)
        val dir3 = direction(l2.p1, l2.p2, l1.p1)
        val dir4 = direction(l2.p1, l2.p2, l1.p2)

        // When intersecting
        if (dir1 != dir2 && dir3 != dir4) return 1

        // When p2 of line2 are on the line1
        if (dir1 == 0 && onLine(l1, l2.p1) == 1) return 1

        // When p1 of line2 are on the line1
        if (dir2 == 0 && onLine(l1, l2.p2) == 1) return 1

        // When p2 of line1 are on the line2
        if (dir3 == 0 && onLine(l2, l1.p1) == 1) return 1

        // When p1 of line1 are on the line2
        return if (dir4 == 0 && onLine(l2, l1.p2) == 1) 1 else 0
    }


    fun getMinDistance(poly: ArrayList<GpsLocation>, p: GpsLocation): Double?{

        var line : Line
        var minDistance :Double? = null

        for (i in 0 until poly.size){
            if (i != poly.size-1){
                 line = Line(poly[i], poly[i+1])
            }else{
                line = Line(poly[i], poly[0])
            }

            val d = getDistance(line,p)

            if (minDistance != null){
                minDistance = min(minDistance,d)
            }else{
                minDistance = d
            }
        }
        return minDistance
    }

    private fun getDistance(line: Line, point: GpsLocation): Double {

//        Latitude: 1 deg = 110.574 km
//        Longitude: 1 deg = 111.320*cos(latitude) km


        return getDistance(point.latitude,point.longitude,line.p1.latitude,line.p1.longitude,
            line.p2.latitude,line.p2.longitude)
    }

    //A solution from the Internet - stackoverflow
    private fun getDistance(x: Double, y: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val a = x - x1
        val b = y - y1
        val c = x2 - x1
        val d = y2 - y1

        val lenSq = c * c + d * d
        val param = if (lenSq != .0) { //in case of 0 length line
            val dot = a * c + b * d
            dot / lenSq
        } else {
            -1.0
        }

        val (xx, yy) = when {
            param < 0 -> x1 to y1
            param > 1 -> x2 to y2
            else -> x1 + param * c to y1 + param * d
        }

        val dx = x - xx
        val dy = y - yy
        return Math.hypot(dx, dy)
    }
}