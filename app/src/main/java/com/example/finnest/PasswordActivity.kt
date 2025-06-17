package com.example.finnest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AppCompatActivity

class PasswordActivity : AppCompatActivity() {

    private lateinit var inputLayoutPassword: TextInputLayout
    private lateinit var inputLayoutConfirmPassword: TextInputLayout
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        inputLayoutPassword = findViewById(R.id.inputLayoutPassword)
        inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        continueButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener {
            validatePasswordFields()
        }

        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.length >= 8) {
                    inputLayoutPassword.error = null
                }
                validateMatchIfBothNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editTextConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val confirm = s.toString()
                if (confirm.isNotEmpty()) {
                    inputLayoutConfirmPassword.error = null
                }
                validateMatchIfBothNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validatePasswordFields() {
        val password = editTextPassword.text?.toString()?.trim() ?: ""
        val confirmPassword = editTextConfirmPassword.text?.toString()?.trim() ?: ""
        var isValid = true

        // Validate password
        if (password.isEmpty()) {
            inputLayoutPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            inputLayoutPassword.error = "Password must be at least 8 characters"
            isValid = false
        } else {
            inputLayoutPassword.error = null
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            inputLayoutConfirmPassword.error = "Confirm Password is required"
            isValid = false
        } else if (password != confirmPassword) {
            inputLayoutConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            inputLayoutConfirmPassword.error = null
        }

        // Proceed if valid
        if (isValid) {
            // TODO: handle success (e.g., navigate, save password, etc.)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateMatchIfBothNotEmpty() {
        val password = editTextPassword.text?.toString()?.trim() ?: ""
        val confirmPassword = editTextConfirmPassword.text?.toString()?.trim() ?: ""

        if (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (password == confirmPassword) {
                inputLayoutConfirmPassword.error = null
            } else {
                inputLayoutConfirmPassword.error = "Passwords do not match"
            }
        }
    }
}
