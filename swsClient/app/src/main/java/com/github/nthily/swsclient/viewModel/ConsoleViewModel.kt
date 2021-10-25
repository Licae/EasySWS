package com.github.nthily.swsclient.viewModel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.github.nthily.swsclient.utils.PacketType
import com.github.nthily.swsclient.utils.Sender
import com.github.nthily.swsclient.utils.Utils
import com.github.nthily.swsclient.utils.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStream

class ConsoleViewModel(
    application: Application,
) : AndroidViewModel(application), DefaultLifecycleObserver, SensorEventListener {

    private val app = getApplication<Application>()
    private lateinit var sensorManager: SensorManager

    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gamometer: Sensor? = null

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var rotation = FloatArray(3)

    private var supportedSensorLevel = SupportedSensorLevel.NONE
    private val matrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var updateScope = CoroutineScope(Dispatchers.IO)

    var brakeValue = mutableStateOf(0f)
    var throttleValue = mutableStateOf(0f)
    var onSensorDataChanged: ((data: Float) -> Unit)? = null

    // MainActivity
    override fun onCreate(owner: LifecycleOwner) {

        super.onCreate(owner)

        sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gamometer = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

        if (gamometer != null)
            supportedSensorLevel = SupportedSensorLevel.FULL
        else if (accelerometer != null && magnetometer != null)
            supportedSensorLevel = SupportedSensorLevel.MEDIUM
        else if (accelerometer != null && magnetometer == null)
            supportedSensorLevel = SupportedSensorLevel.BASIC
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        sensorManager.unregisterListener(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        registerSensorListeners()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        updateScope.launch {
            when (event?.sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> gravity = event.values
                Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
                Sensor.TYPE_GAME_ROTATION_VECTOR -> rotation = event.values
            }
            val x = when (supportedSensorLevel) {
                SupportedSensorLevel.BASIC -> {
                    ((1 + (gravity[1] / SensorManager.STANDARD_GRAVITY)) / 2)
                }
                SupportedSensorLevel.MEDIUM -> {
                    SensorManager.getRotationMatrix(
                        matrix, null, gravity, geomagnetic
                    )
                    SensorManager.getOrientation(matrix, orientation)
                    (((Math.PI / 2) - orientation[1]) / Math.PI).toFloat()
                }
                SupportedSensorLevel.FULL -> {
                    SensorManager.getRotationMatrixFromVector(matrix, rotation)
                    SensorManager.getOrientation(matrix, orientation)
                    (((Math.PI / 2) - orientation[1]) / Math.PI).toFloat()
                }
                else -> 0.5F
            }.coerceIn(0.0F, 1.0F)
            onSensorDataChanged?.invoke(x)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    fun registerSensorListeners() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gamometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun unregisterListener () {
        sensorManager.unregisterListener(this)
    }

    private enum class SupportedSensorLevel {
        NONE, BASIC, MEDIUM, FULL
    }
}
