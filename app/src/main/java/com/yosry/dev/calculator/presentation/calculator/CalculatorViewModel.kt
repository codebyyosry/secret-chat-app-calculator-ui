package com.yosry.dev.calculator.presentation.calculator

// CalculatorViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosry.dev.calculator.domain.CalculateUseCase
import com.yosry.dev.calculator.domain.CalculatorAction
import com.yosry.dev.calculator.domain.CalculatorOperation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val calculateUseCase: CalculateUseCase = CalculateUseCase()
) : ViewModel(), CalculatorContract.Presenter {

    private val _state = MutableStateFlow(CalculatorContract.State())
    override val state: StateFlow<CalculatorContract.State> = _state.asStateFlow()
    // One-time events channel
    private val _uiEvent = MutableSharedFlow<CalculatorUiEvent>()
    override val uiEvent = _uiEvent.asSharedFlow()

    override fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> _state.update { CalculatorContract.State() }
            is CalculatorAction.Delete -> performDeletion()
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Calculate -> performCalculation()
        }
    }

    private fun enterOperation(operation: CalculatorOperation) {
        if (_state.value.number1.isNotBlank()) {
            _state.update { it.copy(operation = operation) }
        }
    }

    private fun performCalculation() {
        val currentState = _state.value
        // 1. Check for the secret code first
        // Check for either User A or User B's secret code
        if (currentState.number1 == "280626" || currentState.number1 == "200460") {
            viewModelScope.launch {
                // Pass the exact code that was entered
                _uiEvent.emit(CalculatorUiEvent.NavigateToChat(currentState.number1))

                // Clear the calculator screen so the code isn't visible when they press back
                _state.update { CalculatorContract.State() }
            }
            return
        }
        val operation = currentState.operation ?: return



        if (currentState.number1.isNotBlank() && currentState.number2.isNotBlank()) {
            val result = calculateUseCase(
                num1 = currentState.number1,
                num2 = currentState.number2,
                operation = operation
            )

            // Format to remove trailing .0 for clean display
            val formattedResult = if (result % 1 == 0.0) result.toInt().toString() else result.toString()

            _state.update {
                it.copy(
                    number1 = formattedResult,
                    number2 = "",
                    operation = null
                )
            }
        }
    }

    private fun performDeletion() {
        _state.update { state ->
            when {
                state.number2.isNotBlank() -> state.copy(number2 = state.number2.dropLast(1))
                state.operation != null -> state.copy(operation = null)
                state.number1.isNotBlank() -> state.copy(number1 = state.number1.dropLast(1))
                else -> state
            }
        }
    }

    private fun enterDecimal() {
        _state.update { state ->
            when {
                state.operation == null && !state.number1.contains(".") && state.number1.isNotBlank() -> {
                    state.copy(number1 = state.number1 + ".")
                }
                state.operation != null && !state.number2.contains(".") && state.number2.isNotBlank() -> {
                    state.copy(number2 = state.number2 + ".")
                }
                else -> state
            }
        }
    }

    private fun enterNumber(number: Int) {
        _state.update { state ->
            if (state.operation == null) {
                if (state.number1.length >= 15) return@update state
                state.copy(number1 = state.number1 + number)
            } else {
                if (state.number2.length >= 15) return@update state
                state.copy(number2 = state.number2 + number)
            }
        }
    }
}