package com.github.nthily.swsclient

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.github.nthily.swsclient.ui.theme.SwsClientTheme
import com.github.nthily.swsclient.utils.Utils

class SwsclientApp: Application() {
    override fun onCreate() {
        super.onCreate()
        /*
        startKoin {
            androidLogger()
            androidContext(this@SwsclientApp)
            modules(
                module {
                    viewModel { AppViewModel() }
                }
            )
        }*/
    }
}

class MainActivity : ComponentActivity() {

    private val appViewModel by viewModels<AppViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(appViewModel)

        setContent {
            SwsClientTheme {

                val bthReady by remember { appViewModel.bthReady }
                val bthEnabled by remember { appViewModel.bthEnabled }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8F8F8))
                        .padding(top = 48.dp, start = 14.dp, end = 14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if(bthReady) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "设备名称",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.h6
                                )
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    appViewModel.bthDevice?.let {
                                        Text(
                                            text = it,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.h6
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.padding(vertical = 8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "开启蓝牙",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.h6
                                )
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Switch(
                                        checked = bthEnabled,
                                        onCheckedChange = {
                                            if(bthEnabled)
                                                appViewModel.disableBluetooth()
                                            else
                                                appViewModel.enableBluetooth()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedTrackColor = Color(0xFF0079D3),
                                            checkedThumbColor = Color(0xFF0079D3)
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.padding(vertical = 8.dp))
                            BthDeviceList(appViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BthDeviceList(
    appViewModel: AppViewModel
) {
    val context = LocalContext.current
    val pairedDevices = remember { appViewModel.pairedDevices }
    val scannedDevices  = remember { appViewModel.scannedDevices }
    val bthEnabled = remember { appViewModel.bthEnabled }
    val bthDiscovering = remember { appViewModel.bthDiscovering }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) appViewModel.startDeviceScan()
        else Toast.makeText(context, "开启权限失败", Toast.LENGTH_LONG).show()
    }

    if(bthEnabled.value) {
        Column {
            if(pairedDevices.isNotEmpty()) {
                PairedDevices(pairedDevices) {
                    appViewModel.connectBluetoothDevice(it)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "可用设备 ${scannedDevices.size}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.h6
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = {
                              if(!bthDiscovering.value) {
                                  if(ContextCompat.checkSelfPermission(
                                          context,
                                          Manifest.permission.ACCESS_FINE_LOCATION
                                      ) == PERMISSION_GRANTED) {
                                      appViewModel.startDeviceScan()
                                  } else {
                                      requestPermissionLauncher.launch(
                                          Manifest.permission.ACCESS_FINE_LOCATION
                                      )
                                  }
                              } else appViewModel.stopDeviceScan()
                        }
                    ) {
                        if(bthDiscovering.value) Icon(Icons.Filled.Close, null)
                        else Icon(Icons.Filled.Refresh, null)
                    }
                }
            }

            if(bthDiscovering.value)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp)),
                    color = Color(0xFF0079D3)
                )

            ScannedDevices(scannedDevices) {
                appViewModel.pairBlueToothDevice(it)
            }
        }
    }
}

@Composable
fun PairedDevices(
    pairedDevices: SnapshotStateList<BluetoothDevice>,
    connectBluetoothDevice: (device: BluetoothDevice) -> Unit
) {
    Text(
        text = "已配对设备",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.h6

    )
    pairedDevices.forEach { item ->
        Spacer(Modifier.padding(vertical = 5.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            elevation = 5.dp
        ){
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { connectBluetoothDevice(item) }
                        .padding(10.dp)
                ) {
                    when(item.bluetoothClass.deviceClass) {
                        BluetoothClass.Device.COMPUTER_LAPTOP -> Icon(painterResource(R.drawable.laptop), null)
                        BluetoothClass.Device.PHONE_SMART -> Icon(painterResource(R.drawable.smartphone), null)
                        BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> Icon(painterResource(R.drawable.headphones), null)
                        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER -> Icon(painterResource(R.drawable.tv), null)
                        BluetoothClass.Device.WEARABLE_WRIST_WATCH -> Icon(painterResource(R.drawable.watch), null)
                        BluetoothClass.Device.Major.UNCATEGORIZED -> Icon(painterResource(R.drawable.bluetooth), null)
                        else ->  Icon(painterResource(R.drawable.bluetooth), null)
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                    Column {
                        item.name?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.h6
                            )
                        }
                        item.address?.let {
                            Utils.SecondaryText(it)
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "已保存",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color.Green)
                )
            }
        }
    }
    Spacer(Modifier.padding(vertical = 8.dp))
}

@Composable
fun ScannedDevices(
    scannedDevices: SnapshotStateList<BluetoothDevice>,
    pairBluetoothDevice: (device: BluetoothDevice) -> Unit
) {
    scannedDevices.forEach { item ->
        Spacer(Modifier.padding(vertical = 5.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            elevation = 5.dp
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { pairBluetoothDevice(item) }
                    .padding(10.dp)
            ) {
                when(item.bluetoothClass.deviceClass) {
                    BluetoothClass.Device.COMPUTER_LAPTOP -> Icon(painterResource(R.drawable.laptop), null)
                    BluetoothClass.Device.PHONE_SMART -> Icon(painterResource(R.drawable.smartphone), null)
                    BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> Icon(painterResource(R.drawable.headphones), null)
                    BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER -> Icon(painterResource(R.drawable.tv), null)
                    BluetoothClass.Device.WEARABLE_WRIST_WATCH -> Icon(painterResource(R.drawable.watch), null)
                    BluetoothClass.Device.Major.UNCATEGORIZED -> Icon(painterResource(R.drawable.bluetooth), null)
                    else ->  Icon(painterResource(R.drawable.bluetooth), null)
                }
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Column {
                    item.name?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.h6
                        )
                    }
                    item.address?.let {
                        Utils.SecondaryText(it)
                    }
                }
            }
        }
    }
}
