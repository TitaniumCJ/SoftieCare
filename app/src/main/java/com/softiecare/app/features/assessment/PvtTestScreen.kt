package com.softiecare.app.features.assessment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.softiecare.app.ui.components.PrimaryActionButton
import com.softiecare.app.ui.theme.TextPrimary

// Placeholder del Modulo 3: lo reemplaza el Integrante 3
@Composable
fun PvtTestScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Test PVT (placeholder)", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        PrimaryActionButton("Volver", onBack)
    }
}
