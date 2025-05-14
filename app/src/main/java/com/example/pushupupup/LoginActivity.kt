package com.example.pushupupup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val ageInput = findViewById<EditText>(R.id.ageInput)
        val idInput = findViewById<EditText>(R.id.idInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val name = nameInput.text.toString()
            val age = ageInput.text.toString()
            val id = idInput.text.toString()
            val password = passwordInput.text.toString()

            if (name.isNotBlank() && age.isNotBlank() && id.isNotBlank() && password.isNotBlank()) {
                val intent = Intent(this, SelectActivity::class.java)
                intent.putExtra("userName", name)
                intent.putExtra("userAge", age)
                startActivity(intent)
            } else {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
