package com.github.nthily.swsclient.utils

// 发送控制器的数据到服务器端的代码
// 数据包的构成为 [数据包长度][类型][数据]

// 数据包长度用于接收端解析数据包的数据

object Joystick {

    fun sendUpShiftButtonsData(
        press: Boolean
    ): ByteArray {

        val pressValue = if (press) 1 else 0

        val packetSize = (UInt.SIZE_BYTES + 1).toByteArray() // Unit + Boolean, 按钮的位置 / 按压状态
        val packetType = JoystickState.buttons.toByteArray()
        val packetData = (pressValue shl Buttons.upShiftButton.toInt()).toByteArray()
        return packetSize + packetType + packetData
    }

    fun sendDownShiftButtonsData(
        press: Boolean
    ): ByteArray {
        val pressValue = if (press) 1 else 0

        val packetSize = (UInt.SIZE_BYTES + 1).toByteArray() // Unit + Boolean, 按钮的位置 / 按压状态
        val packetType = JoystickState.buttons.toByteArray()
        val packetData = (pressValue shl Buttons.downShiftButton.toInt()).toByteArray()
        return packetSize + packetType + packetData
    }

}

object JoystickState { // vJoy 数据类型
    const val buttons = 1
    const val Axis = 2
}

object Buttons { // vJoy 按钮的位置
    const val upShiftButton: UInt = 1u
    const val downShiftButton: UInt = 2u
}

fun Int.toByteArray() = this.let {
    byteArrayOf(
        (it and 0xFF).toByte(), ((it ushr 8) and 0xFF).toByte(),
        ((it ushr 16) and 0xFF).toByte(), ((it ushr 24) and 0xFF).toByte()
    )
}

fun UInt.toByteArray() = this.let {
    byteArrayOf(
        (it and 0xFFu).toByte(), ((it shr 8) and 0xFFu).toByte(),
        ((it shr 16) and 0xFFu).toByte(), ((it shr 24) and 0xFFu).toByte()
    )
}
