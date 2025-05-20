package com.example.pushupupup

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var idInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        nameInput = findViewById(R.id.signupNameInput)
        idInput = findViewById(R.id.signupIdInput)
        ageInput = findViewById(R.id.signupAgeInput)
        passwordInput = findViewById(R.id.signupPasswordInput)
        signupButton = findViewById(R.id.signupButton)

        signupButton.setOnClickListener {
            val name = nameInput.text.toString()
            val id = idInput.text.toString()
            val age = ageInput.text.toString()
            val password = passwordInput.text.toString()

            if (name.isBlank() || id.isBlank() || age.isBlank() || password.isBlank()) {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(id)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    Toast.makeText(this, "이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show()
                } else {
                    val userData = hashMapOf(
                        "name" to name,
                        "age" to age,
                        "password" to password
                    )
                    userRef.set(userData).addOnSuccessListener {
                        Toast.makeText(this, "회원가입 성공! 로그인 해주세요", Toast.LENGTH_SHORT).show()
                        finish() // 돌아가기
                    }.addOnFailureListener {
                        Toast.makeText(this, "회원가입 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "네트워크 오류: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
