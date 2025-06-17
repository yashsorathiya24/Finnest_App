package com.example.finnest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var signUpButton: Button
    private lateinit var signInText: TextView

    private var formSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize views
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        signUpButton = findViewById(R.id.signUpButton)
        signInText = findViewById(R.id.signUpTextView)

        // Hide error when user types
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (formSubmitted && isValidEmail(s.toString())) {
                    emailInputLayout.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Sign Up button click
        signUpButton.setOnClickListener {
            formSubmitted = true
            val email = emailEditText.text.toString().trim()

            if (validateEmail(email)) {
                Toast.makeText(this, "Email is valid!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, VerificationAcitvity::class.java)
                startActivity(intent)
            }
        }

        // Navigate to sign-in
        signInText.setOnClickListener {
            finish()
        }
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                emailInputLayout.error = "Email is required"
                false
            }
            !isValidEmail(email) -> {
                emailInputLayout.error = "Enter a valid email address"
                false
            }
            else -> {
                emailInputLayout.error = null
                true
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
