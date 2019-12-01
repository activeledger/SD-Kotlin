/*
 * MIT License (MIT)
 * Copyright (c) 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.activeledgersdk.event

import android.arch.lifecycle.MutableLiveData
import android.support.annotation.WorkerThread
import android.util.Log

import com.example.activeledgersdk.http.HttpClient
import com.here.oksse.ServerSentEvent

import java.util.ArrayList

import okhttp3.Request
import okhttp3.Response

class SSEUtil {
    var eventLiveData = MutableLiveData<Event>()

    fun createURL(protocol: String, ip: String, port: String, api: String): String {

        return "$protocol://$ip:$port$api"
    }

    fun subscribeToEvent(protocol: String, ip: String, port: String, api: String, listener: ServerEventListener?): ServerSentEvent {
        var listener = listener
        if (listener == null) {
            listener = createLister()
        }
        val url = createURL(protocol, ip, port, api)
        val ssevent = HttpClient.getInstance().subscribeToEvent(url, listener)
        openEvents!!.add(ssevent)
        return ssevent
    }


    fun createLister(): ServerEventListener {

        //should be the users resp to close it
        return object : ServerEventListener {
            override fun onOpen(sse: ServerSentEvent, response: Response) {
                // When the channel is opened
                Log.d("SSE EVENT", "onOpen response$response")
            }

            override fun onMessage(sse: ServerSentEvent, id: String, event: String, message: String) {
                // When a message is received
                Log.d("SSE EVENT", "onMessage message$message")
                val incomming_event = Event(sse, id, event, message)
                eventLiveData.postValue(incomming_event)

            }

            @WorkerThread
            override fun onComment(sse: ServerSentEvent, comment: String) {
                // When a comment is received
                Log.d("SSE EVENT", "onComment comment$comment")

            }

            @WorkerThread
            override fun onRetryTime(sse: ServerSentEvent, milliseconds: Long): Boolean {
                Log.d("SSE EVENT", "onRetryTime milliseconds$milliseconds")
                return true

            }

            @WorkerThread
            override fun onRetryError(sse: ServerSentEvent, throwable: Throwable, response: Response): Boolean {
                Log.d("SSE EVENT", "onRetryError response$response")

                return true // True to retry, false otherwise
            }

            @WorkerThread
            override fun onClosed(sse: ServerSentEvent) {
                Log.d("SSE EVENT", "onClosed ")
                // Channel closed
            }

            override fun onPreRetry(sse: ServerSentEvent, originalRequest: Request): Request {
                Log.d("SSE EVENT", "onPreRetry originalRequest$originalRequest")
                return originalRequest
            }
        }
    }


    fun closeEvents() {
        for (i in openEvents!!.indices) {
            openEvents!![i].close()
        }
    }

    companion object {

        internal var openEvents: MutableList<ServerSentEvent>? = null
        private var instance: SSEUtil? = null

        fun getInstance(): SSEUtil {
            if (instance == null) {
                instance = SSEUtil()
                openEvents = ArrayList()
            }
            return instance as SSEUtil
        }
    }

}
