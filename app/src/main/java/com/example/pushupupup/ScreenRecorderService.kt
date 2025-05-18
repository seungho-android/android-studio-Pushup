package com.example.pushupupup

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ScreenRecorderService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var videoFile: File? = null
    private var projectionManager: MediaProjectionManager? = null

    override fun onCreate() {
        super.onCreate()
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, buildNotification())

        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: return START_NOT_STICKY
        val data = intent.getParcelableExtra<Intent>("data") ?: return START_NOT_STICKY

        val metrics = DisplayMetrics().apply {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                wm.currentWindowMetrics.bounds.let {
                    widthPixels = it.width()
                    heightPixels = it.height()
                }
                densityDpi = resources.configuration.densityDpi
            } else {
                wm.defaultDisplay.getMetrics(this)
            }
        }

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val dpi = metrics.densityDpi

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "PushupVideo_$timeStamp.mp4"
        videoFile = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), fileName)

        mediaRecorder = MediaRecorder().apply {
            //setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(videoFile!!.absolutePath)
            setVideoSize(width, height)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            //setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncodingBitRate(8 * 1000 * 1000)
            setVideoFrameRate(30)
            prepare()
        }

        mediaProjection = projectionManager?.getMediaProjection(resultCode, data)
        mediaProjection?.createVirtualDisplay(
            "PushupRecorder",
            width, height, dpi,
            0,
            mediaRecorder!!.surface,
            null,
            null
        )

        mediaRecorder?.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaProjection?.stop()

            videoFile?.let { file ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/")
                    }
                    val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            file.inputStream().copyTo(outputStream)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ScreenRecorder", "Error stopping recorder: ${e.message}")
        }
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, CameraActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "record_channel")
            .setContentTitle("푸쉬업 녹화 중")
            .setContentText("운동 영상이 저장되고 있습니다.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "record_channel",
                "Screen Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
