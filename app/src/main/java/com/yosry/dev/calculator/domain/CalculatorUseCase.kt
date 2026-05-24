package com.yosry.dev.calculator.domain

// CalculatorOperation.kt
enum class CalculatorOperation(val symbol: String) {
    Add("+"),
    Subtract("-"),
    Multiply("×"),
    Divide("÷")
}

// CalculatorAction.kt
sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Decimal : CalculatorAction()
    object Calculate : CalculatorAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorAction()
}

// CalculateUseCase.kt
class CalculateUseCase {
    operator fun invoke(num1: String, num2: String, operation: CalculatorOperation): Double {
        val n1 = num1.toDoubleOrNull() ?: return 0.0
        val n2 = num2.toDoubleOrNull() ?: return 0.0

        return when (operation) {
            CalculatorOperation.Add -> n1 + n2
            CalculatorOperation.Subtract -> n1 - n2
            CalculatorOperation.Multiply -> n1 * n2
            CalculatorOperation.Divide -> if (n2 != 0.0) n1 / n2 else Double.NaN
        }
    }
}