package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveStatusSnapshot
import com.example.data.model.PrayerWindowCategory
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AnekBanglaFontFamily
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.IslamicTeal
import com.example.ui.theme.SoftMint
import com.example.ui.theme.WarmAmber
import com.example.util.BengaliFormatter

@Composable
fun PrayerCircularRing(
    status: ActiveStatusSnapshot,
    modifier: Modifier = Modifier
) {
    val animatedRemaining by animateFloatAsState(
        targetValue = status.progressFraction.coerceIn(0.01f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "ring_remaining_progress"
    )

    val isForbidden = status.category == PrayerWindowCategory.FORBIDDEN
    val isNafl = status.category == PrayerWindowCategory.NAFL

    val arcColor = when {
        isForbidden -> AlertRed     // #ff5252
        isNafl -> IslamicTeal       // #04484A
        else -> WarmAmber           // #f9a826
    }

    val trackColor = when {
        isForbidden -> Color(0xFFFFD6D6)
        else -> SoftMint            // #CEE6DE provides sharp contrast for #04484A, #f9a826, and #ff5252
    }

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokePx = 12.5.dp.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(diameter, diameter)

            // Outer ring track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Remaining time sweep arc
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedRemaining,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Inner 195dp #04484A circle
        Box(
            modifier = Modifier
                .size(195.dp)
                .background(IslamicTeal, CircleShape)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isForbidden) AlertRed.copy(alpha = 0.22f) else SoftMint.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isForbidden) "নিষিদ্ধ সময়" else "চলমান ওয়াক্ত",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isForbidden) AlertRed else SoftMint,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = status.currentTitleBn,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isForbidden) AlertRed else CleanWhite
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = status.nextLabelBn,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SoftMint
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = BengaliFormatter.formatCountdown(status.countdownSeconds),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = AnekBanglaFontFamily,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isForbidden) AlertRed else WarmAmber
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
