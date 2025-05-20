package com.example.pushupupup

import android.graphics.Color
import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore

import java.text.SimpleDateFormat
import java.util.*

class GraphActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        lineChart = findViewById(R.id.lineChart)
        val userId = intent.getStringExtra("userId") ?: return

        loadData(userId)
    }

    private fun loadData(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .collection("records")
            .get()
            .addOnSuccessListener { result ->
                val entries = mutableListOf<Entry>()
                val labels = mutableListOf<String>()

                val sorted = result.sortedBy { it.getString("date") + it.getString("time") }

                sorted.forEachIndexed { index, doc ->
                    val date = doc.getString("date") ?: return@forEachIndexed
                    val time = doc.getString("time") ?: return@forEachIndexed
                    val count = doc.getLong("count")?.toFloat() ?: return@forEachIndexed

                    try {
                        val y = date.take(4)
                        val m = date.drop(4).take(2)
                        val d = date.drop(6).take(2)
                        val h = time.padStart(6, '0').take(2)
                        val min = time.padStart(6, '0').substring(2, 4)
                        val label = "$m/$d $h:$min"
                        labels.add(label)
                        entries.add(Entry(index.toFloat(), count))
                        Log.d("GraphActivity", "추가됨: label=$label count=$count")
                    } catch (e: Exception) {
                        Log.e("GraphActivity", "label 포맷 에러: $date $time", e)
                    }
                }

                if (entries.isEmpty()) {
                    Log.w("GraphActivity", "entries가 비어있습니다.")
                    return@addOnSuccessListener
                }

                val dataSet = LineDataSet(entries, "푸쉬업 개수").apply {
                    color = Color.CYAN
                    valueTextColor = Color.WHITE
                    lineWidth = 2f
                    circleRadius = 4f
                    setCircleColor(Color.YELLOW)
                    setDrawFilled(true)
                    fillAlpha = 50
                    fillColor = Color.CYAN
                }

                lineChart.data = LineData(dataSet)
                lineChart.description = Description().apply {
                    text = "전체 푸쉬업 기록"
                    textColor = Color.WHITE
                }

                lineChart.axisLeft.textColor = Color.WHITE
                lineChart.axisRight.isEnabled = false
                lineChart.xAxis.apply {
                    textColor = Color.WHITE
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawLabels(true)
                    valueFormatter = IndexAxisValueFormatter(labels)
                }

                lineChart.legend.textColor = Color.WHITE
                lineChart.invalidate()
            }
    }
}
