package com.example.pushupupup

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.concurrent.Executors
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.min

class CameraActivity : AppCompatActivity(), PoseLandmarkerHelper.LandmarkerListener {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var pushupText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var recordButton: Button
    private lateinit var resetButton: ImageButton

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private lateinit var imageAnalyzer: ImageAnalysis
    private var isFrontCamera = false

    private var previousState = "UP"
    private var pushupCount = 0
    private var isRecording = false

    private val RECORD_SCREEN_REQUEST_CODE = 1011

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        pushupText = findViewById(R.id.pushupCountText)
        feedbackText = findViewById(R.id.feedbackText)
        recordButton = findViewById(R.id.recordButton)
        resetButton = findViewById(R.id.resetButton)

        if (allPermissionsGranted()) {
            setupPoseLandmarker()
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), 1001
            )
        }

        recordButton.setOnClickListener {
            if (!isRecording) {
                startScreenRecording()
            } else {
                stopScreenRecording()
            }
        }

        resetButton.setOnClickListener {
            pushupCount = 0
            pushupText.text = "PUSH UPS: 0"
            Toast.makeText(this, "카운트 초기화됨", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun setupPoseLandmarker() {
        poseLandmarkerHelper = PoseLandmarkerHelper(
            context = this,
            runningMode = RunningMode.LIVE_STREAM,
            poseLandmarkerHelperListener = this
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            isFrontCamera = cameraSelector.lensFacing == CameraSelector.LENS_FACING_FRONT

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        poseLandmarkerHelper.detectLiveStream(imageProxy, isFrontCamera)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("Camera", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        runOnUiThread {
            if (resultBundle.results.isEmpty()) return@runOnUiThread

            val result = resultBundle.results.firstOrNull() ?: return@runOnUiThread
            val landmarks = result.landmarks().firstOrNull() ?: return@runOnUiThread

            val lShoulder = landmarks.getOrNull(11) ?: return@runOnUiThread
            val lElbow = landmarks.getOrNull(13) ?: return@runOnUiThread
            val lWrist = landmarks.getOrNull(15) ?: return@runOnUiThread
            val rShoulder = landmarks.getOrNull(12) ?: return@runOnUiThread
            val rElbow = landmarks.getOrNull(14) ?: return@runOnUiThread
            val rWrist = landmarks.getOrNull(16) ?: return@runOnUiThread

            val leftAngle = calculateAngle(
                lShoulder.x(), lShoulder.y(),
                lElbow.x(), lElbow.y(),
                lWrist.x(), lWrist.y()
            )

            val rightAngle = calculateAngle(
                rShoulder.x(), rShoulder.y(),
                rElbow.x(), rElbow.y(),
                rWrist.x(), rWrist.y()
            )

            val minAngle = min(leftAngle, rightAngle)

            if (previousState == "UP" && minAngle <= 110) {
                previousState = "DOWN"
            } else if (previousState == "DOWN" && minAngle >= 160) {
                previousState = "UP"
                pushupCount += 1
                pushupText.text = "PUSH UPS: $pushupCount"
            }

            feedbackText.text = if (minAngle < 120) "정자세입니다!" else "더 낮게 내려가세요!"

            overlayView.setResults(
                result,
                resultBundle.inputImageHeight,
                resultBundle.inputImageWidth,
                RunningMode.LIVE_STREAM
            )
            overlayView.invalidate()
        }
    }

    private fun calculateAngle(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Double {
        val ab = floatArrayOf(ax - bx, ay - by)
        val cb = floatArrayOf(cx - bx, cy - by)

        val dotProduct = ab[0] * cb[0] + ab[1] * cb[1]
        val magnitudeAB = sqrt(ab[0].pow(2) + ab[1].pow(2))
        val magnitudeCB = sqrt(cb[0].pow(2) + cb[1].pow(2))

        return Math.toDegrees(acos((dotProduct / (magnitudeAB * magnitudeCB)).coerceIn(-1.0f, 1.0f).toDouble()))
    }

    private fun startScreenRecording() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = projectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, RECORD_SCREEN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECORD_SCREEN_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val serviceIntent = Intent(this, ScreenRecorderService::class.java).apply {
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            isRecording = true
            recordButton.text = "녹화 중지"
            Toast.makeText(this, "녹화 시작됨", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopScreenRecording() {
        stopService(Intent(this, ScreenRecorderService::class.java))
        isRecording = false
        recordButton.text = "녹화 시작"
        Toast.makeText(this, "녹화 저장됨", Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: String, errorCode: Int) {
        Log.e("Pose", "에러 발생: $error ($errorCode)")
    }
}
