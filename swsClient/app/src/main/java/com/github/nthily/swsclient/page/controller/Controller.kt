package com.github.nthily.swsclient.page.controller

import android.content.pm.ActivityInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.nthily.swsclient.R
import com.github.nthily.swsclient.utils.ComposeVerticalSlider
import com.github.nthily.swsclient.utils.ShiftButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.github.nthily.swsclient.utils.Utils.findActivity
import com.github.nthily.swsclient.utils.rememberComposeVerticalSliderState
import com.github.nthily.swsclient.viewModel.AppViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@ExperimentalComposeUiApi
@Composable
fun Controller(appViewModel: AppViewModel) {

    val brakeState = rememberComposeVerticalSliderState()
    val throttleState = rememberComposeVerticalSliderState()
    val context = LocalContext.current

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
            progressValue = 100,
            onProgressChanged =  {

            }
        ) {

        }
        Spacer(Modifier.padding(horizontal = 10.dp))
        ShiftButton(
            onClick = {
                val os = appViewModel.mBluetoothSocket.outputStream
                val str = "升挡".encodeToByteArray()
                os.write(str)
                os.flush()
            },
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.padding(horizontal = 60.dp))
        ShiftButton(
            onClick = { }
        ) {
            Icon(painterResource(id = R.drawable.remove), contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.padding(horizontal = 10.dp))
        ComposeVerticalSlider(
            state = brakeState,
            progressValue = 100,
            onProgressChanged =  {

            }
        ) {

        }
    }
}