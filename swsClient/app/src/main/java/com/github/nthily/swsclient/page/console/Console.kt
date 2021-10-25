package com.github.nthily.swsclient.page.console

import android.content.pm.ActivityInfo
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nthily.swsclient.ui.view.ComposeVerticalSlider
import com.github.nthily.swsclient.ui.view.DownShiftButton
import com.github.nthily.swsclient.ui.view.UpShiftButton
import com.github.nthily.swsclient.ui.view.rememberComposeVerticalSliderState
import com.github.nthily.swsclient.utils.Sender
import com.github.nthily.swsclient.utils.Utils
import com.github.nthily.swsclient.utils.Utils.findActivity
import com.github.nthily.swsclient.viewModel.AppViewModel
import com.github.nthily.swsclient.viewModel.ConsoleViewModel
import com.github.nthily.swsclient.viewModel.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

// 控制器的界面

@RequiresApi(Build.VERSION_CODES.Q)
@ExperimentalComposeUiApi
@Composable
fun Console(
    appViewModel: AppViewModel,
    consoleViewModel: ConsoleViewModel,
    navController: NavController
) {

    val throttleState = rememberComposeVerticalSliderState()
    val throttleValue by remember { consoleViewModel.throttleValue }

    val brakeState = rememberComposeVerticalSliderState()
    val brakeValue by remember { consoleViewModel.brakeValue}

    val os = appViewModel.mBluetoothSocket.outputStream
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    DisposableEffect(true) {
        // 当进入这个 Composable
        consoleViewModel.registerSensorListeners()
        consoleViewModel.onSensorDataChanged = { data ->
            if (appViewModel.mBluetoothSocket.isConnected && appViewModel.serverEnabled.value) {
                scope.launch(Dispatchers.IO) { os.write(Sender.getSensorData(data)) }
            }
        }
        onDispose { // 当离开这个 Composable
            consoleViewModel.unregisterListener()   // 取消传感器监听器
        }
    }

    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose { }
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 15.dp),
    ) {
        ComposeVerticalSlider( // 刹车
            state = brakeState,
            progressValue = (brakeValue * 100).toInt(),
            onProgressChanged =  {
                consoleViewModel.brakeValue.value = it / 100f
                scope.launch(Dispatchers.IO) { os.write(Sender.getBrakeData(brakeValue)) }
            }
        ) {
            consoleViewModel.brakeValue.value = 0f
            brakeState.update(0)
            scope.launch(Dispatchers.IO) { os.write(Sender.getBrakeData(brakeValue)) }
        }
        Spacer(Modifier.padding(horizontal = 10.dp))
        UpShiftButton { // 升档
            scope.launch(Dispatchers.IO) {
                os.write(Sender.getUpShiftButtonsData(true))
                delay(150)
                os.write(Sender.getUpShiftButtonsData(false))
            }
        }
        Spacer(Modifier.padding(horizontal = 60.dp))
        DownShiftButton { // 降档
            scope.launch(Dispatchers.IO) {
                os.write(Sender.getDownShiftButtonsData(true))
                delay(150)
                os.write(Sender.getDownShiftButtonsData(false))
            }
        }
        Spacer(Modifier.padding(horizontal = 10.dp))
        ComposeVerticalSlider( // 油门
            state = throttleState,
            progressValue = (throttleValue * 100).toInt(),
            onProgressChanged =  {
                consoleViewModel.throttleValue.value = it / 100f
                scope.launch(Dispatchers.IO) { os.write(Sender.getThrottleData(throttleValue)) }
            }
        ) {
            consoleViewModel.throttleValue.value = 0f
            throttleState.update(0)
            scope.launch(Dispatchers.IO) { os.write(Sender.getThrottleData(throttleValue)) }
        }
    }

    BackHandler(
        enabled = navController.currentBackStackEntry?.destination?.route == Screen.Console.route
    ) {
        appViewModel.mBluetoothSocket.close()
    }
}
