package com.smart2pay.example.requests.interceptors

import android.text.TextUtils
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.GZIPInputStream

class LoggingInterceptor : Interceptor {
    private val UTF8 = Charset.forName("UTF-8")
    private val JSON_INDENT = 3

    private val LINE_SEPARATOR = System.getProperty("line.separator")
    private val DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR

    private val OMITTED_RESPONSE = arrayOf(LINE_SEPARATOR, "Omitted response body")
    private val OMITTED_REQUEST = arrayOf(LINE_SEPARATOR, "Omitted request body")

    private val REQUEST_UP_LINE = "┌────── Request ────────────────────────────────────────────────────────────────────────"
    private val END_LINE = "└───────────────────────────────────────────────────────────────────────────────────────"
    private val RESPONSE_UP_LINE = "┌────── Response ───────────────────────────────────────────────────────────────────────"
    private val BODY_TAG = "Body:"
    private val URL_TAG = "URL: "
    private val METHOD_TAG = "Method: @"
    private val HEADERS_TAG = "Headers:"
    private val STATUS_CODE_TAG = "Status Code: "
    private val RECEIVED_TAG = "Received in: "
    private val CORNER_UP = "┌ "
    private val CORNER_BOTTOM = "└ "
    private val CENTER_LINE = "├ "
    private val DEFAULT_LINE = "│ "

    private val REQUEST_TAG = "Request"
    private val RESPONSE_TAG = "Response"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestBody = request.body()
        val rSubtype: String? = requestBody?.contentType()?.subtype()
        if (isNotFileRequest(rSubtype)) {
            printJsonRequest(request)
        } else {
            printFileRequest(request)
        }

        val st = System.nanoTime()
        val response = chain.proceed(request)
        val chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st)

        val segmentList = request.url().encodedPathSegments()
        val header = response.headers().toString()
        val code = response.code()
        val isSuccessful = response.isSuccessful
        val message = response.message()
        val responseBody = response.body()
        val contentType = responseBody!!.contentType()

        var subtype: String? = null

        if (contentType != null) {
            subtype = contentType.subtype()
        }

        if (isNotFileRequest(subtype) && HttpHeaders.hasBody(response)) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()

            if(!bodyEncoded(response.headers()) && isPlaintext(buffer)) {
                if (responseBody.contentLength() != 0L) {
                    val charset = responseBody.contentType()?.charset(UTF8) ?: UTF8
                    printJsonResponse(chainMs, isSuccessful, code, header, buffer.clone().readString(charset), segmentList, message, response.request().url().toString())
                } else {
                    printFileResponse(chainMs, isSuccessful, code, header, segmentList, message)
                }
            } else if("gzip".equals(response.header("Content-Encoding"), true)) {
                val inputstream = buffer.clone().inputStream()
                val gis = GZIPInputStream(inputstream, 32)
                val string = StringBuilder()
                val data = ByteArray(32)
                var bytesRead: Int = gis.read(data)
                while (bytesRead != -1) {
                    string.append(String(data, 0, bytesRead))
                    bytesRead = gis.read(data)
                }
                gis.close()
                inputstream.close()

                printJsonResponse(chainMs, isSuccessful, code, header, string.toString(), segmentList, message, response.request().url().toString())
            } else {
                printFileResponse(chainMs, isSuccessful, code, header, segmentList, message)
            }
        } else {
            printFileResponse(chainMs, isSuccessful, code, header, segmentList, message)
        }

        return response
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size() < 64) buffer.size() else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    private fun isNotFileRequest(subtype: String?): Boolean {
        return subtype != null && (subtype.contains("json")
            || subtype.contains("xml")
            || subtype.contains("plain")
            || subtype.contains("html"))
    }

    private fun printJsonRequest(request: Request) {
        val requestBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyToString(request)

        val tag = REQUEST_TAG

        log(tag, REQUEST_UP_LINE)

        logLines(tag, arrayOf(URL_TAG + request.url()), false)
        logLines(tag, getRequest(request), true)
        logLines(tag, requestBody.split(LINE_SEPARATOR).toTypedArray(), true)

        log(tag, END_LINE)
    }

    private fun printJsonResponse(chainMs: Long, isSuccessful: Boolean, code: Int, headers: String, bodyString: String, segments: List<String>, message: String, responseUrl: String) {
        val responseBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + getJsonString(bodyString)
        val tag = RESPONSE_TAG
        val urlLine = arrayOf(URL_TAG + responseUrl, "\n")
        val response = getResponse(headers, chainMs, code, isSuccessful, segments, message)

        log(tag, RESPONSE_UP_LINE)

        logLines(tag, urlLine, true)
        logLines(tag, response, true)

        logLines(tag, responseBody.split(LINE_SEPARATOR).toTypedArray(), true)

        log(tag, END_LINE)
    }

    private fun printFileRequest(request: Request) {
        val tag = REQUEST_TAG
        log(tag, REQUEST_UP_LINE)
        logLines(tag, arrayOf(URL_TAG + request.url()), false)
        logLines(tag, getRequest(request), true)
        logLines(tag, OMITTED_REQUEST, true)
        log(tag, END_LINE)
    }

    private fun printFileResponse(chainMs: Long, isSuccessful: Boolean, code: Int, headers: String, segments: List<String>, message: String) {
        val tag = RESPONSE_TAG
        log(tag, RESPONSE_UP_LINE)
        logLines(tag, getResponse(headers, chainMs, code, isSuccessful, segments, message), true)
        logLines(tag, OMITTED_RESPONSE, true)
        log(tag, END_LINE)
    }

    private fun getRequest(request: Request): Array<String> {
        val header = request.headers().toString()
        val log = METHOD_TAG + request.method() + DOUBLE_SEPARATOR +
            if (TextUtils.isEmpty(header)) "" else HEADERS_TAG + LINE_SEPARATOR + dotHeaders(header)
        return log.split(LINE_SEPARATOR).toTypedArray()
    }

    private fun getResponse(header: String, tookMs: Long, code: Int, isSuccessful: Boolean,
                            segments: List<String>, message: String): Array<String> {
        val segmentString = slashSegments(segments)
        val log = (((if (!TextUtils.isEmpty(segmentString)) segmentString + " - " else "") + "is success : "
            + isSuccessful + " - " + RECEIVED_TAG + tookMs + "ms" + DOUBLE_SEPARATOR + STATUS_CODE_TAG +
            code + " / " + message + DOUBLE_SEPARATOR + (if (TextUtils.isEmpty(header))
            ""
        else
            HEADERS_TAG + LINE_SEPARATOR +
                dotHeaders(header)
            )))
        return log.split((LINE_SEPARATOR).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    private fun slashSegments(segments: List<String>): String {
        val segmentString = StringBuilder()
        for (segment in segments) {
            segmentString.append("/").append(segment)
        }
        return segmentString.toString()
    }

    private fun dotHeaders(header: String): String {
        val headers = header.split(LINE_SEPARATOR).toTypedArray()
        val builder = StringBuilder()
        var tag = "─ "
        if (headers.size > 1) {
            for (i in headers.indices) {
                tag = when (i) {
                    0 -> CORNER_UP
                    headers.size - 1 -> CORNER_BOTTOM
                    else -> CENTER_LINE
                }
                builder.append(tag).append(headers[i]).append("\n")
            }
        } else {
            for (item in headers) {
                builder.append(tag).append(item).append("\n")
            }
        }
        return builder.toString()
    }

    private fun logLines(tag: String, lines: Array<String>, withLineSize: Boolean) {
        for (line in lines) {
            val lineLength = line.length
            val MAX_LONG_SIZE = if (withLineSize) 110 else lineLength
            for (i in 0..lineLength / MAX_LONG_SIZE) {
                val start = i * MAX_LONG_SIZE
                var end = (i + 1) * MAX_LONG_SIZE
                end = if (end > line.length) line.length else end
                log(tag, DEFAULT_LINE + line.substring(start, end))
            }
        }
    }

    private fun bodyToString(request: Request): String {
        try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            if (copy.body() == null)
                return ""
            copy.body()!!.writeTo(buffer)
            return getJsonString(buffer.readUtf8())
        } catch (e: IOException) {
            return "{\"err\": \"" + e.message + "\"}"
        }

    }

    private fun getJsonString(msg: String): String {
        return try {
            when {
                msg.startsWith("{") -> {
                    val jsonObject = JSONObject(msg)
                    jsonObject.toString(JSON_INDENT)
                }
                msg.startsWith("[") -> {
                    val jsonArray = JSONArray(msg)
                    jsonArray.toString(JSON_INDENT)
                }
                else -> msg
            }
        } catch (e: JSONException) {
            msg
        }
    }

    private fun log(tag: String, message: String) {
        Logger.getLogger(tag).log(Level.INFO, message)
    }
}