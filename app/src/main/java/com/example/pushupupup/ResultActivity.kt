package com.example.pushupupup

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ResultActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecordAdapter
    private val records = mutableListOf<PushupRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        recyclerView = findViewById(R.id.resultRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecordAdapter(records)
        recyclerView.adapter = adapter

        val passedUserId = intent.getStringExtra("userId") ?: return

        findViewById<Button>(R.id.graphButton).setOnClickListener {
            val intent = android.content.Intent(this, GraphActivity::class.java)
            intent.putExtra("userId", passedUserId)
            startActivity(intent)
        }

        loadPushupData(passedUserId)
    }

    private fun loadPushupData(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .collection("records")
            .get()
            .addOnSuccessListener { result ->
                records.clear()
                val inputFormatDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val outputFormatDate = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
                val inputFormatTime = SimpleDateFormat("HHmmss", Locale.getDefault())
                val outputFormatTime = SimpleDateFormat("HH:mm", Locale.getDefault())

                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val age = doc.getString("age") ?: "?"
                    val count = doc.getLong("count")?.toInt() ?: 0
                    val dateRaw = doc.getString("date") ?: ""
                    val timeRaw = doc.getString("time") ?: ""

                    val formattedDate = try {
                        outputFormatDate.format(inputFormatDate.parse(dateRaw)!!)
                    } catch (e: Exception) {
                        dateRaw
                    }

                    val formattedTime = try {
                        outputFormatTime.format(inputFormatTime.parse(timeRaw)!!)
                    } catch (e: Exception) {
                        timeRaw
                    }

                    val dateTime = "$formattedDate $formattedTime"
                    records.add(PushupRecord(dateTime, name, age, count))
                }
                records.sortByDescending { it.dateTime }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ResultActivity", "데이터 로딩 실패: ${e.message}", e)
                Toast.makeText(this, "기록을 불러오지 못했습니다", Toast.LENGTH_LONG).show()
            }
    }
}
