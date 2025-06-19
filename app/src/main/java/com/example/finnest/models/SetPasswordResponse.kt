package com.example.finnest.models

data class SetPasswordRequest(
    val email: String,
    val password: String
)

data class SetPasswordResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val token: String? = null,
    val error: String? = null
) {
    fun getErrorMessage(): String {
        // Return error if available; otherwise, return message or default
        return when {
            !error.isNullOrBlank() -> error
            !message.isNullOrBlank() && !(success ?: true) -> message
            else -> "Something went wrong"
        }
    }

    fun getSuccessMessage(): String {
        return message?.ifBlank { "Password set successfully" } ?: "Password set successfully"
    }

    fun isSuccessful(): Boolean {
        // Use explicit success flag if present, otherwise infer from message
        return when {
            success != null -> success
            !message.isNullOrBlank() -> message.contains("success", ignoreCase = true)
            else -> false
        }
    }
}

