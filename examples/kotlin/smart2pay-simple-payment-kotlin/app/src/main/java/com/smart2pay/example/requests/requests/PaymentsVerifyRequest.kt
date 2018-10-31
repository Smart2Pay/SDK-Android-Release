package com.smart2pay.example.requests.requests

import com.smart2pay.example.requests.RequestManager
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class PaymentsVerifyRequest(requestManager: RequestManager) : UnauthorizedRequest<JSONObject>(requestManager) {
    var callback: Callback? = null

    interface Callback {
        fun onSuccess()
        fun onFailure()
    }

    fun setRequestBody(paymentId: Int, body: HashMap<String, Any>) {
        createRequest(requestManager.api.postPaymentsVerification(paymentId, JSONObject(body)))
    }

    override fun handleSuccess(call: Call<JSONObject>, response: Response<JSONObject>) {
        try {
            val jsonObject = response.body()!!

            try {
                if (jsonObject["status"].toString() == "Success") {
                    callback?.onSuccess()
                } else {
                    callback?.onFailure()
                }
            } catch(e: JSONException) {
                e.printStackTrace()

                callback?.onFailure()
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