package com.example.finnest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finnest.api.RetrofitClient
import com.example.finnest.models.LoginRequest
import com.example.finnest.models.LoginResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: TextView
    private lateinit var forgotPasswordButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)

        // Sign up navigation
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Email field validation
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    emailInputLayout.error = null
                } else if (!isValidEmail(s.toString())) {
                    emailInputLayout.error = "Enter a valid email address"
                } else {
                    emailInputLayout.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Password field validation
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    passwordInputLayout.error = null
                } else if (s.length < 8) {
                    passwordInputLayout.error = "Password must be at least 8 characters"
                } else {
                    passwordInputLayout.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Login button
        loginButton.setOnClickListener {
            validateInputs()
        }
    }

    private fun validateInputs() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        var isValid = true

        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailInputLayout.error = "Enter a valid email address"
            isValid = false
        } else {
            emailInputLayout.error = null
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 8) {
            passwordInputLayout.error = "Password must be at least 8 characters"
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        if (isValid) {
            // TODO: Proceed with login
            // startActivity(Intent(this, DashboardActivity::class.java))
            val email = emailEditText.text.toString().trim();
            val password = passwordEditText.text.toString().trim();

            val loginRequest = LoginRequest(email, password)

            RetrofitClient.instance.userLogin(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()
                        Toast.makeText(applicationContext, loginResponse?.message ?: "Login successful", Toast.LENGTH_LONG).show()
                        // ✅ Go to DashboardActivity
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish() // Optional
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            val jsonObject = JSONObject(errorBody ?: "")
                            jsonObject.getString("error")  // or jsonObject.getString("message") depending on your API
                        } catch (e: Exception) {
                            "Invalid credentials. Please try again."
                        }

                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("Login Failed")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}