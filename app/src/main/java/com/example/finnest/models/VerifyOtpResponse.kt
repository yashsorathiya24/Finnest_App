package com.example.finnest.models

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class VerifyOtpResponse(
    val message: String? = null,
    val success: Boolean? = null,
    val token: String? = null
) {
    // Helper function to determine success
    fun isSuccessful(): Boolean {
        return when {
            success != null -> success // If success flag exists, use it
            message != null -> message.contains("success", ignoreCase = true)
            else -> false
        }
    }
}