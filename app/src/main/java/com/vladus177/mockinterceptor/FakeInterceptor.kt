package com.vladus177.mockinterceptor

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.SystemClock
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.Response.Builder
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.*
import java.net.URI
import java.util.*


class FakeInterceptor constructor(private val context: Context) : Interceptor {

    private var mContentType = "application/json"


    @SuppressLint("DefaultLocale")
    override fun intercept(chain: Chain): Response {
        val listSuggestionFileName: List<String> = ArrayList()
        val method = chain.request().method.toLowerCase()
        var response: Response? = null
        val uri = chain.request().url.toUri()
        val defaultFileName: String? = getFileName(chain)

        Log.d(TAG, "--> Request url: [" + method.toUpperCase() + "]" + uri.toString())
        SystemClock.sleep(LOAD_TIME)
        if (defaultFileName != null) {
            val fileName: String = getFilePath(uri, defaultFileName)

            Log.d(ContentValues.TAG, "Read data from file: $fileName")

            try {
                val inputStream: InputStream = context.assets.open(fileName)
                val r = BufferedReader(InputStreamReader(inputStream) as Reader)
                val responseStringBuilder = StringBuilder()
                var line: String?
                while (r.readLine().also { line = it } != null) {
                    responseStringBuilder.append(line).append('\n')
                }
                Log.d(ContentValues.TAG, "Response: $responseStringBuilder")

                val builder = Builder()
                builder.request(chain.request())
                builder.protocol(Protocol.HTTP_1_0)
                builder.addHeader("content-type", mContentType)
                builder.body(
                    responseStringBuilder.toString().toByteArray()
                        .toResponseBody(mContentType.toMediaTypeOrNull())
                )
                builder.code(200)
                builder.message(responseStringBuilder.toString())
                response = builder.build()
            } catch (e: IOException) {
                Log.e(TAG, e.message, e)
            }
        } else {
            for (file in listSuggestionFileName) {
                Log.e(ContentValues.TAG, "File not exist: " + getFilePath(uri, file))
            }
            response = chain.proceed(chain.request())
        }

        Log.d(TAG, "<-- END [" + method.toUpperCase() + "]" + uri.toString())
        return response!!
    }

    @SuppressLint("DefaultLocale")
    private fun getFileName(chain: Chain): String? {
        val fileName =
            chain.request().url.pathSegments[chain.request().url.pathSegments.size - 1]
        return if (fileName.isEmpty()) "index$FILE_EXTENSION" else fileName + "_" + chain.request().method.toLowerCase() + FILE_EXTENSION
    }

    private fun getFilePath(uri: URI, fileName: String): String {
        val path: String = if (uri.path.lastIndexOf('/') != uri.path.length - 1) {
            uri.path.substring(0, uri.path.lastIndexOf('/') + 1)
        } else {
            uri.path
        }
        return uri.host.toString() + path + fileName
    }

    companion object {
        private val TAG = FakeInterceptor::class.java.simpleName
        private const val FILE_EXTENSION = ".json"
        private const val LOAD_TIME = 1500.toLong()
    }

}