package com.smart2pay.example.requests.requests

import com.smart2pay.example.requests.RequestManager
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class PayAfter3dRequest(requestManager: RequestManager) : UnauthorizedRequest<JSONObject>(requestManager) {
    var callback: Callback? = null

    interface Callback {
        fun onSuccess(json: JSONObject)
        fun onFailure(e: Throwable)
    }

    fun setRequestBody(parameters: Map<String, Any>) {
        createRequest(requestManager.api.payAfter3d(JSONObject(parameters)))
    }

    override fun handleSuccess(call: Call<JSONObject>, response: Response<JSONObject>) {
        try {
            callback?.onSuccess(response.body()!!)
        } catch(e: Throwable) {
            e.printStackTrace()
            callback?.onFailure(e)
        }
    }

    override fun handleOnFailure(call: Call<JSONObject>, t: Throwable) {
        callback?.onFailure(t)
    }
}