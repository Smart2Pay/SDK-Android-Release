package com.smart2pay.example.requests.requests

import com.smart2pay.example.requests.RequestManager
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class AuthorizationApiKeyRequest(requestManager: RequestManager) : UnauthorizedRequest<JSONObject>(requestManager) {
    var callback: Callback? = null

    interface Callback {
        fun onSuccess(apiKey: String)
        fun onFailure()
    }

    init {
        createRequest(requestManager.api.postAuthorizationApiKey())
    }

    override fun handleSuccess(call: Call<JSONObject>, response: Response<JSONObject>) {
        try {
            val jsonObject = response.body()!!
            val apiKeyObject = jsonObject["ApiKey"] as JSONObject

            callback?.onSuccess(apiKeyObject["Value"].toString())
        } catch(e: Throwable) {
            e.printStackTrace()

            callback?.onFailure()
        }
    }

    override fun handleOnFailure(call: Call<JSONObject>, t: Throwable) {
        callback?.onFailure()
    }
}