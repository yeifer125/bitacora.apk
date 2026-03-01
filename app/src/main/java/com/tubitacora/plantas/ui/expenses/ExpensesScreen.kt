package com.tubitacora.plantas.ui.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tubitacora.plantas.data.local.entity.PlantExpenseEntity
import com.tubitacora.plantas.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    plantId: Long,
    plantName: String,
    onBack: () -> Unit,
    viewModel: ExpenseViewModel = viewModel()
) {
    val expenses by viewModel.getExpensesForPlant(plantId).collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<PlantExpenseEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finanzas: $plantName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                selectedExpense = null
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Registro")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            val totalGains = expenses.filter { !it.isExpense }.sumOf { it.amount.toDouble() }
            val totalExpenses = expenses.filter { it.isExpense }.sumOf { it.amount.toDouble() }
            val netBalance = totalGains - totalExpenses

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Balance Neto", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "₡${String.format("%.2f", netBalance)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (netBalance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ventas: ₡${String.format("%.2f", totalGains)}", style = MaterialTheme.typography.bodySmall)
                        Text("Gastos: ₡${String.format("%.2f", totalExpenses)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            LazyColumn {
                items(expenses) { expense ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dateFormat.format(Date(expense.date)),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = (if (expense.isExpense) "- " else "+ ") + "₡${String.format("%.2f", expense.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (expense.isExpense) Color(0xFFF44336) else Color(0xFF4CAF50)
                                    )
                                    expense.note?.let {
                                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                Row {
                                    IconButton(onClick = {
                                        selectedExpense = expense
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteTransaction(expense) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ExpenseTransactionDialog(
            expenseToEdit = selectedExpense,
            onDismiss = { showDialog = false },
            onConfirm = { amount, note, isExpense ->
                if (selectedExpense == null) {
                    viewModel.addTransaction(plantId, amount, note, isExpense)
                } else {
                    viewModel.updateTransaction(selectedExpense!!.copy(amount = amount, note = note, isExpense = isExpense))
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun ExpenseTransactionDialog(
    expenseToEdit: PlantExpenseEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (Float, String, Boolean) -> Unit
) {
    var amount by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(expenseToEdit?.note ?: "") }
    var isExpense by remember { mutableStateOf(expenseToEdit?.isExpense ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (expenseToEdit == null) "Nuevo Registro" else "Editar Registro") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = isExpense,
                        onClick = { isExpense = true },
                        label = { Text("Gasto") }
                    )
                    FilterChip(
                        selected = !isExpense,
                        onClick = { isExpense = false },
                        label = { Text("Ganancia") }
                    )
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monto") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Descripción (ej. Venta de cosecha, Fertilizante)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amount.toFloatOrNull() ?: 0f
                    if (amt > 0) {
                        onConfirm(amt, note, isExpense)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
