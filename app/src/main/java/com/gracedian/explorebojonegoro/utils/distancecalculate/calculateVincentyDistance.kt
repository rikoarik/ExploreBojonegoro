package com.gracedian.explorebojonegoro.utils.distancecalculate

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

fun calculateVincentyDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val a = 6378137.0 // Radius semi-mayor Bumi dalam meter
    val b = 6356752.3142 // Radius semi-minor Bumi dalam meter
    val f = 1 / 298.257223563 // Flattening

    val phi1 = Math.toRadians(lat1)
    val lambda1 = Math.toRadians(lon1)
    val phi2 = Math.toRadians(lat2)
    val lambda2 = Math.toRadians(lon2)

    val L = lambda2 - lambda1

    var tanU1 = (1 - f) * tan(phi1)
    val cosU1 = 1 / sqrt(1 + tanU1 * tanU1)
    val sinU1 = tanU1 * cosU1

    var tanU2 = (1 - f) * tan(phi2)
    val cosU2 = 1 / sqrt(1 + tanU2 * tanU2)
    val sinU2 = tanU2 * cosU2

    var lambda = L
    var lambdaP: Double
    var iterLimit = 100
    var cosAlpha: Double
    var sinSigma: Double
    var cosSigma: Double
    var sigma: Double
    var cos2SigmaM: Double
    var cosSigmaM: Double

    do {
        val sinLambda = sin(lambda)
        val cosLambda = cos(lambda)

        sinSigma = sqrt((cosU2 * sinLambda).pow(2) + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda).pow(2))
        cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
        sigma = atan2(sinSigma, cosSigma)

        val sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
        cosAlpha = 1 - sinAlpha * sinAlpha

        cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosAlpha
        val C = f / 16 * cosAlpha * (4 + f * (4 - 3 * cosAlpha))
        lambdaP = lambda
        lambda = L + (1 - C) * f * sinAlpha * (sigma + C * sinAlpha * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM.pow(2))))
    } while (abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0)

    if (iterLimit == 0) {
        throw IllegalStateException("Vincenty formula failed to converge")
    }

    val u2 = cosAlpha * (a * a - b * b) / (b * b)
    val A = 1 + u2 / 16384 * (4096 + u2 * (-768 + u2 * (320 - 175 * u2)))
    val B = u2 / 1024 * (256 + u2 * (-128 + u2 * (74 - 47 * u2)))
    val deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma.pow(2)) * (-3 + 4 * cos2SigmaM * cos2SigmaM)))

    val s = b * A * (sigma - deltaSigma)

    return s
}