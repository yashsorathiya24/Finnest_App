package com.example.finnest.models

data class OtpRequest(
    val email: String
)

data class OtpResponse(
    val message: String,
    val success: Boolean,  // Adjust based on your API response
    val otp: String? = null  // Optional, if your API returns the OTP
)