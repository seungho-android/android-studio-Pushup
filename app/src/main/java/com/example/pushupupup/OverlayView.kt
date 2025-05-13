package com.example.pushupupup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var result: PoseLandmarkerResult? = null
    private var imageHeight: Int = 0
    private var imageWidth: Int = 0
    private var runningMode: RunningMode = RunningMode.LIVE_STREAM
    private var scaleFactor: Float = 1f
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f

    private val pointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 8f
    }

    private val linePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    fun setResults(
        poseResult: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode
    ) {
        this.result = poseResult
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        this.runningMode = runningMode

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }

        offsetX = (width - imageWidth * scaleFactor) / 2
        offsetY = (height - imageHeight * scaleFactor) / 2

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val landmarks = result?.landmarks()?.firstOrNull() ?: return

        // 점 그리기
        for (landmark in landmarks) {
            val x = landmark.x() * imageWidth * scaleFactor + offsetX
            val y = landmark.y() * imageHeight * scaleFactor + offsetY
            canvas.drawCircle(x, y, 8f, pointPaint)
        }

        // 관절 연결
        PoseLandmarker.POSE_LANDMARKS.forEach { connection ->
            val startIdx = connection!!.start()
            val endIdx = connection.end()
            if (startIdx < landmarks.size && endIdx < landmarks.size) {
                val start = landmarks[startIdx]
                val end = landmarks[endIdx]
                canvas.drawLine(
                    start.x() * imageWidth * scaleFactor + offsetX,
                    start.y() * imageHeight * scaleFactor + offsetY,
                    end.x() * imageWidth * scaleFactor + offsetX,
                    end.y() * imageHeight * scaleFactor + offsetY,
                    linePaint
                )
            }
        }
    }
}
