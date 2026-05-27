package com.yosry.dev.calculator.presentation.calculator

// CalculatorContract.kt
import com.yosry.dev.calculator.domain.CalculatorAction
import com.yosry.dev.calculator.domain.CalculatorOperation
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


sealed class CalculatorUiEvent {
    data class NavigateToChat(val userCode: String) : CalculatorUiEvent() // Added parameter
    object NavigateToFileViewer : CalculatorUiEvent() // Added parameter
    data class LoginAsClient(val userCode: String) : CalculatorUiEvent()
}


interface CalculatorContract {
    data class State(
        val number1: String = "",
        val number2: String = "",
        val operation: CalculatorOperation? = null
    )

    interface Presenter {
        val state: StateFlow<State>
        val uiEvent: SharedFlow<CalculatorUiEvent> // Add this stream
        fun onAction(action: CalculatorAction)
    }
}
