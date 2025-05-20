package com.example.pushupupup

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val helpButton = findViewById<Button>(R.id.helpButton)
        helpButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("앱 사용법")
                .setMessage("""
                    1. 정보를 입력하고 로그인합니다.
                    2. '푸쉬업 하러가기'를 누릅니다.
                    3. '녹화 시작' 버튼을 눌러 녹화를 시작합니다.
                    4. '푸쉬업 시작하기'를 눌러 음성 안내에 따라 운동을 시작합니다.
                    5. 운동이 끝나면 '푸쉬업 종료' 버튼을 누릅니다.
                    6. '녹화 중지' 버튼을 눌러 저장합니다.
                    7. 저장된 영상은 갤러리에서 확인할 수 있습니다.
                """.trimIndent())
                .setPositiveButton("확인", null)
                .show()
        }
    }
}
