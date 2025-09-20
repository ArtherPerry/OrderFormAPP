package com.fortyeight.orderformapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fortyeight.orderformapp.ui.theme.OrderFormAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrderFormAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OrderFormApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFormApp(viewModel: OrderViewModel = viewModel()) {
    var serverUrl by remember { mutableStateOf("") }
    var isServerSet by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val townships by viewModel.townships.collectAsState()
    val merchants by viewModel.merchants.collectAsState()
    val context = LocalContext.current

    if (!isServerSet) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Order Form App",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Enter Ngrok URL") },
                placeholder = { Text("https://your-ngrok-url.ngrok.io") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (serverUrl.isNotBlank()) {
                        viewModel.setServerUrl(serverUrl)
                        isServerSet = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect to Server")
            }
        }
    } else {
        OrderForm(
            viewModel = viewModel,
            uiState = uiState,
            townships = townships,
            merchants = merchants,
            onRawBtPrint = { commands ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("rawbt:${Uri.encode(commands)}")
                    setPackage("ru.a402d.rawbtprinter")
                }

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to Play Store
                    val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=ru.a402d.rawbtprinter")
                    }
                    context.startActivity(playStoreIntent)
                }
            }
        )
    }

    if (uiState.showPreview) {
        PreviewDialog(
            uiState = uiState,
            townships = townships,
            onDismiss = { viewModel.hidePreview() },
            onPrint = { commands ->
                viewModel.saveOrder(
                    onSuccess = {
                        Toast.makeText(context, "Order saved successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("rawbt:${Uri.encode(commands)}")
                            setPackage("ru.a402d.rawbtprinter")
                        }

                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://play.google.com/store/apps/details?id=ru.a402d.rawbtprinter")
                            }
                            context.startActivity(playStoreIntent)
                        }
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}