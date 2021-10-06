package com.github.nthily.swsclient

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.github.nthily.swsclient.utils.SecondaryText
import com.github.nthily.swsclient.utils.Utils
import kotlinx.coroutines.launch

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
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(appViewModel)

        setContent {
            SwsClientTheme {

                val bthReady by remember { appViewModel.bthReady }
                val bthEnabled by remember { appViewModel.bthEnabled }
                val selectedDevice by remember { appViewModel.selectedPairedDevice }

                val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)


                ModalBottomSheetLayout(
                    sheetState = sheetState,
                    sheetContent = {
                        SheetContent(
                            device = selectedDevice,
                            sheetState = sheetState,
                            connectDevice = { appViewModel.connectBluetoothDevice(it) },
                            unBondDevice = { appViewModel.unBondBluetoothDevice(it) }
                        )
                    }
                ) {
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
                                BthDeviceList(appViewModel, sheetState)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BthDeviceList(
    appViewModel: AppViewModel,
    sheetState: ModalBottomSheetState,
) {
    val context = LocalContext.current
    val pairedDevices = remember { appViewModel.pairedDevices }
    val scannedDevices  = remember { appViewModel.scannedDevices }
    val bthEnabled = remember { appViewModel.bthEnabled }
    val bthDiscovering = remember { appViewModel.bthDiscovering }
    val scope = rememberCoroutineScope()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) appViewModel.startDeviceScan()
        else Toast.makeText(context, "开启权限失败", Toast.LENGTH_LONG).show()
    }

    if(bthEnabled.value) {
        Column {
            if(pairedDevices.isNotEmpty()) {
                PairedDevices(
                    pairedDevices = pairedDevices,
                    onClickPairedDevice = {
                        appViewModel.selectedPairedDevice.value = it
                        scope.launch {
                            sheetState.show()
                        }
                    }
                )
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
                appViewModel.bondBluetoothDevice(it)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PairedDevices(
    pairedDevices: SnapshotStateList<BluetoothDevice>,
    onClickPairedDevice: (selectedDevice: BluetoothDevice) -> Unit
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
                        .clickable {
                            onClickPairedDevice(item)
                        }
                        .padding(10.dp)
                ) {
                    GetDeviceIcon(item)
                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                    Column {
                        item.name?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.h6
                            )
                        }
                        item.address?.let {
                            SecondaryText(it)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Green)
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
                GetDeviceIcon(item)
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Column {
                    item.name?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.h6
                        )
                    }
                    item.address?.let {
                        SecondaryText(it)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetContent(
    device: BluetoothDevice?,
    sheetState: ModalBottomSheetState,
    connectDevice: (device: BluetoothDevice) -> Unit,
    unBondDevice: (device: BluetoothDevice) -> Unit
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "设备 ${device?.name} 已配对",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        SecondaryText(str = "你可能想要")
        Spacer(Modifier.padding(vertical = 15.dp))
        Button(
            onClick = {
                device?.let {
                    connectDevice(it)
                    scope.launch {
                        sheetState.hide()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF44D670)),
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Text("连接设备", color = Color.White)
        }
        Spacer(Modifier.padding(vertical = 15.dp))
        Button(
            onClick = {
                  device?.let {
                      unBondDevice(it)
                      scope.launch {
                          sheetState.hide()
                      }
                  }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF8488A5)),
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),

        ) {
            Text("取消配对")
        }
    }

    BackHandler(
        enabled = (sheetState.currentValue == ModalBottomSheetValue.HalfExpanded
                || sheetState.currentValue == ModalBottomSheetValue.Expanded),
    ) {
        scope.launch {
            sheetState.hide()
        }
    }

}

@Composable
fun GetDeviceIcon(device: BluetoothDevice) {
    val icon: Unit = when(device.bluetoothClass.deviceClass) {
        BluetoothClass.Device.COMPUTER_LAPTOP -> Icon(painterResource(R.drawable.laptop), null)
        BluetoothClass.Device.PHONE_SMART -> Icon(painterResource(R.drawable.smartphone), null)
        BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> Icon(painterResource(R.drawable.headphones), null)
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER -> Icon(painterResource(R.drawable.tv), null)
        BluetoothClass.Device.WEARABLE_WRIST_WATCH -> Icon(painterResource(R.drawable.watch), null)
        BluetoothClass.Device.Major.UNCATEGORIZED -> Icon(painterResource(R.drawable.bluetooth), null)
        else -> Icon(painterResource(R.drawable.bluetooth), null)
    }
    return icon
}