package com.smart2pay.example.requests.requests

//import nl.zooma.rav.mto.extensions.defaultRealm
//import nl.zooma.rav.mto.extensions.getModifiedClose
//import nl.zooma.rav.mto.extensions.transactionClose
//import nl.zooma.rav.mto.models.realm.Modified
//import nl.zooma.rav.mto.requests.RequestManager
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//

//abstract class NotModifiedRequest<T>(private val modifiedKey: String, requestManager: RequestManager) : UnauthorizedRequest<T>(requestManager) {
//    protected val modifiedSince: String?
//        get() = defaultRealm().getModifiedClose(modifiedKey)
//
//    override fun validateSuccess(call: Call<T>, response: Response<T>): Boolean {
//        return response.isSuccessful || response.code() == 304
//    }
//
//    override fun handleSuccess(call: Call<T>, response: Response<T>) {
//        val lastModified = response.headers().get("Last-Modified")
//        if(lastModified != null) {
//            defaultRealm().transactionClose {
//                it.insertOrUpdate(Modified(modifiedKey, lastModified))
//            }
//        }
//    }
//}