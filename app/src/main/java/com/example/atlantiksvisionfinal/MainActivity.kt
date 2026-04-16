/*
 * Разработано: Юрий (CeBePOK74)
 * Проект: Universal AI Vision AR
 * Логика: Автономное распознавание поверхностей без Google ARCore
 * Все права защищены (c) 2026
 */
package com.example.atlantiksvisionfinal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.ar.core.ArCoreApk

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var gridOverlay: GridOverlayView
    private lateinit var tvStatus: TextView

    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    // Переменная для хранения последней позиции пальца
    private var lastY: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFinder = findViewById(R.id.viewFinder)
        gridOverlay = findViewById(R.id.gridOverlay)
        tvStatus = findViewById(R.id.tv_status)
        val btnScan = findViewById<Button>(R.id.btn_scan)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        btnScan.setOnClickListener { checkVRSupport() }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // Добавляем управление пальцем для масштабирования сетки
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = lastY - event.y
                // Меняем размер ячейки (минимум 50, максимум 800)
                gridOverlay.gridStep = (gridOverlay.gridStep + deltaY).coerceIn(50f, 800f)
                gridOverlay.invalidate()
                lastY = event.y
            }
        }
        return true
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
            gridOverlay.updateRotation(roll)
        }
    }

    private fun checkVRSupport() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isSupported) {
            tvStatus.text = "ATLANTIKS: VR READY"
        } else {
            tvStatus.text = "ATLANTIKS: SENSOR MODE"
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                tvStatus.text = "ATLANTIKS SYSTEM: ACTIVE"
            } catch (exc: Exception) {
                Toast.makeText(this, "Ошибка камеры", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}