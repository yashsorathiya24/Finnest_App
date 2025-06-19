package com.example.finnest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.finnest.api.RetrofitClient
import com.example.finnest.models.SetPasswordRequest
import com.example.finnest.models.SetPasswordResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordActivity : AppCompatActivity() {

    private lateinit var inputLayoutPassword: TextInputLayout
    private lateinit var inputLayoutConfirmPassword: TextInputLayout
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var continueButton: Button
    private lateinit var progressBar: ProgressBar

    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        initViews()
        setupTextWatchers()
    }

    private fun initViews() {
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword)
        inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        continueButton = findViewById(R.id.continueButton)
        progressBar = findViewById(R.id.progressBar)

        email = intent.getStringExtra("EMAIL") ?: run {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        continueButton.setOnClickListener { validatePasswordFields() }
    }

    private fun setupTextWatchers() {
        editTextPassword.addTextChangedListener(createPasswordTextWatcher())
        editTextConfirmPassword.addTextChangedListener(createConfirmPasswordTextWatcher())
    }

    private fun createPasswordTextWatcher() = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val password = s.toString()
            if (password.length >= 8) inputLayoutPassword.error = null
            validateMatchIfBothNotEmpty()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun createConfirmPasswordTextWatcher() = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val confirm = s.toString()
            if (confirm.isNotEmpty()) inputLayoutConfirmPassword.error = null
            validateMatchIfBothNotEmpty()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun validatePasswordFields() {
        val password = editTextPassword.text?.toString()?.trim() ?: ""
        val confirmPassword = editTextConfirmPassword.text?.toString()?.trim() ?: ""
        var isValid = true

        if (password.isEmpty()) {
            inputLayoutPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            inputLayoutPassword.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            inputLayoutConfirmPassword.error = "Confirm Password is required"
            isValid = false
        } else if (password != confirmPassword) {
            inputLayoutConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        if (isValid) callSetPasswordApi(email, password)
    }

    private fun validateMatchIfBothNotEmpty() {
        val password = editTextPassword.text?.toString()?.trim() ?: ""
        val confirmPassword = editTextConfirmPassword.text?.toString()?.trim() ?: ""

        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            inputLayoutConfirmPassword.error = "Passwords do not match"
        }
    }

    private fun callSetPasswordApi(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        continueButton.isEnabled = false

        RetrofitClient.instance.setPassword(SetPasswordRequest(email, password))
            .enqueue(object : Callback<SetPasswordResponse> {
                override fun onResponse(call: Call<SetPasswordResponse>, response: Response<SetPasswordResponse>) {
                    progressBar.visibility = View.GONE
                    continueButton.isEnabled = true
                    handleApiResponse(response)
                }

                override fun onFailure(call: Call<SetPasswordResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    continueButton.isEnabled = true
                    showErrorDialog("Network Error", t.message ?: "Network request failed")
                }
            })
    }

    private fun handleApiResponse(response: Response<SetPasswordResponse>) {
        when {
            response.isSuccessful && response.body() != null -> {
                val res = response.body()!!
                if (res.isSuccessful()) {
                    showSuccessDialog(res.getSuccessMessage()) // ✅ success message with proper title
                } else {
                    showErrorDialog("Error", res.getErrorMessage())
                }
            }
            response.errorBody() != null -> {
                parseErrorResponse(response.errorBody()!!)
            }
            else -> showErrorDialog("Error", "Unexpected response format")
        }
    }

    private fun parseErrorResponse(errorBody: ResponseBody) {
        try {
            val errorJson = errorBody.string()
            val jsonObj = JSONObject(errorJson)
            val errorMessage = when {
                jsonObj.has("error") -> jsonObj.getString("error")
                jsonObj.has("message") -> jsonObj.getString("message")
                else -> "Unknown server error"
            }
            showErrorDialog("Error", errorMessage)
        } catch (e: Exception) {
            showErrorDialog("Error", "Failed to parse error response")
        }
    }

    // ✅ Fix: Title is "Success", and on OK move to LoginActivity
    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                navigateToLogin() // ✅ Navigate to login screen
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this@PasswordActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
