package com.fortyeight.orderformapp


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderForm(
    viewModel: OrderViewModel,
    uiState: OrderUiState,
    townships: List<Township>,
    merchants: List<Merchant>,
    onRawBtPrint: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "New Order",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Date Fields
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.pickupDate,
                onValueChange = { viewModel.updateField("pickupDate", it) },
                label = { Text("Pickup Date") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            OutlinedTextField(
                value = uiState.deliverDate,
                onValueChange = { viewModel.updateField("deliverDate", it) },
                label = { Text("Delivery Date") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Customer Info
        OutlinedTextField(
            value = uiState.customerName,
            onValueChange = { viewModel.updateField("customerName", it) },
            label = { Text("Customer Name *") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.customerPhone,
            onValueChange = { viewModel.updateField("customerPhone", it) },
            label = { Text("Customer Phone *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Address
        OutlinedTextField(
            value = uiState.deliveryAddress,
            onValueChange = { viewModel.updateField("deliveryAddress", it) },
            label = { Text("Delivery Address *") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Township Dropdown
        var expandedTownship by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedTownship,
            onExpandedChange = { expandedTownship = !expandedTownship }
        ) {
            OutlinedTextField(
                value = townships.find { it.townshipID == uiState.selectedTownship }?.townshipName ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Township *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTownship) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedTownship,
                onDismissRequest = { expandedTownship = false }
            ) {
                townships.forEach { township ->
                    DropdownMenuItem(
                        text = { Text("${township.townshipName} (${township.deliveryCharge})") },
                        onClick = {
                            viewModel.updateTownship(township.townshipID)
                            expandedTownship = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Merchant Dropdown
        var expandedMerchant by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedMerchant,
            onExpandedChange = { expandedMerchant = !expandedMerchant }
        ) {
            OutlinedTextField(
                value = merchants.find { it.id.toInt() == uiState.selectedMerchant }?.name ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("OS Name *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMerchant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedMerchant,
                onDismissRequest = { expandedMerchant = false }
            ) {
                merchants.forEach { merchant ->
                    DropdownMenuItem(
                        text = { Text(merchant.name) },
                        onClick = {
                            viewModel.updateMerchant(merchant.id)
                            expandedMerchant = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amount Fields
        OutlinedTextField(
            value = uiState.productAmount,
            onValueChange = { viewModel.updateField("productAmount", it) },
            label = { Text("Product Amount *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.extraCharge,
                onValueChange = { viewModel.updateField("extraCharge", it) },
                label = { Text("Extra Charge") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            OutlinedTextField(
                value = uiState.osPaidAmount,
                onValueChange = { viewModel.updateField("osPaidAmount", it) },
                label = { Text("OS Paid Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Charges
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Delivery Charge: ${String.format("%.2f", uiState.deliveryCharge)}")
                Text(
                    "Total: ${String.format("%.2f", uiState.total)}",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parcel Size
        var expandedSize by remember { mutableStateOf(false) }
        val sizes = listOf("SMALL", "MEDIUM", "LARGE")
        ExposedDropdownMenuBox(
            expanded = expandedSize,
            onExpandedChange = { expandedSize = !expandedSize }
        ) {
            OutlinedTextField(
                value = uiState.parcelSize,
                onValueChange = { },
                readOnly = true,
                label = { Text("Parcel Size") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSize) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedSize,
                onDismissRequest = { expandedSize = false }
            ) {
                sizes.forEach { size ->
                    DropdownMenuItem(
                        text = { Text(size) },
                        onClick = {
                            viewModel.updateParcelSize(size)
                            expandedSize = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delivery Notes
        OutlinedTextField(
            value = uiState.deliveryNotes,
            onValueChange = { viewModel.updateField("deliveryNotes", it) },
            label = { Text("Delivery Notes") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Generate Preview Button
        Button(
            onClick = { viewModel.generatePreview() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Generate Preview")
            }
        }
    }
}

@Composable
fun PreviewDialog(
    uiState: OrderUiState,
    townships: List<Township>,
    onDismiss: () -> Unit,
    onPrint: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Label Preview",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Barcode: ${uiState.barcode}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Customer Name: ${uiState.customerName}")
                    Text("Customer Phone: ${uiState.customerPhone}")
                    Text("Pickup Date: ${uiState.pickupDate}")
                    Text("Township: ${townships.find { it.townshipID == uiState.selectedTownship }?.townshipName ?: ""}")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Product Amount: ${uiState.productAmount}")
                    Text("Total: ${String.format("%.2f", uiState.total)}", fontWeight = FontWeight.Bold)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val commands = createPrintCommands(uiState, townships)
                            onPrint(commands)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Print")
                    }
                }
            }
        }
    }
}

private fun createPrintCommands(uiState: OrderUiState, townships: List<Township>): String {
    val township = townships.find { it.townshipID == uiState.selectedTownship }

    return """
        |
        |Barcode:
        |${uiState.barcode}
        |
        |Customer Name:   ${uiState.customerName}
        |Customer Ph:     ${uiState.customerPhone}
        |Pickup Date:     ${uiState.pickupDate}
        |Township:        ${township?.townshipName ?: ""}
        |==================================
        |Product Amount:  ${uiState.productAmount}
        |Total:           ${String.format("%.2f", uiState.total)}
        |==================================
        |
    """.trimMargin()
}