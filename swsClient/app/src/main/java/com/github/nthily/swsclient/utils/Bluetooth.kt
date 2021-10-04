package com.github.nthily.swsclient.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothReceiver(
    private val onDeviceStateChanged: (requestCode: Int, device: BluetoothDevice) -> Unit,
    private val onDiscoveryStarted: () -> Unit,
    private val onDiscoveryFinished: () -> Unit,
    private val onFoundDevice: (device: BluetoothDevice) -> Unit,
    private val onConnect: () -> Unit,
    private val onDisconnect: () -> Unit
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device?.name
                val deviceHardwareAddress = device?.address
                val bluetoothClass = device?.bluetoothClass?.deviceClass
                val bluetoothMajor = device?.bluetoothClass?.majorDeviceClass
                Utils.log("发现了 $deviceName 地址 $deviceHardwareAddress $bluetoothClass 子类型是 $bluetoothMajor")
                device?.let { onFoundDevice(it) }
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val requestCode = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let { onDeviceStateChanged(requestCode, it) }
            }
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> onDiscoveryStarted()
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> onDiscoveryFinished()
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val requestCode = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                if(requestCode == BluetoothAdapter.STATE_OFF) onDisconnect() else onConnect()
            }
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                val requestCode = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                when(requestCode) {
                    BluetoothAdapter.STATE_CONNECTING -> Utils.log("设备 ${device?.name} 正在连接")
                    BluetoothAdapter.STATE_DISCONNECTED -> Utils.log("设备 ${device?.name} 已断开连接")
                    BluetoothAdapter.STATE_CONNECTED -> Utils.log("设备 ${device?.name} 已连接")
                }
            }
        }
    }
}
