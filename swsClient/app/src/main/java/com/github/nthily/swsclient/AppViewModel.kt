package com.github.nthily.swsclient

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context.BLUETOOTH_SERVICE
import android.content.IntentFilter
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.nthily.swsclient.utils.BluetoothReceiver
import com.github.nthily.swsclient.utils.Utils
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppViewModel(
    application: Application
) : AndroidViewModel(application), DefaultLifecycleObserver {

    private val app = getApplication<Application>()

    private val bthManager = app.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    private val bthAdapter: BluetoothAdapter? = bthManager.adapter

    private val MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"

    val bthDevice = bthAdapter?.name
    val pairedDevices = mutableStateListOf<BluetoothDevice>()
    val scannedDevices = mutableStateListOf<BluetoothDevice>()

    var bthReady = mutableStateOf(false)
    var bthEnabled = mutableStateOf(false)
    var bthDiscovering = mutableStateOf(false)
    var selectedPairedDevice = mutableStateOf<BluetoothDevice?>(null)

    private val bluetoothReceiver = BluetoothReceiver(
        onDeviceBondStateChanged = { requestCode, device ->
            Utils.log("设备 ${device.name} 状态是 ${device.bondState}")
            when(requestCode) {
                BluetoothDevice.BOND_NONE -> {
                    Utils.log("设备 ${device.name} 取消绑定了")
                    pairedDevices.remove(device)
                }
                BluetoothDevice.BOND_BONDED -> {
                    pairedDevices.add(device)
                    scannedDevices.remove(device)
                }
                BluetoothDevice.BOND_BONDING -> {
                    Utils.log("设备正在绑定")
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
        onConnect = { bthEnabled.value = true },
        onDisconnect = { bthEnabled.value = false }
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
            bthAdapter.bondedDevices.forEach {
                pairedDevices.add(it)
            }
        }
    }

    // MainActivity
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        app.unregisterReceiver(bluetoothReceiver)
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

    fun bondBluetoothDevice(device: BluetoothDevice) { device.createBond() }

    fun unBondBluetoothDevice(device: BluetoothDevice) { device.javaClass.getMethod("removeBond").invoke(device) }

    fun connectBluetoothDevice(device: BluetoothDevice) {
        Utils.log("开始连接")
        if(bthDiscovering.value) stopDeviceScan()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mBluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))

                mBluetoothSocket.connect()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
