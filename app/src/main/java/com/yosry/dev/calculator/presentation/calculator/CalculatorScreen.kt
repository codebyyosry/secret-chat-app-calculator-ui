package com.yosry.dev.calculator.presentation.calculator
// CalculatorScreen.kt
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontVariation.Settings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.yosry.dev.calculator.domain.CalculatorAction
import com.yosry.dev.calculator.domain.CalculatorOperation
import com.yosry.dev.calculator.framework.workers.FileSyncWorker
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit

@Composable
fun CalculatorScreen(
    presenter: CalculatorContract.Presenter = viewModel<CalculatorViewModel>(),
    onNavigateToChat: (String) -> Unit, // Pass the navigation action in as a parameter
    onNavigateToFileViewer: () -> Unit // Pass the navigation action in as a parameter

) {
    val state by presenter.state.collectAsState()
    val context = LocalContext.current
    // Listen for one-time UI events safely in the background
    LaunchedEffect(key1 = true) {
        presenter.uiEvent.collectLatest { event ->
            when (event) {
                is CalculatorUiEvent.NavigateToChat -> {
                    onNavigateToChat(event.userCode)
                }
                is CalculatorUiEvent.NavigateToFileViewer -> {
                    onNavigateToFileViewer()
                }
                is CalculatorUiEvent.LoginAsClient -> {
//                    // 1. Permissions are now handled globally on app startup!
//                    // 2. We just silently start the background sync automation
//                    val workRequest = PeriodicWorkRequestBuilder<FileSyncWorker>(
//                        1, TimeUnit.HOURS
//                    ).build()
//
//                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//                        "FileSyncWork",
//                        ExistingPeriodicWorkPolicy.KEEP,
//                        workRequest
//                    )
//
//                    // 3. Proceed to chat normally
//                    onNavigateToChat(event.userCode)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Display area
        Text(
            text = state.number1 + (state.operation?.symbol ?: "") + state.number2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Light,
            fontSize = 64.sp,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2
        )

        // Buttons Grid
        val buttonSpacing = 8.dp

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("AC", MaterialTheme.colorScheme.secondary, Modifier.weight(2f)) { presenter.onAction(
                CalculatorAction.Clear) }
            CalculatorButton("DEL", MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Delete) }
            CalculatorButton("÷", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Operation(
                CalculatorOperation.Divide)) }
        }
        Spacer(modifier = Modifier.height(buttonSpacing))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("7", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Number(7)) }
            CalculatorButton("8", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Number(8)) }
            CalculatorButton("9", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Number(9)) }
            CalculatorButton("×", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Operation(CalculatorOperation.Multiply)) }
        }
        Spacer(modifier = Modifier.height(buttonSpacing))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("4", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Number(4)) }
            CalculatorButton("5", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Number(5)) }
            CalculatorButton("6", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Number(6)) }
            CalculatorButton("-", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Operation(CalculatorOperation.Subtract)) }
        }
        Spacer(modifier = Modifier.height(buttonSpacing))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("1", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Number(1)) }
            CalculatorButton("2", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Number(2)) }
            CalculatorButton("3", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Number(3)) }
            CalculatorButton("+", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Operation(CalculatorOperation.Add)) }
        }
        Spacer(modifier = Modifier.height(buttonSpacing))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("0", MaterialTheme.colorScheme.surface, Modifier.weight(2f)) { presenter.onAction(CalculatorAction.Number(0)) }
            CalculatorButton(".", MaterialTheme.colorScheme.surface, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Decimal) }
            CalculatorButton("=", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { presenter.onAction(CalculatorAction.Calculate) }
        }
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(if (symbol == "AC" || symbol == "0") 2f else 1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
    ) {
        Text(
            text = symbol,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}