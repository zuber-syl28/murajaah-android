package com.sylergy.murajaah.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Official Murajaah Vector Icons:
 * Sourced directly from Murajaah design system (24px grid, 1.8dp stroke).
 * Replaces system emojis with crisp, tintable, authentic Qur'anic vectors.
 */
object MurajaahIcons {

    @Composable
    fun Revision(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF2F4A3D)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val scale = this.size.width / 24f
            val strokeWidth = 1.8f * scale

            val leftPage = Path().apply {
                moveTo(2f * scale, 4f * scale)
                lineTo(8f * scale, 4f * scale)
                cubicTo(10.2f * scale, 4f * scale, 12f * scale, 5.8f * scale, 12f * scale, 8f * scale)
                lineTo(12f * scale, 21f * scale)
                cubicTo(10.3f * scale, 19.8f * scale, 8.3f * scale, 18f * scale, 2f * scale, 18f * scale)
                close()
            }
            val rightPage = Path().apply {
                moveTo(22f * scale, 4f * scale)
                lineTo(16f * scale, 4f * scale)
                cubicTo(13.8f * scale, 4f * scale, 12f * scale, 5.8f * scale, 12f * scale, 8f * scale)
                lineTo(12f * scale, 21f * scale)
                cubicTo(13.7f * scale, 19.8f * scale, 15.7f * scale, 18f * scale, 22f * scale, 18f * scale)
                close()
            }

            drawPath(
                path = leftPage,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = rightPage,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }

    @Composable
    fun Progress(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF2F4A3D)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val scale = this.size.width / 24f
            val strokeWidth = 2.0f * scale

            // Three vertical progress/retention bars
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(6f * scale, 20f * scale),
                end = androidx.compose.ui.geometry.Offset(6f * scale, 14f * scale),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(12f * scale, 20f * scale),
                end = androidx.compose.ui.geometry.Offset(12f * scale, 4f * scale),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(18f * scale, 20f * scale),
                end = androidx.compose.ui.geometry.Offset(18f * scale, 10f * scale),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }

    @Composable
    fun Friends(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF2F4A3D)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val scale = this.size.width / 24f
            val strokeWidth = 1.8f * scale

            // Primary user head & shoulders
            drawCircle(
                color = tint,
                radius = 4f * scale,
                center = androidx.compose.ui.geometry.Offset(9f * scale, 7f * scale),
                style = Stroke(width = strokeWidth)
            )
            val body = Path().apply {
                moveTo(17f * scale, 21f * scale)
                lineTo(17f * scale, 19f * scale)
                cubicTo(17f * scale, 16.8f * scale, 15.2f * scale, 15f * scale, 13f * scale, 15f * scale)
                lineTo(5f * scale, 15f * scale)
                cubicTo(2.8f * scale, 15f * scale, 1f * scale, 16.8f * scale, 1f * scale, 19f * scale)
                lineTo(1f * scale, 21f * scale)
            }
            drawPath(
                path = body,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Companion shoulder line
            val companionBody = Path().apply {
                moveTo(23f * scale, 21f * scale)
                lineTo(23f * scale, 19f * scale)
                cubicTo(23f * scale, 17.1f * scale, 21.6f * scale, 15.6f * scale, 20f * scale, 15.2f * scale)
            }
            drawPath(
                path = companionBody,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }

    @Composable
    fun Bell(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF2F4A3D)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val scale = this.size.width / 24f
            val strokeWidth = 1.8f * scale

            val bellPath = Path().apply {
                moveTo(18f * scale, 8f * scale)
                cubicTo(18f * scale, 4.7f * scale, 15.3f * scale, 2f * scale, 12f * scale, 2f * scale)
                cubicTo(8.7f * scale, 2f * scale, 6f * scale, 4.7f * scale, 6f * scale, 8f * scale)
                cubicTo(6f * scale, 15f * scale, 3f * scale, 17f * scale, 3f * scale, 17f * scale)
                lineTo(21f * scale, 17f * scale)
                cubicTo(21f * scale, 17f * scale, 18f * scale, 15f * scale, 18f * scale, 8f * scale)
            }
            drawPath(
                path = bellPath,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Clapper
            drawArc(
                color = tint,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(10.3f * scale, 18f * scale),
                size = androidx.compose.ui.geometry.Size(3.4f * scale, 3.4f * scale),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }

    @Composable
    fun Settings(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = Color(0xFF2F4A3D)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val scale = this.size.width / 24f
            val strokeWidth = 1.8f * scale

            // Center circle
            drawCircle(
                color = tint,
                radius = 3f * scale,
                center = androidx.compose.ui.geometry.Offset(12f * scale, 12f * scale),
                style = Stroke(width = strokeWidth)
            )
            // Outer gear rim
            drawCircle(
                color = tint,
                radius = 7.5f * scale,
                center = androidx.compose.ui.geometry.Offset(12f * scale, 12f * scale),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }

    @Composable
    fun Mic(
        modifier: Modifier = Modifier,
        size: Dp = 16.dp,
        tint: Color = Color(0xFF2F4A3D)
    ) {
        Canvas(modifier = modifier.size(size)) {
            val scale = this.size.width / 24f
            val strokeWidth = 1.8f * scale

            val micCapsule = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = androidx.compose.ui.geometry.Rect(9f * scale, 2f * scale, 15f * scale, 14f * scale),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale, 3f * scale)
                    )
                )
            }
            drawPath(
                path = micCapsule,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Base cradle
            val cradle = Path().apply {
                moveTo(5f * scale, 10f * scale)
                cubicTo(5f * scale, 14f * scale, 8f * scale, 17f * scale, 12f * scale, 17f * scale)
                cubicTo(16f * scale, 17f * scale, 19f * scale, 14f * scale, 19f * scale, 10f * scale)
            }
            drawPath(
                path = cradle,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(12f * scale, 17f * scale),
                end = androidx.compose.ui.geometry.Offset(12f * scale, 22f * scale),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }

    @Composable
    fun Listen(
        modifier: Modifier = Modifier,
        size: Dp = 16.dp,
        tint: Color = Color.White
    ) {
        Canvas(modifier = modifier.size(size)) {
            val scale = this.size.width / 24f
            val strokeWidth = 1.8f * scale

            // Headband
            val band = Path().apply {
                moveTo(3f * scale, 18f * scale)
                lineTo(3f * scale, 12f * scale)
                cubicTo(3f * scale, 7f * scale, 7f * scale, 3f * scale, 12f * scale, 3f * scale)
                cubicTo(17f * scale, 3f * scale, 21f * scale, 7f * scale, 21f * scale, 12f * scale)
                lineTo(21f * scale, 18f * scale)
            }
            drawPath(
                path = band,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Left ear cup
            drawRoundRect(
                color = tint,
                topLeft = androidx.compose.ui.geometry.Offset(2f * scale, 14f * scale),
                size = androidx.compose.ui.geometry.Size(3f * scale, 6f * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f * scale, 1f * scale),
                style = Stroke(width = strokeWidth)
            )
            // Right ear cup
            drawRoundRect(
                color = tint,
                topLeft = androidx.compose.ui.geometry.Offset(19f * scale, 14f * scale),
                size = androidx.compose.ui.geometry.Size(3f * scale, 6f * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f * scale, 1f * scale),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
