package com.smart2pay.example.requests

import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface Api {
    @POST("/api/payments")
    fun postPayments(@Body parameters: JSONObject): Call<JSONObject>

    @POST("/api/payments/{payment_id}/verify")
    fun postPaymentsVerification(@Path("payment_id") paymentId: Int, @Body parameters: JSONObject): Call<JSONObject>

    @POST("/api/authorization/apikey")
    fun postAuthorizationApiKey(): Call<JSONObject>
}
