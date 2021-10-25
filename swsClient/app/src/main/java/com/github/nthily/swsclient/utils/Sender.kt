package com.github.nthily.swsclient.utils

// 发送控制器的数据到服务器端的代码
// 数据包的构成为 [数据包长度][数据包类型][输入/.../.../][数据]

// 数据包长度用于接收端解析数据包的数据
// 数据包类型表示这个数据包用于发送什么，在底下 PacketType 中，input 就代表输入 vJoy，代表发送控制 vJoy 手柄的数据包。以此类推，可以有 message 类型等
// 子类型，根据第二个参数而定
// 数据

object Sender {

    fun sendUpShiftButtonsData(
        press: Boolean
    ): ByteArray {

        val upShiftButton: Byte = 0 // 按钮位置
        val pressValue: Byte = if (press) 1 else 0 // 按下 / 松开

        val input = byteArrayOf(PacketType.Input.buttons)

        val packetData = (pressValue.toInt() shl upShiftButton.toInt()).toByteArray()
        val packetType = byteArrayOf(PacketType.input)
        val packetSize = byteArrayOf((packetType.size + packetData.size + input.size).toByte())

        return packetSize + packetType + input + packetData // 返回数据包 [长度][包类型][按钮ID][按钮状态]
    }

    fun sendDownShiftButtonsData(
        press: Boolean,
    ): ByteArray {

        val downShiftButton: Byte = 1 // 按钮位置
        val pressValue: Byte = if (press) 1 else 0 // 按下 / 松开

        val input = byteArrayOf(PacketType.Input.buttons)

        val packetData = (pressValue.toInt() shl downShiftButton.toInt()).toByteArray()
        val packetType = byteArrayOf(PacketType.input)
        val packetSize = byteArrayOf((packetType.size + packetData.size + input.size).toByte())

        return packetSize + packetType + input + packetData // 返回数据包 [长度][包类型][按钮ID][按钮状态]
    }
}

object PacketType { // 数据包类型

    const val input: Byte = 3

    // 具体类型

    object Input { // vJoy 类型 ID
        const val buttons: Byte = 1
        const val axisX: Byte = 11
        const val axisY: Byte = 12
    }
}

fun Int.toByteArray() = this.let {
    byteArrayOf(
        (it and 0xFF).toByte(), ((it ushr 8) and 0xFF).toByte(),
        ((it ushr 16) and 0xFF).toByte(), ((it ushr 24) and 0xFF).toByte()
    )
}

fun Float.toByteArray() = this.toBits().let {
    byteArrayOf(
        (it and 0xFF).toByte(), ((it ushr 8) and 0xFF).toByte(),
        ((it ushr 16) and 0xFF).toByte(), ((it ushr 24) and 0xFF).toByte()
    )
}
