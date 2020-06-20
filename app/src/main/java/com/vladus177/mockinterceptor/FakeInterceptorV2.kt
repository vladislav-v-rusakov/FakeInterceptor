package com.vladus177.mockinterceptor

import android.content.Context
import android.os.SystemClock
import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

class FakeInterceptorV2 constructor(private val context: Context, private val fileName: String) :
    Interceptor {

    private var mContentType = "application/json"


    override fun intercept(chain: Interceptor.Chain): Response {
        SystemClock.sleep(LOAD_TIME)
        val builder = Response.Builder()
        builder.request(chain.request())
        builder.protocol(Protocol.HTTP_1_0)
        builder.addHeader("content-type", mContentType)

        try {
            val jsonFile: String =
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            lateinit var jsonObject: Any
            if (jsonFile.trim().first() == '[') {
                jsonObject = JSONArray(jsonFile)
            } else if (jsonFile.trim().first() == '{') {
                jsonObject = JSONObject(jsonFile)
            }
            builder.body(
                jsonObject.toString().toByteArray().toResponseBody(mContentType.toMediaTypeOrNull())
            )
            builder.code(200)
            builder.message(jsonFile)
            Log.d(TAG, "Response: $jsonObject")
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            builder.code(404)
            builder.message("File: $fileName not found")
        }
        return builder.build()
    }

    companion object {
        private val TAG = FakeInterceptorV2::class.java.simpleName
        private const val LOAD_TIME = 1500.toLong()
    }
}