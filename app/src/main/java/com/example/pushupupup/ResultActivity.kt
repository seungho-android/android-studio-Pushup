package com.example.pushupupup

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ResultActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var database: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        resultTextView = findViewById(R.id.resultTextView)
        userId = intent.getStringExtra("userId")

        if (userId == null) {
            resultTextView.text = "사용자 정보를 불러올 수 없습니다."
            return
        }

        database = FirebaseDatabase.getInstance().reference
        loadPushupData()
    }

    private fun loadPushupData() {
        val userRef = database.child("pushup_records").child(userId!!)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    resultTextView.text = "운동 기록이 없습니다."
                    return
                }

                val builder = StringBuilder()
                val sortedDates = snapshot.children.sortedBy { it.key }

                for (dateSnapshot in sortedDates) {
                    val date = dateSnapshot.key ?: continue
                    val sortedTimes = dateSnapshot.children.sortedBy { it.key }

                    for (timeSnapshot in sortedTimes) {
                        val time = timeSnapshot.key ?: continue
                        val count = timeSnapshot.child("count").getValue(Int::class.java) ?: 0
                        val name = timeSnapshot.child("name").getValue(String::class.java) ?: ""
                        val age = timeSnapshot.child("age").getValue(String::class.java) ?: ""

                        builder.append("날짜: $date $time\n")
                        builder.append("이름: $name, 나이: $age\n")
                        builder.append("푸쉬업 개수: ${count}개\n\n")
                    }
                }

                resultTextView.text = builder.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                resultTextView.text = "데이터 로딩 실패: ${error.message}"
            }
        })
    }
}
