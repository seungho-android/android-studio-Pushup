package com.example.pushupupup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        val goPushupBtn: Button = findViewById(R.id.goPushupButton)
        val viewResultBtn: Button = findViewById(R.id.viewResultButton)

        goPushupBtn.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        viewResultBtn.setOnClickListener {
            // 추후 Firebase 연동 예정
        }
    }
}
