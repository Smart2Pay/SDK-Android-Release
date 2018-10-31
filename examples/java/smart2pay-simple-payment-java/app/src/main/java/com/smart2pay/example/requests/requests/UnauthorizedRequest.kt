package com.smart2pay.example.requests.requests

import com.smart2pay.example.requests.RequestManager
import retrofit2.Call
import retrofit2.Response

abstract class UnauthorizedRequest<T>(requestManager: RequestManager) : BaseRequest<T>(requestManager) {

    override fun handleUnsuccessful(call: Call<T>, response: Response<T>): Boolean {
        if(response.code() == 401) {
            requestManager.unauthorizedHandler?.unauthorizedRequest()
            return true
        }
        return super.handleUnsuccessful(call, response)
    }
}