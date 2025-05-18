package com.example.pushupupup

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val nameInput: EditText = findViewById(R.id.nameInput)
        val idInput: EditText = findViewById(R.id.idInput)
        val ageInput: EditText = findViewById(R.id.ageInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val name = nameInput.text.toString()
            val age = ageInput.text.toString()
            val id = idInput.text.toString()
            val password = passwordInput.text.toString()

            if (name.isNotEmpty() && id.isNotEmpty() && password.isNotEmpty()) {
                val intent = Intent(this, SelectActivity::class.java)
                intent.putExtra("userName", name)   // ✅ 이름
                intent.putExtra("userId", id)       // ✅ 아이디
                intent.putExtra("userAge", age)    // ✅ (예시) 나이 — 필요하면 EditText 하나 더 만들어서 받기
                startActivity(intent)
            } else {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
