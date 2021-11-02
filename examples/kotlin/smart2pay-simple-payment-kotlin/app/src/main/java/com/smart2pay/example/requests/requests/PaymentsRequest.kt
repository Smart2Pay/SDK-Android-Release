package com.smart2pay.example.requests.requests

import com.smart2pay.example.requests.RequestManager
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class PaymentsRequest(requestManager: RequestManager) : UnauthorizedRequest<JSONObject>(requestManager) {
    var callback: Callback? = null

    interface Callback {
        fun onSuccess(paymentId: Int, paymentsResponse: String)
        fun onFailure()
    }

    fun setRequestBody(parameters: HashMap<String, Any>) {
        createRequest(requestManager.api.postPayments(JSONObject(parameters)))
    }

    override fun handleSuccess(call: Call<JSONObject>, response: Response<JSONObject>) {
        try {
            val jsonObject = response.body()!!

            try {
                callback?.onSuccess(jsonObject["ID"] as Int, jsonObject["Instructions"].toString())
            } catch(e: JSONException) {
                e.printStackTrace()
            }
        } catch(e: JSONException) {
            e.printStackTrace()

            callback?.onFailure()
        }
    }

    override fun handleOnFailure(call: Call<JSONObject>, t: Throwable) {
        callback?.onFailure()
    }
}