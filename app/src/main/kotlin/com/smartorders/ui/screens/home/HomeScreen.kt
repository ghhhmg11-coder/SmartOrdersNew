package com.smartorders.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartorders.data.database.entities.Order
import com.smartorders.data.database.entities.OrderStatus
import com.smartorders.ui.theme.*
import com.smartorders.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Orders",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "إدارة الطلبات",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "الإحصائيات",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddOrderDialog(true) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("طلب جديد") },
                containerColor = Primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Stats Row
            uiState.todayStats?.let { stats ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "طلبات اليوم",
                        value = stats.totalOrders.toString(),
                        icon = Icons.Default.ShoppingCart,
                        backgroundColor = PrimaryContainer,
                        iconColor = Primary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "الإيرادات",
                        value = "${String.format("%.0f", stats.totalRevenue)} ر.س",
                        icon = Icons.Default.AttachMoney,
                        backgroundColor = SuccessContainer,
                        iconColor = Success
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "نشطة",
                        value = uiState.activeOrderCount.toString(),
                        icon = Icons.Default.Pending,
                        backgroundColor = WarningContainer,
                        iconColor = Warning
                    )
                }
            }

            // Orders list
            if (uiState.orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "لا توجد طلبات اليوم",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "اضغط على زر + لإضافة طلب جديد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 100.dp, top = 4.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onClick = { viewModel.showOrderDetails(order) },
                            onStatusChange = { newStatus ->
                                viewModel.updateOrderStatus(order.id, newStatus)
                            },
                            onDelete = { viewModel.deleteOrder(order) }
                        )
                    }
                }
            }
        }
    }

    // Add Order Dialog
    if (uiState.showAddOrderDialog) {
        AddOrderDialog(
            onDismiss = { viewModel.showAddOrderDialog(false) },
            onConfirm = { name, items, amount, address, notes ->
                viewModel.addOrder(name, items, amount, address, notes)
            }
        )
    }

    // Order Details Dialog
    uiState.showOrderDetails?.let { order ->
        OrderDetailsDialog(
            order = order,
            onDismiss = { viewModel.showOrderDetails(null) },
            onStatusChange = { newStatus ->
                viewModel.updateOrderStatus(order.id, newStatus)
                viewModel.showOrderDetails(null)
            }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = iconColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = orderStatusColor(order.status).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = orderStatusIcon(order.status),
                    contentDescription = null,
                    tint = orderStatusColor(order.status),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeFormat.format(Date(order.createdAt)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = order.customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(status = order.status)
                    Text(
                        text = "${String.format("%.2f", order.totalAmount)} ر.س",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    OrderStatus.values().filter { it != order.status }.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(orderStatusLabel(status)) },
                            onClick = {
                                onStatusChange(status)
                                showMenu = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("حذف", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: OrderStatus) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = orderStatusColor(status).copy(alpha = 0.15f)
    ) {
        Text(
            text = orderStatusLabel(status),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = orderStatusColor(status),
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun orderStatusColor(status: OrderStatus): Color = when (status) {
    OrderStatus.PENDING -> StatusPending
    OrderStatus.ACCEPTED -> StatusAccepted
    OrderStatus.PREPARING -> StatusPreparing
    OrderStatus.DELIVERED -> StatusDelivered
    OrderStatus.CANCELLED -> StatusCancelled
}

private fun orderStatusIcon(status: OrderStatus) = when (status) {
    OrderStatus.PENDING -> Icons.Default.HourglassEmpty
    OrderStatus.ACCEPTED -> Icons.Default.CheckCircle
    OrderStatus.PREPARING -> Icons.Default.Restaurant
    OrderStatus.DELIVERED -> Icons.Default.LocalShipping
    OrderStatus.CANCELLED -> Icons.Default.Cancel
}

fun orderStatusLabel(status: OrderStatus): String = when (status) {
    OrderStatus.PENDING -> "قيد الانتظار"
    OrderStatus.ACCEPTED -> "مقبول"
    OrderStatus.PREPARING -> "جاري التحضير"
    OrderStatus.DELIVERED -> "تم التوصيل"
    OrderStatus.CANCELLED -> "ملغي"
}

@Composable
private fun AddOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var items by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة طلب جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("اسم العميل *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = items,
                    onValueChange = { items = it },
                    label = { Text("الأصناف *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("المبلغ (ر.س) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("العنوان") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    if (customerName.isNotBlank() && items.isNotBlank()) {
                        onConfirm(customerName, items, amountDouble, address, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("إضافة") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun OrderDetailsDialog(
    order: Order,
    onDismiss: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit
) {
    val timeFormat = SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("تفاصيل الطلب", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                StatusChip(order.status)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(label = "رقم الطلب", value = order.orderNumber)
                DetailRow(label = "العميل", value = order.customerName)
                if (order.customerPhone.isNotBlank())
                    DetailRow(label = "الهاتف", value = order.customerPhone)
                if (order.address.isNotBlank())
                    DetailRow(label = "العنوان", value = order.address)
                DetailRow(label = "الأصناف", value = order.items)
                DetailRow(label = "المبلغ", value = "${String.format("%.2f", order.totalAmount)} ر.س")
                DetailRow(label = "المصدر", value = order.source)
                DetailRow(label = "الوقت", value = timeFormat.format(Date(order.createdAt)))
                if (order.notes.isNotBlank())
                    DetailRow(label = "ملاحظات", value = order.notes)

                if (order.status == OrderStatus.PENDING || order.status == OrderStatus.ACCEPTED) {
                    HorizontalDivider()
                    Text(
                        text = "تغيير الحالة",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (order.status == OrderStatus.PENDING) {
                            Button(
                                onClick = { onStatusChange(OrderStatus.ACCEPTED) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusAccepted),
                                modifier = Modifier.weight(1f)
                            ) { Text("قبول", style = MaterialTheme.typography.labelLarge) }
                        }
                        if (order.status == OrderStatus.ACCEPTED) {
                            Button(
                                onClick = { onStatusChange(OrderStatus.PREPARING) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPreparing),
                                modifier = Modifier.weight(1f)
                            ) { Text("تحضير", style = MaterialTheme.typography.labelLarge) }
                        }
                        OutlinedButton(
                            onClick = { onStatusChange(OrderStatus.CANCELLED) },
                            modifier = Modifier.weight(1f)
                        ) { Text("إلغاء", color = StatusCancelled, style = MaterialTheme.typography.labelLarge) }
                    }
                }
                if (order.status == OrderStatus.PREPARING) {
                    HorizontalDivider()
                    Button(
                        onClick = { onStatusChange(OrderStatus.DELIVERED) },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("تم التوصيل") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
