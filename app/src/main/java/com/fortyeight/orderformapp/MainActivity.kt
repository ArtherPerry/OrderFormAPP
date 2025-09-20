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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
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

@Composable
fun OrderFormApp(
    authViewModel: AuthViewModel? = null,
    orderViewModel: OrderViewModel = viewModel()
) {
    val context = LocalContext.current
    val authViewModelInstance = authViewModel ?: viewModel { AuthViewModel(context) }
    var serverUrl by remember { mutableStateOf("") }
    val authUiState by authViewModelInstance.uiState.collectAsState()

    val isServerConnected = authUiState.isServerConnected
    val orderUiState by orderViewModel.uiState.collectAsState()
    val townships by orderViewModel.townships.collectAsState()
    val merchants by orderViewModel.merchants.collectAsState()



    // Show server URL setup first
    if (!isServerConnected) {
        ServerUrlSetupScreen(
            serverUrl = serverUrl,
            onServerUrlChange = { serverUrl = it },
            onConnect = {
                if (serverUrl.isNotBlank()) {
                    authViewModelInstance.setServerUrl(serverUrl)
                    orderViewModel.setServerUrl(serverUrl)
                }
            }
        )
    }
    // Show login screen after server is connected
    else if (!authUiState.isLoggedIn) {
        LoginScreen(
            onLogin = { username, password ->
                authViewModelInstance.login(username, password)
            },
            onRegister = { username, password, email ->
                authViewModelInstance.register(username, password, email)
            },
            isLoading = authUiState.isLoading,
            errorMessage = authUiState.errorMessage,
            onBackToServerSetup = {
                authViewModelInstance.logout()
                serverUrl = ""
            }
        )
    }
    // Show main app after login
    else {
        MainAppScreen(
            orderViewModel = orderViewModel,
            uiState = orderUiState,
            townships = townships,
            merchants = merchants,
            onLogout = {
                authViewModelInstance.logout()
                serverUrl = ""
            }
        )
    }
}

@Composable
fun ServerUrlSetupScreen(
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    onConnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Server Setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter your server URL to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = onServerUrlChange,
                    label = { Text("Server URL") },
                    placeholder = { Text("https://your-ngrok-url.ngrok.io") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = onConnect,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = serverUrl.isNotBlank()
                ) {
                    Text("Connect to Server")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    orderViewModel: OrderViewModel,
    uiState: OrderUiState,
    townships: List<Township>,
    merchants: List<Merchant>,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Order Form App") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OrderForm(
                viewModel = orderViewModel,
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
    }

    if (uiState.showPreview) {
        PreviewDialog(
            uiState = uiState,
            townships = townships,
            onDismiss = { orderViewModel.hidePreview() },
            onPrint = { commands ->
                orderViewModel.saveOrder(
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