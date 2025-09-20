package com.fortyeight.orderformapp


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val _townships = MutableStateFlow<List<Township>>(emptyList())
    val townships: StateFlow<List<Township>> = _townships.asStateFlow()

    private val _merchants = MutableStateFlow<List<Merchant>>(emptyList())
    val merchants: StateFlow<List<Merchant>> = _merchants.asStateFlow()

    fun setServerUrl(url: String) {
        repository.setBaseUrl(url)
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val townshipsResult = repository.getTownships()
            val merchantsResult = repository.getMerchants()

            townshipsResult.onSuccess { _townships.value = it }
            merchantsResult.onSuccess { _merchants.value = it }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                pickupDate = LocalDate.now().toString(),
                deliverDate = LocalDate.now().plusDays(1).toString()
            )
        }
    }

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "pickupDate" -> {
                val deliverDate = try {
                    LocalDate.parse(value).plusDays(1).toString()
                } catch (e: Exception) {
                    _uiState.value.deliverDate
                }
                _uiState.value.copy(pickupDate = value, deliverDate = deliverDate)
            }
            "deliverDate" -> _uiState.value.copy(deliverDate = value)
            "customerName" -> _uiState.value.copy(customerName = value)
            "customerPhone" -> _uiState.value.copy(customerPhone = value)
            "productAmount" -> {
                val newState = _uiState.value.copy(productAmount = value)
                newState.copy(total = calculateTotal(newState))
            }
            "extraCharge" -> {
                val newState = _uiState.value.copy(extraCharge = value)
                newState.copy(total = calculateTotal(newState))
            }
            "osPaidAmount" -> {
                val newState = _uiState.value.copy(osPaidAmount = value)
                newState.copy(total = calculateTotal(newState))
            }
            "deliveryAddress" -> _uiState.value.copy(deliveryAddress = value)
            "deliveryNotes" -> _uiState.value.copy(deliveryNotes = value)
            else -> _uiState.value
        }
    }

    fun updateTownship(townshipId: Int) {
        val township = _townships.value.find { it.townshipID == townshipId }
        val newState = _uiState.value.copy(
            selectedTownship = townshipId,
            deliveryCharge = township?.deliveryCharge ?: 0.0
        )
        _uiState.value = newState.copy(total = calculateTotal(newState))
    }

    fun updateMerchant(merchantId: Int) {
        _uiState.value = _uiState.value.copy(selectedMerchant = merchantId)
    }

    fun updateParcelSize(size: String) {
        _uiState.value = _uiState.value.copy(parcelSize = size)
    }

    private fun calculateTotal(state: OrderUiState): Double {
        val productAmount = state.productAmount.toDoubleOrNull() ?: 0.0
        val extraCharge = state.extraCharge.toDoubleOrNull() ?: 0.0
        val osPaidAmount = state.osPaidAmount.toDoubleOrNull() ?: 0.0
        return productAmount + extraCharge + state.deliveryCharge - osPaidAmount
    }

    fun generatePreview() {
        val barcode = "PARCEL-${System.currentTimeMillis()}"
        _uiState.value = _uiState.value.copy(
            barcode = barcode,
            showPreview = true
        )
    }

    fun hidePreview() {
        _uiState.value = _uiState.value.copy(showPreview = false)
    }

    fun saveOrder(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value
        if (!isValidOrder(state)) {
            onError("Please fill all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val parcel = Parcel(
                pickupDate = state.pickupDate,
                deliverDate = state.deliverDate,
                customerName = state.customerName,
                customerPhNumber = state.customerPhone,
                productAmount = state.productAmount,
                extraCharge = state.extraCharge.toDoubleOrNull() ?: 0.0,
                osPaidAmount = state.osPaidAmount.toDoubleOrNull() ?: 0.0,
                deliveryAddress = state.deliveryAddress,
                barcode = state.barcode,
                township = state.selectedTownship,
                merchant = state.selectedMerchant,
                total = state.total,
                parcelSize = state.parcelSize,
                deliveryNotes = state.deliveryNotes
            )

            val result = repository.saveOrder(parcel)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess {
                if (it.success) onSuccess() else onError(it.message ?: "Failed to save order")
            }.onFailure {
                onError(it.message ?: "Network error")
            }
        }
    }

    private fun isValidOrder(state: OrderUiState): Boolean {
        return state.customerName.isNotBlank() &&
                state.customerPhone.isNotBlank() &&
                state.productAmount.isNotBlank() &&
                state.deliveryAddress.isNotBlank() &&
                state.selectedTownship > 0 &&
                state.selectedMerchant > 0
    }

    fun createRawBtIntent(): String {
        val state = _uiState.value
        val township = _townships.value.find { it.townshipID == state.selectedTownship }

        val escPosCommands = """
            |
            |Barcode:
            |${state.barcode}
            |
            |Customer Name:   ${state.customerName}
            |Customer Ph:     ${state.customerPhone}
            |Pickup Date:     ${state.pickupDate}
            |Township:        ${township?.townshipName ?: ""}
            |==================================
            |Product Amount:  ${state.productAmount}
            |Total:           ${String.format("%.2f", state.total)}
            |==================================
            |
        """.trimMargin()

        return escPosCommands
    }
}

data class OrderUiState(
    val isLoading: Boolean = false,
    val pickupDate: String = "",
    val deliverDate: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val productAmount: String = "",
    val extraCharge: String = "",
    val osPaidAmount: String = "",
    val deliveryAddress: String = "",
    val deliveryNotes: String = "",
    val selectedTownship: Int = 0,
    val selectedMerchant: Int = 0,
    val deliveryCharge: Double = 0.0,
    val total: Double = 0.0,
    val parcelSize: String = "SMALL",
    val barcode: String = "",
    val showPreview: Boolean = false
)