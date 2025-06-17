package com.example.finnest

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.collections.forEach

class VerificationAcitvity : AppCompatActivity() {

    private lateinit var otp1: TextInputEditText
    private lateinit var otp2: TextInputEditText
    private lateinit var otp3: TextInputEditText
    private lateinit var otp4: TextInputEditText

    private lateinit var otpLayout1: TextInputLayout
    private lateinit var otpLayout2: TextInputLayout
    private lateinit var otpLayout3: TextInputLayout
    private lateinit var otpLayout4: TextInputLayout

    private lateinit var otpLayouts: List<TextInputLayout>
    private lateinit var otpFields: List<TextInputEditText>
    private lateinit var verifyButton: Button

    private lateinit var resendTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private val resendWaitTimeMillis = 30000L // 30 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_acitvity)

        // Init views
        resendTextView = findViewById(R.id.resendTextView)
        verifyButton = findViewById(R.id.verifyButton)

        otpLayout1 = findViewById(R.id.otpLayout1)
        otpLayout2 = findViewById(R.id.otpLayout2)
        otpLayout3 = findViewById(R.id.otpLayout3)
        otpLayout4 = findViewById(R.id.otpLayout4)

        otp1 = findViewById(R.id.otp1)
        otp2 = findViewById(R.id.otp2)
        otp3 = findViewById(R.id.otp3)
        otp4 = findViewById(R.id.otp4)

        otpLayouts = listOf(otpLayout1, otpLayout2, otpLayout3, otpLayout4)
        otpFields = listOf(otp1, otp2, otp3, otp4)

        setupOtpInputs()
        setupFocusListeners()
        startResendCountdown()

        resendTextView.setOnClickListener {
            if (resendTextView.isEnabled) {
                clearOtpFields()
                startResendCountdown()
                // TODO: trigger resend OTP API here
            }
        }

        verifyButton.setOnClickListener {
            var allFilled = true

            otpFields.forEachIndexed { i, field ->
                val input = field.text?.toString()?.trim()
                if (input.isNullOrEmpty()) {
                    allFilled = false
                    otpLayouts[i].setBoxStrokeColor(ContextCompat.getColor(this, R.color.red_error))
                    otpLayouts[i].setHintTextColor(ContextCompat.getColorStateList(this, R.color.red_error))
                    otpLayouts[i].boxStrokeWidth = 2
                } else {
                    otpLayouts[i].setBoxStrokeColor(ContextCompat.getColor(this, R.color.gray_border))
                    otpLayouts[i].setHintTextColor(ContextCompat.getColorStateList(this, R.color.gray_border))
                    otpLayouts[i].boxStrokeWidth = 2
                }
            }

            if (allFilled) {
                val intent = Intent(this, PasswordActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupOtpInputs() {
        otpFields.forEachIndexed { index, currentField ->
            val nextField = otpFields.getOrNull(index + 1)
            val prevField = otpFields.getOrNull(index - 1)

            currentField.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        otpLayouts[index].setBoxStrokeColor(ContextCompat.getColor(this@VerificationAcitvity, R.color.paris_green))
                        nextField?.requestFocus() ?: hideKeyboard()
                    } else if (s.isNullOrEmpty()) {
                        otpLayouts[index].setBoxStrokeColor(ContextCompat.getColor(this@VerificationAcitvity, R.color.gray_border))
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
                resendTextView.text = "(00:${String.format("%02d", seconds)})"
            }

            override fun onFinish() {
                resendTextView.text = "Resend OTP"
                resendTextView.isEnabled = true
            }
        }.start()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { view ->
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) countDownTimer.cancel()
    }
}
