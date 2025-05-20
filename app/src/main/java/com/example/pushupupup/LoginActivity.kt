package com.example.pushupupup

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val idInput: EditText = findViewById(R.id.idInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val loginButton: Button = findViewById(R.id.loginButton)
        val signupButton: Button = findViewById(R.id.signupButton)

        loginButton.setOnClickListener {
            val id = idInput.text.toString()
            val password = passwordInput.text.toString()

            if (id.isNotEmpty() && password.isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("users").document(id)

                userRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val storedPassword = document.getString("password")
                        if (storedPassword == password) {
                            val intent = Intent(this, SelectActivity::class.java)
                            intent.putExtra("userName", document.getString("name"))
                            intent.putExtra("userId", id)
                            intent.putExtra("userAge", document.getString("age"))
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "존재하지 않는 아이디입니다", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "네트워크 오류: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
