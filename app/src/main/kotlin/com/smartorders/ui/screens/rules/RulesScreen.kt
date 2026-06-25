package com.smartorders.ui.screens.rules

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartorders.data.preferences.TargetApp
import com.smartorders.ui.theme.Primary
import com.smartorders.ui.theme.Success
import com.smartorders.ui.theme.SuccessContainer
import com.smartorders.ui.viewmodel.RulesViewModel

@Composable
fun RulesScreen(
    modifier: Modifier = Modifier,
    viewModel: RulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            snackbarHostState.showSnackbar("✓ تم حفظ القواعد")
            viewModel.clearSavedSuccess()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Price Rules ───────────────────────────────────────────────────
            RulesSection(icon = Icons.Default.AttachMoney, title = "أسعار الرحلة") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    PriceField(
                        modifier    = Modifier.weight(1f),
                        label       = "الحد الأدنى",
                        value       = uiState.minTripPrice,
                        suffix      = "ر.س",
                        onValueChange = viewModel::onMinPriceChange
                    )
                    PriceField(
                        modifier    = Modifier.weight(1f),
                        label       = "الحد الأقصى",
                        value       = uiState.maxTripPrice,
                        suffix      = "ر.س",
                        onValueChange = viewModel::onMaxPriceChange
                    )
                }
            }

            // ── Distance Rules ────────────────────────────────────────────────
            RulesSection(icon = Icons.Default.SocialDistance, title = "مسافات الرحلة") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    PriceField(
                        modifier    = Modifier.weight(1f),
                        label       = "أقصى مسافة استلام",
                        value       = uiState.maxPickupDistance,
                        suffix      = "كم",
                        onValueChange = viewModel::onMaxPickupChange
                    )
                    PriceField(
                        modifier    = Modifier.weight(1f),
                        label       = "أقصى مسافة رحلة",
                        value       = uiState.maxTripDistance,
                        suffix      = "كم",
                        onValueChange = viewModel::onMaxTripChange
                    )
                }
            }

            // ── Target App ────────────────────────────────────────────────────
            RulesSection(icon = Icons.Default.Apps, title = "التطبيق المستهدف") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TargetApp.values().forEach { app ->
                        val selected = uiState.targetApp == app
                        FilterChip(
                            selected  = selected,
                            onClick   = { viewModel.onTargetAppChange(app) },
                            label     = { Text(app.displayName, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (selected) ({
                                Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                            }) else null,
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(10.dp),
                            colors    = FilterChipDefaults.filterChipColors(
                                selectedContainerColor     = Primary.copy(alpha = 0.15f),
                                selectedLabelColor         = Primary,
                                selectedLeadingIconColor   = Primary
                            )
                        )
                    }
                }
            }

            // ── Alerts ────────────────────────────────────────────────────────
            RulesSection(icon = Icons.Default.NotificationsActive, title = "تنبيهات القبول") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ToggleRow(
                        icon    = Icons.Default.VolumeUp,
                        label   = "الصوت",
                        checked = uiState.soundEnabled,
                        onChange = viewModel::onSoundChange
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    ToggleRow(
                        icon    = Icons.Default.Vibration,
                        label   = "الاهتزاز",
                        checked = uiState.vibrationEnabled,
                        onChange = viewModel::onVibrationChange
                    )
                }
            }

            // ── Accessibility Button ──────────────────────────────────────────
            RulesSection(icon = Icons.Default.Accessibility, title = "خدمة إمكانية الوصول") {
                Button(
                    onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.OpenInNew, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("فتح إعدادات إمكانية الوصول")
                }
            }

            // ── Save Button ───────────────────────────────────────────────────
            Button(
                onClick  = viewModel::saveRules,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Success)
            ) {
                Icon(Icons.Default.Save, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("حفظ القواعد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) { data ->
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessContainer)
            ) {
                Text(data.visuals.message, modifier = Modifier.padding(16.dp),
                    color = Success, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RulesSection(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
            }
            content()
        }
    }
}

@Composable
private fun PriceField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    suffix: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = value,
        onValueChange = { if (it.length <= 6) onValueChange(it) },
        label         = { Text(label, style = MaterialTheme.typography.labelMedium) },
        suffix        = { Text(suffix, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant) },
        singleLine    = true,
        modifier      = modifier,
        shape         = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            focusedLabelColor  = Primary
        )
    )
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary
            )
        )
    }
}
