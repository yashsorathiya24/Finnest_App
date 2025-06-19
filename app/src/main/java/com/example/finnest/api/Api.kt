package com.example.finnest.api

import com.example.finnest.models.LoginRequest
import com.example.finnest.models.LoginResponse
import com.example.finnest.models.OtpRequest
import com.example.finnest.models.OtpResponse
import com.example.finnest.models.SetPasswordRequest
import com.example.finnest.models.SetPasswordResponse
import com.example.finnest.models.VerifyOtpRequest
import com.example.finnest.models.VerifyOtpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {
    @POST("login")
    fun userLogin(@Body request: LoginRequest): Call<LoginResponse>

    @POST("request-otp")
    fun requestOtp(@Body request: OtpRequest): Call<OtpResponse>

    @POST("verify-otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>

    @POST("set-password")
    fun setPassword(@Body request: SetPasswordRequest): Call<SetPasswordResponse>
}
