package com.example.pushupupup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        val helpButton = findViewById<Button>(R.id.helpButton)
        val problemButton = findViewById<Button>(R.id.problemButton)

        startButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        helpButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("앱 사용법")
                .setMessage(
                    "정보를 입력하고 로그인합니다.\n" +
                            "그 후 '푸쉬업 하러가기'를 누르고 녹화 시작 버튼을 누릅니다.\n" +
                            "푸쉬업 시작 버튼을 누른 후 음성에 맞춰 푸쉬업을 시작합니다.\n" +
                            "측정이 완료되면 푸쉬업 종료 버튼을 누르고 녹화 종료 버튼을 누릅니다.\n" +
                            "갤러리에 저장된 자신의 푸쉬업 자세를 점검합니다."
                )
                .setPositiveButton("확인", null)
                .show()
        }

        problemButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("문제 해결 안내")
                .setMessage(
                    "카메라 권한 허용 시에도 화면이 보이지 않는다면\n" +
                            "뒤로가기 후 다시 눌러주세요.\n\n" +
                            "이 외에 문제가 발생한 경우\n" +
                            "rla112101@naver.com 으로 이메일을 보내주세요."
                )
                .setPositiveButton("확인", null)
                .show()
        }
    }
}
