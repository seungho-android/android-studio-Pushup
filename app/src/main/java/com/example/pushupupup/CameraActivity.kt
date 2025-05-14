package com.example.pushupupup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.*

class CameraActivity : AppCompatActivity(), PoseLandmarkerHelper.LandmarkerListener {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var pushupText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var recordButton: Button
    private lateinit var resetButton: ImageButton
    private lateinit var startPushupButton: Button
    private lateinit var stopPushupButton: Button
    private lateinit var timerText: TextView

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private lateinit var imageAnalyzer: ImageAnalysis
    private var isFrontCamera = false

    private var previousState = "UP"
    private var pushupCount = 0
    private var isRecording = false
    private var isCounting = false

    private var tts: TextToSpeech? = null
    private var countdownTimer: CountDownTimer? = null

    private val RECORD_SCREEN_REQUEST_CODE = 1011

    // 상태 변경 지연 프레임 수
    private var downFrameCount = 0
    private var upFrameCount = 0
    private val requiredFrames = 2  // 연속으로 만족해야 상태 변경

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        pushupText = findViewById(R.id.pushupCountText)
        feedbackText = findViewById(R.id.feedbackText)
        recordButton = findViewById(R.id.recordButton)
        resetButton = findViewById(R.id.resetButton)
        startPushupButton = findViewById(R.id.startPushupButton)
        stopPushupButton = findViewById(R.id.stopPushupButton)
        timerText = findViewById(R.id.timerText)

        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts?.language = Locale.KOREAN
            }
        }

        if (allPermissionsGranted()) {
            setupPoseLandmarker()
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
        }

        startPushupButton.setOnClickListener { startPushupRoutine() }
        stopPushupButton.setOnClickListener { stopPushupRoutine() }
        recordButton.setOnClickListener {
            if (!isRecording) startScreenRecording() else stopScreenRecording()
        }
        resetButton.setOnClickListener {
            pushupCount = 0
            pushupText.text = "PUSH UPS: 0"
            Toast.makeText(this, "카운트 초기화됨", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPushupRoutine() {
        isCounting = false
        speak("푸쉬업 자세를 준비하세요. 5초 뒤 시작합니다.")
        Handler(Looper.getMainLooper()).postDelayed({
            countdownFrom(5) {
                isCounting = true
                startTimer()
            }
        }, 3000)
    }

    private fun stopPushupRoutine() {
        isCounting = false
        countdownTimer?.cancel()
        timerText.text = "타이머 종료됨"
    }

    private fun startTimer() {
        countdownTimer = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                timerText.text = "남은 시간: ${seconds}초"
            }

            override fun onFinish() {
                isCounting = false
                timerText.text = "타이머 종료됨"
                speak("1분이 지났습니다. 푸쉬업을 종료하세요.")
            }
        }.start()
    }

    private fun countdownFrom(seconds: Int, onFinish: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        for (i in 0..seconds) {
            handler.postDelayed({
                if (i < seconds) speak((seconds - i).toString())
                else {
                    speak("시작하세요.")
                    onFinish()
                }
            }, (i * 1000).toLong())
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
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
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
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

            val leftAngle = calculateAngle(lShoulder.x(), lShoulder.y(), lElbow.x(), lElbow.y(), lWrist.x(), lWrist.y())
            val rightAngle = calculateAngle(rShoulder.x(), rShoulder.y(), rElbow.x(), rElbow.y(), rWrist.x(), rWrist.y())
            val minAngle = min(leftAngle, rightAngle)

            if (isCounting) {
                if (previousState == "UP") {
                    if (minAngle <= 105) {
                        downFrameCount++
                        if (downFrameCount >= requiredFrames) {
                            previousState = "DOWN"
                            downFrameCount = 0
                        }
                    } else {
                        downFrameCount = 0
                    }
                } else if (previousState == "DOWN") {
                    if (minAngle >= 160) {
                        upFrameCount++
                        if (upFrameCount >= requiredFrames) {
                            previousState = "UP"
                            upFrameCount = 0
                            pushupCount++
                            pushupText.text = "PUSH UPS: $pushupCount"
                        }
                    } else {
                        upFrameCount = 0
                    }
                }
            }

            feedbackText.text = if (minAngle < 120) "정자세입니다!" else "더 낮게 내려가세요!"

            overlayView.setResults(result, resultBundle.inputImageHeight, resultBundle.inputImageWidth, RunningMode.LIVE_STREAM)
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

    override fun onDestroy() {
        super.onDestroy()
        tts?.shutdown()
        countdownTimer?.cancel()
    }

    private fun startScreenRecording() { /* 생략 */ }
    private fun stopScreenRecording() { /* 생략 */ }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { /* 생략 */ }
    override fun onError(error: String, errorCode: Int) { Log.e("Pose", "에러 발생: $error ($errorCode)") }
}
