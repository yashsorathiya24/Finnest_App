package com.example.finnest

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finnest.api.RetrofitClient
import com.example.finnest.models.OtpRequest
import com.example.finnest.models.OtpResponse
import com.example.finnest.models.VerifyOtpRequest
import com.example.finnest.models.VerifyOtpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificationActivity : AppCompatActivity() {

    private lateinit var otp1: TextInputEditText
    private lateinit var otp2: TextInputEditText
    private lateinit var otp3: TextInputEditText
    private lateinit var otp4: TextInputEditText
    private lateinit var otp5: TextInputEditText
    private lateinit var otp6: TextInputEditText

    private lateinit var otpLayout1: TextInputLayout
    private lateinit var otpLayout2: TextInputLayout
    private lateinit var otpLayout3: TextInputLayout
    private lateinit var otpLayout4: TextInputLayout
    private lateinit var otpLayout5: TextInputLayout
    private lateinit var otpLayout6: TextInputLayout

    private lateinit var otpLayouts: List<TextInputLayout>
    private lateinit var otpFields: List<TextInputEditText>
    private lateinit var verifyButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resendTextView: TextView

    private lateinit var countDownTimer: CountDownTimer
    private val resendWaitTimeMillis = 30000L // 30 seconds
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_acitvity)

        // Initialize views
        initViews()

        // Get email from intent
        email = intent.getStringExtra("EMAIL") ?: run {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup OTP fields behavior
        setupOtpInputs()
        setupFocusListeners()
        startResendCountdown()

        // Set click listeners
        resendTextView.setOnClickListener {
            if (resendTextView.isEnabled) {
                clearOtpFields()
                requestNewOtp()
            }
        }

        verifyButton.setOnClickListener {
            verifyOtp()
        }
    }

    private fun initViews() {
        otp1 = findViewById(R.id.otp1)
        otp2 = findViewById(R.id.otp2)
        otp3 = findViewById(R.id.otp3)
        otp4 = findViewById(R.id.otp4)
        otp5 = findViewById(R.id.otp5)
        otp6 = findViewById(R.id.otp6)

        otpLayout1 = findViewById(R.id.otpLayout1)
        otpLayout2 = findViewById(R.id.otpLayout2)
        otpLayout3 = findViewById(R.id.otpLayout3)
        otpLayout4 = findViewById(R.id.otpLayout4)
        otpLayout5 = findViewById(R.id.otpLayout5)
        otpLayout6 = findViewById(R.id.otpLayout6)

        verifyButton = findViewById(R.id.verifyButton)
        progressBar = findViewById(R.id.progressBar)
        resendTextView = findViewById(R.id.resendTextView)

        otpLayouts = listOf(otpLayout1, otpLayout2, otpLayout3, otpLayout4, otpLayout5, otpLayout6)
        otpFields = listOf(otp1, otp2, otp3, otp4, otp5, otp6)
    }

    private fun setupOtpInputs() {
        otpFields.forEachIndexed { index, currentField ->
            val nextField = otpFields.getOrNull(index + 1)
            val prevField = otpFields.getOrNull(index - 1)

            currentField.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        otpLayouts[index].setBoxStrokeColor(ContextCompat.getColor(this@VerificationActivity, R.color.paris_green))
                        nextField?.requestFocus() ?: run {
                            hideKeyboard()
                            verifyButton.performClick() // Auto-submit when last digit entered
                        }
                    } else if (s.isNullOrEmpty()) {
                        otpLayouts[index].setBoxStrokeColor(ContextCompat.getColor(this@VerificationActivity, R.color.gray_border))
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            currentField.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (currentField.text.isNullOrEmpty()) {
                        prevField?.requestFocus()
                        prevField?.setText("")
                        true
                    } else {
                        currentField.setText("")
                        true
                    }
                } else {
                    false
                }
            }
        }
    }

    private fun setupFocusListeners() {
        otpFields.forEachIndexed { index, editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setFocusedField(index)
                } else {
                    if (editText.text.isNullOrEmpty()) {
                        otpLayouts[index].setBoxStrokeColor(ContextCompat.getColor(this, R.color.gray_border))
                    }
                }
            }
        }
    }

    private fun setFocusedField(focusedIndex: Int) {
        otpLayouts.forEachIndexed { index, layout ->
            if (index == focusedIndex) {
                layout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.paris_green))
            } else {
                layout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.gray_border))
            }
        }
    }

    private fun clearOtpFields() {
        otpFields.forEach { it.setText("") }
        otpFields[0].requestFocus()
        setFocusedField(0)
    }

    private fun startResendCountdown() {
        resendTextView.isEnabled = false
        resendTextView.setTextColor(ContextCompat.getColor(this, R.color.text_primary))

        countDownTimer = object : CountDownTimer(resendWaitTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                resendTextView.text = "Resend OTP in ${String.format("%02d", seconds)}s"
            }

            override fun onFinish() {
                resendTextView.text = "Resend OTP)"
                resendTextView.isEnabled = true
                resendTextView.setTextColor(ContextCompat.getColor(this@VerificationActivity, R.color.paris_green))
            }
        }.start()
    }

    private fun verifyOtp() {
        var allFilled = true
        val otpBuilder = StringBuilder()

        otpFields.forEachIndexed { i, field ->
            val input = field.text?.toString()?.trim()
            if (input.isNullOrEmpty()) {
                allFilled = false
                otpLayouts[i].setBoxStrokeColor(ContextCompat.getColor(this, R.color.red_error))
                otpLayouts[i].setHintTextColor(ContextCompat.getColorStateList(this, R.color.red_error))
                otpLayouts[i].boxStrokeWidth = 2
            } else {
                otpBuilder.append(input)
                otpLayouts[i].setBoxStrokeColor(ContextCompat.getColor(this, R.color.gray_border))
                otpLayouts[i].setHintTextColor(ContextCompat.getColorStateList(this, R.color.gray_border))
                otpLayouts[i].boxStrokeWidth = 1
            }
        }

        if (allFilled) {
            val otp = otpBuilder.toString()
            progressBar.visibility = View.VISIBLE
            verifyButton.isEnabled = false
            disableAllOtpFields()

            val verifyOtpRequest = VerifyOtpRequest(email = email, otp = otp)

            RetrofitClient.instance.verifyOtp(verifyOtpRequest).enqueue(object : Callback<VerifyOtpResponse> {
                override fun onResponse(call: Call<VerifyOtpResponse>, response: Response<VerifyOtpResponse>) {
                    progressBar.visibility = View.GONE
                    verifyButton.isEnabled = true
                    enableAllOtpFields()

                    if (response.isSuccessful) {
                        response.body()?.let { otpResponse ->
                            if (otpResponse.isSuccessful()) {
                                // OTP verification successful
                                navigateToPasswordActivity()
                            } else {
                                showErrorDialog("Verification Failed", otpResponse.message ?: "Invalid OTP")
                                clearOtpFields()
                            }
                        } ?: run {
                            showErrorDialog("Verification Failed", "Empty response from server")
                            clearOtpFields()
                        }
                    } else {
                        val errorMessage = try {
                            response.errorBody()?.string()?.let {
                                JSONObject(it).getString("message")
                            } ?: "Verification failed"
                        } catch (e: Exception) {
                            "Failed to verify OTP. Please try again."
                        }
                        showErrorDialog("Verification Failed", errorMessage)
                        clearOtpFields()
                    }
                }

                override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    verifyButton.isEnabled = true
                    enableAllOtpFields()
                    Toast.makeText(
                        this@VerificationActivity,
                        t.message ?: "Network error occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToPasswordActivity() {
        runOnUiThread {
            Toast.makeText(
                this@VerificationActivity,
                "OTP verified successfully",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this@VerificationActivity, PasswordActivity::class.java).apply {
                putExtra("EMAIL", email)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun disableAllOtpFields() {
        otpFields.forEach { it.isEnabled = false }
    }

    private fun enableAllOtpFields() {
        otpFields.forEach { it.isEnabled = true }
    }

    private fun requestNewOtp() {
        progressBar.visibility = View.VISIBLE
        resendTextView.isEnabled = false

        val otpRequest = OtpRequest(email = email)

        RetrofitClient.instance.requestOtp(otpRequest).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        this@VerificationActivity,
                        response.body()?.message ?: "New OTP sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    startResendCountdown()
                } else {
                    resendTextView.isEnabled = true
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        JSONObject(errorBody ?: "").getString("message")
                    } catch (e: Exception) {
                        "Failed to resend OTP. Please try again."
                    }
                    showErrorDialog("OTP Request Failed", errorMessage)
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                resendTextView.isEnabled = true
                Toast.makeText(
                    this@VerificationActivity,
                    t.message ?: "Network error occurred",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { view ->
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun showErrorDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}