package com.example.finnest.models

data class User(
    val email: String,
    val id: Int
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: Map<String, List<String>>? = null
)