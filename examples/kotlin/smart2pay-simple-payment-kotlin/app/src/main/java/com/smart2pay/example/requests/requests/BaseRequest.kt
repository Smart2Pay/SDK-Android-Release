package com.smart2pay.example.requests.requests

import com.smart2pay.example.requests.RequestManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseRequest<T>(protected val requestManager: RequestManager) {
    private var call: Call<T>? = null
    var state = State.INITIAL
        private set

    open fun createRequest(call: Call<T>) {
        this.call = call
    }

    open fun enqueue() {
        if(call == null) {
            throw IllegalStateException("Call is not set, use createRequest to add call")
        }

        state = State.ONGOING

        call!!.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                state = State.DONE

                if(validateSuccess(call, response)) {
                    handleSuccess(call, response)
                } else {
                    if(!handleUnsuccessful(call, response)) {
                        handleOnFailure(call, NotImplementedError("handleUnsuccessful is not handled"))
                    }
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                state = State.DONE

                if(call.isCanceled) {
                    handleCancelled(call, t)
                } else {
                    handleOnFailure(call, t)
                }
            }
        })
    }

    open fun validateSuccess(call: Call<T>, response: Response<T>): Boolean {
        return response.isSuccessful && response.body() != null
    }

    abstract fun handleSuccess(call: Call<T>, response: Response<T>)

    open fun handleUnsuccessful(call: Call<T>, response: Response<T>): Boolean {
        return false
    }

    abstract fun handleOnFailure(call: Call<T>, t: Throwable)

    open fun handleCancelled(call: Call<T>, t: Throwable) {

    }

    open fun cancelRequest() {
        call?.cancel()
        call = null
    }

    enum class State {
        INITIAL,
        ONGOING,
        DONE
    }
}