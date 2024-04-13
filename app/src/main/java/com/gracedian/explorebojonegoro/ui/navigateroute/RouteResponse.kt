package com.gracedian.explorebojonegoro.ui.navigateroute

data class RouteResponse(
    val route: Route
)

data class Route(
    val shape: Shape
)

data class Shape(
    val shapePoints: List<Double>
)
