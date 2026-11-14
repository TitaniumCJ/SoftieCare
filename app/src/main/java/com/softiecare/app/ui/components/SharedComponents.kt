package com.softiecare.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.model.SpaceHazard
import com.softiecare.app.core.model.StatusLevel
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusCritical
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.StatusWarning
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextSecondary

fun statusColor(level: StatusLevel): Color = when (level) {
    StatusLevel.NORMAL -> StatusNormal
    StatusLevel.WARNING -> StatusWarning
    StatusLevel.CRITICAL -> StatusCritical
}

@Composable
fun SpaceCard(
    statusLevel: StatusLevel,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfacePanel,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, statusColor(statusLevel))
    ) {
        // clickable dentro del Surface para que el ripple respete las esquinas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(12.dp)
        ) {
            content()
        }
    }
}

@Composable
fun StatusBadge(level: StatusLevel) {
    Surface(
        color = statusColor(level),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = level.name,
            color = SpaceBackground,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun HazardTag(hazard: SpaceHazard) {
    Surface(
        color = SpaceBackground,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, TextSecondary)
    ) {
        Text(
            text = hazard.name,
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PrimaryActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StatusNormal,
            contentColor = SpaceBackground
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}
