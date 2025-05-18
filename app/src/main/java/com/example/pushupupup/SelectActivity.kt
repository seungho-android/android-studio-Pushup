package com.example.pushupupup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        // LoginActivity → 전달받은 회원 정보
        val userName = intent.getStringExtra("userName") ?: "unknown"
        val userId = intent.getStringExtra("userId") ?: ""
        val userAge = intent.getStringExtra("userAge") ?: ""

        val goPushupButton: Button = findViewById(R.id.goPushupButton)
        val checkResultButton: Button = findViewById(R.id.checkResultButton)

        // 푸쉬업 하러 가기
        goPushupButton.setOnClickListener {
            val pushupIntent = Intent(this, CameraActivity::class.java).apply {
                putExtra("userName", userName)
                putExtra("userId", userId)
                putExtra("userAge", userAge)
            }
            startActivity(pushupIntent)
        }

        // 내 결과 확인하기
        checkResultButton.setOnClickListener {
            val resultIntent = Intent(this, ResultActivity::class.java).apply {
                putExtra("userId", userId) // ✅ userId만 넘기면 충분 (이걸로 Firebase 조회)
            }
            startActivity(resultIntent)
        }
    }
}
