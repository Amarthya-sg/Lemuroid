package com.swordfish.touchinput.radial.controls

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/** Physical side of an asymmetric shoulder button. The right silhouette mirrors the left. */
enum class ShoulderControlSide {
    LEFT,
    RIGHT,
}

class ShoulderControlShape(
    private val side: ShoulderControlSide,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val width = size.width
        val height = size.height
        val mirror = side == ShoulderControlSide.RIGHT
        fun x(fraction: Float) = width * if (mirror) 1f - fraction else fraction
        fun y(fraction: Float) = height * fraction

        val path = Path().apply {
            moveTo(x(0.09f), y(0.43f))
            cubicTo(x(0.06f), y(0.33f), x(0.14f), y(0.24f), x(0.29f), y(0.24f))
            lineTo(x(0.76f), y(0.24f))
            cubicTo(x(0.91f), y(0.24f), x(0.98f), y(0.32f), x(0.98f), y(0.45f))
            lineTo(x(0.98f), y(0.55f))
            cubicTo(x(0.98f), y(0.68f), x(0.91f), y(0.76f), x(0.78f), y(0.73f))
            cubicTo(x(0.60f), y(0.68f), x(0.47f), y(0.69f), x(0.31f), y(0.79f))
            cubicTo(x(0.16f), y(0.89f), x(0.05f), y(0.79f), x(0.03f), y(0.65f))
            cubicTo(x(0.02f), y(0.55f), x(0.04f), y(0.48f), x(0.09f), y(0.43f))
            close()
        }
        return Outline.Generic(path)
    }
}
