package com.fortyeight.orderformapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val isServerConnected: Boolean = false
)

class AuthViewModel(private val context: Context) : ViewModel() {
    private val authManager = AuthManager(context)
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    

    
    private var apiService: ApiService? = null
    
    init {
        // Check if user is already logged in
        viewModelScope.launch {
            authManager.isLoggedIn.collect { loggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
            }
        }
    }
    
    fun setServerUrl(serverUrl: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl.ensureTrailingSlash())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ApiService::class.java)
        _uiState.value = _uiState.value.copy(isServerConnected = true)
        println("DEBUG: Server URL set, isServerConnected = true")
    }
    
    fun login(username: String, password: String) {
        println("DEBUG: Login called with username: $username")
        if (apiService == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Server URL not set. Please set server URL first."
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                val response = apiService!!.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    println("DEBUG: Login response successful: ${loginResponse?.success}")
                    if (loginResponse?.success == true) {
                        // Save login data - use username as token if no token provided
                        val token = loginResponse.token ?: "session_${System.currentTimeMillis()}"
                        val roles = loginResponse.user?.roles ?: emptyList()
                        authManager.saveLoginData(
                            token,
                            username,
                            roles
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            isServerConnected = true, // Preserve server connection state
                            errorMessage = null
                        )
                        println("DEBUG: Login successful, isLoggedIn set to true")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = loginResponse?.message ?: "Login failed"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Login failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Network error: ${e.message}"
                )
            }
        }
    }
    
    fun register(username: String, password: String, email: String?) {
        if (apiService == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Server URL not set. Please set server URL first."
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                val response = apiService!!.register(RegisterRequest(username, password, email))
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse?.success == true) {
                        // Save login data - use username as token if no token provided
                        val token = registerResponse.token ?: "session_${System.currentTimeMillis()}"
                        val roles = registerResponse.user?.roles ?: emptyList()
                        authManager.saveLoginData(
                            token,
                            username,
                            roles
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            isServerConnected = true, // Preserve server connection state
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = registerResponse?.message ?: "Registration failed"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Registration failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Network error: ${e.message}"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                // Try to logout from server if we have a token
                val token = authManager.getCurrentToken()
                if (token != null && apiService != null) {
                    apiService!!.logout("Bearer $token")
                }
            } catch (e: Exception) {
                // Ignore logout errors, still clear local data
            } finally {
                // Clear local login data
                authManager.clearLoginData()
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = false,
                    isServerConnected = false,
                    errorMessage = null
                )
                println("DEBUG: Logout completed, reset all states")
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun String.ensureTrailingSlash(): String {
        return if (this.endsWith("/")) this else "$this/"
    }
}
