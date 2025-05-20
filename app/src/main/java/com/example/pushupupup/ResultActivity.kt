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
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        recyclerView = findViewById(R.id.resultRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        userId = intent.getStringExtra("userId")
        if (userId == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ 인터페이스 방식으로 RecordAdapter 생성
        adapter = RecordAdapter(records, object : OnRecordDeleteListener {
            override fun onDelete(record: PushupRecord) {
                deleteRecordFromFirestore(record)
            }
        })

        recyclerView.adapter = adapter

        findViewById<Button>(R.id.graphButton).setOnClickListener {
            val intent = android.content.Intent(this, GraphActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        loadPushupData(userId!!)
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
                    records.add(PushupRecord(dateTime, name, age, count, dateRaw, timeRaw))
                }
                records.sortByDescending { it.dateTime }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ResultActivity", "데이터 로딩 실패: ${e.message}", e)
                Toast.makeText(this, "기록을 불러오지 못했습니다", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteRecordFromFirestore(record: PushupRecord) {
        val db = FirebaseFirestore.getInstance()
        val recordId = "${record.dateRaw}_${record.timeRaw}"

        userId?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("records")
                .document(recordId)
                .delete()
                .addOnSuccessListener {
                    records.remove(record)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "기록이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("ResultActivity", "삭제 실패: ${e.message}", e)
                    Toast.makeText(this, "기록 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
