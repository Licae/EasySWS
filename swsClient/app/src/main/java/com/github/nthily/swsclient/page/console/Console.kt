package com.github.nthily.swsclient.page.console

import android.content.pm.ActivityInfo
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.github.nthily.swsclient.ui.view.ComposeVerticalSlider
import com.github.nthily.swsclient.ui.view.DownShiftButton
import com.github.nthily.swsclient.ui.view.UpShiftButton
import com.github.nthily.swsclient.ui.view.rememberComposeVerticalSliderState
import com.github.nthily.swsclient.utils.Joystick
import com.github.nthily.swsclient.utils.Utils.findActivity
import com.github.nthily.swsclient.viewModel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 控制器的界面

@RequiresApi(Build.VERSION_CODES.Q)
@ExperimentalComposeUiApi
@Composable
fun Console(
    appViewModel: AppViewModel,
    navController: NavController
) {

    val brakeState = rememberComposeVerticalSliderState()
    val throttleState = rememberComposeVerticalSliderState()
    val context = LocalContext.current
    val os = appViewModel.mBluetoothSocket.outputStream
    val scope = rememberCoroutineScope()

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
        ComposeVerticalSlider(
            state = throttleState,
            progressValue = 0,
            onProgressChanged =  {

            }
        ) {

        }
        Spacer(Modifier.padding(horizontal = 10.dp))
        UpShiftButton {
            scope.launch(Dispatchers.IO) {
                os.write(Joystick.sendUpShiftButtonsData(true))
                delay(100)
                os.write(Joystick.sendUpShiftButtonsData(false))
            }
        }
        Spacer(Modifier.padding(horizontal = 60.dp))
        DownShiftButton {
            scope.launch(Dispatchers.IO) {
                os.write(Joystick.sendDownShiftButtonsData(true))
                delay(100)
                os.write(Joystick.sendDownShiftButtonsData(false))
            }
        }
        Spacer(Modifier.padding(horizontal = 10.dp))
        ComposeVerticalSlider(
            state = brakeState,
            progressValue = 0,
            onProgressChanged =  {

            }
        ) {

        }
    }

    BackHandler(
        enabled = navController.currentBackStackEntry?.destination?.route == "console"
    ) {
        appViewModel.mBluetoothSocket.close()
    }

}
