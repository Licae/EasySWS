package com.github.nthily.swsclient.viewModel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context.BLUETOOTH_SERVICE
import android.content.IntentFilter
import android.os.Handler
import android.os.HandlerThread
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.nthily.swsclient.utils.BluetoothReceiver
import com.github.nthily.swsclient.utils.Utils
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(
    application: Application
) : AndroidViewModel(application), DefaultLifecycleObserver {

    private val app = getApplication<Application>()

    private val bthManager = app.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    private val bthAdapter: BluetoothAdapter? = bthManager.adapter

    private val uuid = "00001101-0000-1000-8000-00805F9B34FB"
    private var isBondingAnyDevice = false

    val bthDevice = bthAdapter?.name
    val pairedDevices = mutableStateListOf<BluetoothDevice>()
    val scannedDevices = mutableStateListOf<BluetoothDevice>()

    lateinit var mBluetoothSocket: BluetoothSocket
    var bthReady = mutableStateOf(false)
    var bthEnabled = mutableStateOf(false)
    var bthDiscovering = mutableStateOf(false)
    var bthDeviceConnectState = mutableStateOf(false)


    var selectedPairedDevice = mutableStateOf<BluetoothDevice?>(null)

    private val bluetoothReceiver = BluetoothReceiver(
        onDeviceBondStateChanged = { device, state, prevState ->
            Utils.log("设备 ${device.name} 状态是 ${device.bondState}")
            when(state) {
                BluetoothDevice.BOND_NONE -> {
                    Utils.log("test: $prevState")
                    if(prevState == BluetoothDevice.BOND_BONDING) {
                       // recompose
                        scannedDevices.remove(device)
                        scannedDevices.add(device)
                        isBondingAnyDevice = false
                    } else {
                        pairedDevices.remove(device)
                    }
                }
                BluetoothDevice.BOND_BONDED -> {
                    pairedDevices.add(device)
                    scannedDevices.remove(device)
                    isBondingAnyDevice = false
                }
                BluetoothDevice.BOND_BONDING -> {
                    Utils.log("设备正在绑定")

                    // recompose
                    scannedDevices.remove(device)
                    scannedDevices.add(0, device)
                }
            }
        },
        onDiscoveryStarted = { bthDiscovering.value = true },
        onDiscoveryFinished = { bthDiscovering.value = false },
        onFoundDevice = {

            // sort device from a -> z, not null -> null

            if(!pairedDevices.contains(it) && !scannedDevices.contains(it)) {
                if(scannedDevices.isEmpty()) scannedDevices.add(it)
                else {
                    val size = scannedDevices.size
                    for(index in 0 until scannedDevices.size) {
                        val device = scannedDevices[index]
                        if(it.name != null && device.name == null) continue
                        else if(it.name == null) {
                            scannedDevices.add(scannedDevices.size, it)
                            break
                        }
                        else if(device.name != null && it.name.compareTo(device.name, true) < 0) {
                            scannedDevices.add(index, it)
                            break
                        }
                    }
                    // If none of the devices in scannedDevices has a name, add it to the beginning
                    if(size == scannedDevices.size) scannedDevices.add(0, it)
                }
            }
        },
        onBluetoothConnected = {
            getBondedDevices()
            bthEnabled.value = true
        },
        onBluetoothDisconnected = { bthEnabled.value = false },
        onAnyBluetoothDeviceConnectionStateChanged = { bthDeviceConnectState.value = it }
    )

    // MainActivity
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        app.registerReceiver(
            bluetoothReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )

        if(bthAdapter != null) {
            bthReady.value = true
            bthEnabled.value = bthAdapter.isEnabled
            getBondedDevices()
        }
    }

    // MainActivity
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        app.unregisterReceiver(bluetoothReceiver)
    }

    private fun getBondedDevices() {
        bthAdapter!!.bondedDevices.forEach {
            if(!pairedDevices.contains(it)) pairedDevices.add(it)
        }
    }

    fun enableBluetooth() {
        bthAdapter?.enable()
    }

    fun disableBluetooth() {
        bthAdapter?.disable()
    }

    fun startDeviceScan() {
        scannedDevices.clear()
        bthAdapter?.startDiscovery()
    }

    fun stopDeviceScan() {
        bthAdapter?.cancelDiscovery()
    }

    fun bondDevice(device: BluetoothDevice) {
        if(!isBondingAnyDevice) {
            device.createBond()
            isBondingAnyDevice = true
        }
    }

    fun connectDevice(device: BluetoothDevice, navController: NavHostController) {
        val mBuffer = ByteArray(1024)
        if(bthDiscovering.value) stopDeviceScan()
        mBluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))

        viewModelScope.launch(Dispatchers.IO) {

            try {
                mBluetoothSocket.connect()

                val os = mBluetoothSocket.outputStream
                val str = "设备 ${device.name} 已经连接啦啦啦".encodeToByteArray()
                os.write(str)
                os.flush()

                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        while (true) {
                            val msgBytes = mBluetoothSocket.inputStream
                            val msg = mBuffer.decodeToString(endIndex = msgBytes.read(mBuffer))
                            Utils.log("接收到 $msg")
                            if(msg == "connected") {
                                viewModelScope.launch {
                                    navController.navigate("controller")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        viewModelScope.launch { navController.popBackStack() }
                    }
                }

            } catch (e: Exception) {
                Utils.log("服务端未开启\n ${e.printStackTrace()}")
                viewModelScope.launch { Toast.makeText(app.applicationContext, "无法连接", Toast.LENGTH_LONG).show() }
            }
        }
    }
}

fun BluetoothDevice.removeBond() {
    this.javaClass.getMethod("removeBond").invoke(this)
}
