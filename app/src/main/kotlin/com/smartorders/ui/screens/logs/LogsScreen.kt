package com.smartorders.ui.screens.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartorders.data.database.entities.TripLog
import com.smartorders.data.database.entities.TripStatus
import com.smartorders.ui.theme.*
import com.smartorders.ui.viewmodel.LogsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogsScreen(
    modifier: Modifier = Modifier,
    viewModel: LogsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("سجل الرحلات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold)
                Text("${uiState.logs.size} رحلة مسجّلة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (uiState.logs.isNotEmpty()) {
                OutlinedButton(
                    onClick = { viewModel.showClearConfirm(true) },
                    shape  = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteSweep, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("مسح")
                }
            }
        }

        HorizontalDivider()

        if (uiState.logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.AssignmentLate, null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Text("لا توجد رحلات مسجّلة بعد",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Text("ستظهر الرحلات المكتشفة هنا تلقائياً\nبعد تفعيل القبول التلقائي",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.logs, key = { it.id }) { log ->
                    TripLogCard(log = log)
                }
            }
        }
    }

    if (uiState.showClearConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearConfirm(false) },
            icon  = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("مسح السجل", fontWeight = FontWeight.Bold) },
            text  = { Text("سيتم حذف جميع سجلات الرحلات. هذه العملية لا يمكن التراجع عنها.") },
            confirmButton = {
                Button(onClick = viewModel::clearLogs,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("مسح الكل")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.showClearConfirm(false) }) { Text("إلغاء") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun TripLogCard(log: TripLog) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    val (statusColor, statusLabel, statusIcon) = when (log.status) {
        TripStatus.ACCEPTED -> Triple(Success,          "مقبولة",   Icons.Default.CheckCircle)
        TripStatus.REJECTED -> Triple(StatusCancelled,  "مرفوضة",   Icons.Default.Cancel)
        TripStatus.DETECTED -> Triple(Primary,          "مكتشفة",   Icons.Default.Radar)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // Top row: status chip + time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status chip
                Surface(
                    shape  = RoundedCornerShape(20.dp),
                    color  = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(14.dp))
                        Text(statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold)
                    }
                }

                // Time
                Column(horizontalAlignment = Alignment.End) {
                    Text(timeFormat.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(dateFormat.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Source app
            if (log.sourceApp.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.PhoneAndroid, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp))
                    Text(log.sourceApp,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider()

            // Amount + distances
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (log.amount > 0) {
                    InfoPill(Icons.Default.AttachMoney,
                        "${String.format("%.2f", log.amount)} ر.س", Success)
                }
                if (log.pickupDistanceKm > 0) {
                    InfoPill(Icons.Default.NearMe,
                        "استلام ${String.format("%.1f", log.pickupDistanceKm)} كم", Primary)
                }
                if (log.tripDistanceKm > 0) {
                    InfoPill(Icons.Default.Route,
                        "رحلة ${String.format("%.1f", log.tripDistanceKm)} كم", Secondary)
                }
            }

            // Detected text
            Text(
                text = log.detectedText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Reject reason
            if (log.status == TripStatus.REJECTED && log.rejectReason.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Info, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp))
                        Text(log.rejectReason,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.1f)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}
