package com.likeki.alquran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likeki.alquran.ui.theme.Gold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            OwnerAvatar()
            Spacer(Modifier.height(16.dp))
            Text(
                "Mohamadu Nurudini Taahir",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                "Owner & Publisher",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            InfoLine("Application", "AL QURAN")
            InfoLine("Version", "1.0.0")
            InfoLine("Package", "com.likeki.alquran")

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            Text(
                "AL QURAN is a simple, respectful audio player for listening to and learning from the Holy Quran. It contains no advertisements, no subscriptions, no in-app purchases, and no tracking.",
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(20.dp))
            Text(
                "Privacy",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "This application does not collect personal information. Downloaded audio is stored locally on your device and never uploaded. There are no analytics SDKs, no ad SDKs, and no user accounts.",
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(40.dp))
            Text(
                "— Mohamadu Nurudini Taahir",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(48.dp))
        }
    }
}


@Composable
private fun OwnerAvatar() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val hasPhoto = androidx.compose.ui.res.painterResource
    // Try to load the bundled owner photo; fall back to "MN" initials if missing
    val painter = try {
        androidx.compose.ui.res.painterResource(id = ctx.resources.getIdentifier("owner_photo", "drawable", ctx.packageName))
    } catch (e: Exception) { null }

    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (painter != null) {
            androidx.compose.foundation.Image(
                painter = painter,
                contentDescription = "Mohamadu Nurudini Taahir",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                "MN",
                fontSize = 52.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gold
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        Text(value, fontSize = 14.sp)
    }
}
