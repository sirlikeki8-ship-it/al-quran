package com.likeki.alquran.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.likeki.alquran.R
import kotlin.random.Random

@Composable
fun NightSky(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Owner photo, faintly visible
        Image(
            painter = painterResource(R.drawable.owner_photo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Layer 2: Night sky gradient — semi-transparent so photo glows through
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.verticalGradient(
                    0f to Color(0xFF0A1435).copy(alpha = 0.88f),
                    0.55f to Color(0xFF0B2A3A).copy(alpha = 0.80f),
                    1f to Color(0xFF0B3D2E).copy(alpha = 0.92f)
                )
            )
            // Stars
            val rng = Random(42)
            repeat(120) {
                val x = rng.nextFloat() * size.width
                val y = rng.nextFloat() * size.height * 0.7f
                val r = rng.nextFloat() * 1.3f + 0.3f
                val a = rng.nextFloat() * 0.6f + 0.3f
                drawCircle(Color(0xFFF5EFE6).copy(alpha = a), r, Offset(x, y))
            }
            // Bright 4-point stars
            listOf(
                Offset(size.width * 0.15f, size.height * 0.12f),
                Offset(size.width * 0.32f, size.height * 0.28f),
                Offset(size.width * 0.48f, size.height * 0.08f),
                Offset(size.width * 0.72f, size.height * 0.18f),
                Offset(size.width * 0.88f, size.height * 0.35f)
            ).forEach { drawBrightStar(it) }
            // Crescent moon
            drawCrescentMoon(Offset(size.width * 0.82f, size.height * 0.13f), size.width * 0.09f)
            // Clouds
            drawClouds(size.width, size.height)
        }
    }
}

private fun DrawScope.drawBrightStar(center: Offset) {
    val s = 6f
    val c = Color(0xFFE8C97A).copy(alpha = 0.9f)
    drawCircle(c, s * 0.35f, center)
    drawLine(c, Offset(center.x, center.y - s), Offset(center.x, center.y + s), 1.2f)
    drawLine(c, Offset(center.x - s, center.y), Offset(center.x + s, center.y), 1.2f)
}

private fun DrawScope.drawCrescentMoon(center: Offset, radius: Float) {
    drawCircle(Color(0xFFE8C97A).copy(alpha = 0.85f), radius, center)
    drawCircle(Color(0xFF0A1435), radius * 0.85f, Offset(center.x + radius * 0.55f, center.y - radius * 0.15f))
    drawCircle(Color(0xFFE8C97A).copy(alpha = 0.10f), radius * 1.6f, center)
}

private fun DrawScope.drawClouds(w: Float, h: Float) {
    val c = Color(0xFF1B4D5C).copy(alpha = 0.22f)
    drawOval(c, Offset(-w * 0.2f, h * 0.78f), Size(w * 0.8f, h * 0.06f))
    drawOval(c, Offset(w * 0.5f, h * 0.82f), Size(w * 0.7f, h * 0.05f))
    drawOval(c.copy(alpha = 0.16f), Offset(-w * 0.1f, h * 0.88f), Size(w * 1.1f, h * 0.05f))
}
