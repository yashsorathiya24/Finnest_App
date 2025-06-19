package com.example.finnest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.finnest.api.RetrofitClient
import com.example.finnest.models.OtpRequest
import com.example.finnest.models.OtpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var signUpButton: Button
    private lateinit var signInText: TextView
    private lateinit var progressBar: ProgressBar

    private var formSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize views
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        signUpButton = findViewById(R.id.signUpButton)
        signInText = findViewById(R.id.signUpTextView)
        progressBar = findViewById(R.id.progressBar)

        // Initially hide the progress bar
        progressBar.visibility = View.GONE

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
                // Show progress bar and disable button
                progressBar.visibility = View.VISIBLE
                signUpButton.isEnabled = false

                requestOtp(email)
            }
        }

        // Navigate to sign-in
        signInText.setOnClickListener {
            finish()
        }
    }

    private fun requestOtp(email: String) {
        val otpRequest = OtpRequest(email = email)

        RetrofitClient.instance.requestOtp(otpRequest).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                // Hide progress bar and re-enable button
                progressBar.visibility = View.GONE
                signUpButton.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val otpResponse = response.body()
                    Toast.makeText(
                        this@SignUpActivity,
                        otpResponse?.message ?: "OTP sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to verification activity with email
                    val intent = Intent(this@SignUpActivity, VerificationActivity::class.java).apply {
                        putExtra("EMAIL", email)
                    }
                    startActivity(intent)
                } else {
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        JSONObject(errorBody ?: "").getString("message")
                    } catch (e: Exception) {
                        "Failed to send OTP. Please try again."
                    }

                    showErrorDialog("OTP Request Failed", errorMessage)
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                // Hide progress bar and re-enable button
                progressBar.visibility = View.GONE
                signUpButton.isEnabled = true

                Toast.makeText(
                    this@SignUpActivity,
                    t.message ?: "Network error occurred",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showErrorDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
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