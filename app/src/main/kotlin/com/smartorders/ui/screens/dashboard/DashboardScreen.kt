package com.smartorders.ui.screens.dashboard

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartorders.ui.screens.logs.LogsScreen
import com.smartorders.ui.screens.rules.RulesScreen
import com.smartorders.ui.theme.*
import com.smartorders.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.checkAccessibilityStatus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SO", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                        Text("Smart Orders", fontWeight = FontWeight.ExtraBold,
                            color = Color.White, style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "خروج", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("لوحة التحكم") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Tune, null) },
                    label = { Text("القواعد") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("السجل") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> DashboardTab(
                modifier = Modifier.padding(padding),
                autoAcceptEnabled      = uiState.autoAcceptEnabled,
                isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                totalDetected          = uiState.totalDetected,
                totalAccepted          = uiState.totalAccepted,
                totalRejected          = uiState.totalRejected,
                totalEarnings          = uiState.totalEarnings,
                onToggleAutoAccept     = viewModel::toggleAutoAccept,
                onOpenAccessibility    = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                onRefreshStatus        = viewModel::checkAccessibilityStatus
            )
            1 -> RulesScreen(modifier = Modifier.padding(padding))
            2 -> LogsScreen(modifier = Modifier.padding(padding))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("تسجيل الخروج", fontWeight = FontWeight.Bold) },
            text  = { Text("هل أنت متأكد؟") },
            confirmButton = {
                Button(onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("نعم")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("لا") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun DashboardTab(
    modifier: Modifier = Modifier,
    autoAcceptEnabled: Boolean,
    isAccessibilityEnabled: Boolean,
    totalDetected: Int,
    totalAccepted: Int,
    totalRejected: Int,
    totalEarnings: Double,
    onToggleAutoAccept: (Boolean) -> Unit,
    onOpenAccessibility: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Accessibility Service Status ──────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAccessibilityEnabled) SuccessContainer else ErrorContainer
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isAccessibilityEnabled) Success else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "خدمة إمكانية الوصول",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isAccessibilityEnabled) "مفعّلة ✓" else "غير مفعّلة — اضغط للتفعيل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAccessibilityEnabled) Success else MaterialTheme.colorScheme.error
                    )
                }
                if (!isAccessibilityEnabled) {
                    FilledTonalButton(
                        onClick = onOpenAccessibility,
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("تفعيل", style = MaterialTheme.typography.labelLarge) }
                } else {
                    IconButton(onClick = onRefreshStatus) {
                        Icon(Icons.Default.Refresh, null, tint = Success)
                    }
                }
            }
        }

        // ── Auto-Accept Toggle ────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (autoAcceptEnabled) Primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (autoAcceptEnabled) 6.dp else 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = if (autoAcceptEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "القبول التلقائي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (autoAcceptEnabled) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (autoAcceptEnabled) "يعمل الآن — يقبل الرحلات تلقائياً"
                               else "متوقف — اضغط للتفعيل",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (autoAcceptEnabled) Color.White.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoAcceptEnabled,
                    onCheckedChange = onToggleAutoAccept,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor  = Color.White,
                        checkedTrackColor  = Color.White.copy(alpha = 0.4f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // ── Stats Row ─────────────────────────────────────────────────────────
        Text("إحصائيات الرحلات",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(Modifier.weight(1f), "مكتشفة", totalDetected.toString(),
                Icons.Default.Radar, Primary, PrimaryContainer)
            StatCard(Modifier.weight(1f), "مقبولة",  totalAccepted.toString(),
                Icons.Default.CheckCircle, Success, SuccessContainer)
            StatCard(Modifier.weight(1f), "مرفوضة",  totalRejected.toString(),
                Icons.Default.Cancel, StatusCancelled, ErrorContainer)
        }

        // ── Total Earnings ────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Success.copy(alpha = 0.12f), SuccessContainer)),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        Modifier.size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SuccessContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AttachMoney, null, tint = Success, modifier = Modifier.size(30.dp))
                    }
                    Column {
                        Text("إجمالي الأرباح المقبولة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${String.format("%.2f", totalEarnings)} ر.س",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Success
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    bg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Text(value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color)
            Text(label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}
