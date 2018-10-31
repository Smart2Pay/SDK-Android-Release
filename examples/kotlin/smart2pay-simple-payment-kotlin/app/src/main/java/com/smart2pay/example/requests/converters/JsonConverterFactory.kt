package com.smart2pay.example.requests.converters

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class JsonConverterFactory : Converter.Factory() {
    private val MEDIA_TYPE = MediaType.parse("application/json")

    override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
        if (JSONObject::class.java == type) {
            return Converter<ResponseBody, JSONObject> { value ->
                try {
                    return@Converter JSONObject(value.string())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                null
            }
        }
        if (JSONArray::class.java == type) {
            return Converter<ResponseBody, JSONArray> { value ->
                try {
                    return@Converter JSONArray(value.string())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                null
            }
        }
        return null
    }

    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody>? {
        if (JSONObject::class.java == type) {
            return Converter<JSONObject, RequestBody> { value -> RequestBody.create(MEDIA_TYPE, value.toString()) }
        }
        if (JSONArray::class.java == type) {
            return Converter<JSONArray, RequestBody> { value -> RequestBody.create(MEDIA_TYPE, value.toString()) }
        }
        return null
    }
}
