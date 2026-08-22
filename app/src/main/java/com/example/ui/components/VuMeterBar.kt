package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.VuGreen
import com.example.ui.theme.VuRed
import com.example.ui.theme.VuYellow

@Composable
fun VuMeterBar(
    leftLevel: Float, // 0.0 to 1.0
    rightLevel: Float, // 0.0 to 1.0
    isClipping: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkCanvas)
            .border(1.dp, GlassBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = "VU",
            color = if (isClipping) VuRed else Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.width(60.dp)
        ) {
            // Left Channel LED Meter
            SingleLedChannel(level = leftLevel)
            // Right Channel LED Meter
            SingleLedChannel(level = rightLevel)
        }

        // Peak Clip Indicator Lamp
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isClipping) VuRed else Color(0xFF330000))
                .border(0.5.dp, if (isClipping) Color.White else Color.Transparent, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun SingleLedChannel(level: Float) {
    val totalSegments = 12
    val activeCount = (level.coerceIn(0f, 1f) * totalSegments).toInt()

    Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
    ) {
        for (i in 0 until totalSegments) {
            val isLit = (i < activeCount)
            val segmentColor = when {
                i >= 10 -> VuRed
                i >= 7 -> VuYellow
                else -> VuGreen
            }

            val color = if (isLit) segmentColor else segmentColor.copy(alpha = 0.15f)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}
