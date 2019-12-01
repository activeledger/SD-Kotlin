/*
 *  Copyright (c) 2016 HERE Europe B.V.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.activeledgersdk.event

import com.here.oksse.ServerSentEvent

import okhttp3.Request
import okhttp3.Response

interface ServerEventListener : ServerSentEvent.Listener {

    /**
     * Notify when the connection is open an established. From this point on, new message could be received.
     *
     * @param sse      the instance of [ServerSentEvent]
     * @param response the response from the server after establishing the connection
     */
    override fun onOpen(sse: ServerSentEvent, response: Response)

    /**
     * Called every time a message is received.
     *
     * @param sse     the instance of [ServerSentEvent]
     * @param id      id sent by the server to identify the message
     * @param event   event type of this message
     * @param message message payload
     */
    override fun onMessage(sse: ServerSentEvent, id: String, event: String, message: String)

    /**
     * Called every time a comment is received.
     *
     * @param sse     the instance of [ServerSentEvent]
     * @param comment the content of the comment
     */
    override fun onComment(sse: ServerSentEvent, comment: String)

    /**
     * The stream can define the retry time sending a message with "retry: milliseconds"
     * If this event is received this method will be called with the sent value
     *
     * @param sse          the instance of [ServerSentEvent]
     * @param milliseconds new retry time in milliseconds
     * @return true if this retry time should be used, false otherwise
     */
    override fun onRetryTime(sse: ServerSentEvent, milliseconds: Long): Boolean

    /**
     * Notify when the connection failed either because it could not be establish or the connection broke.
     * The Server Sent Event protocol defines that should be able to reestablish a connection using retry mechanism.
     * In some cases depending on the error the connection should not be retry.
     *
     *
     * Implement this method to define this behavior.
     *
     * @param sse       the instance of [ServerSentEvent]
     * @param throwable the instance of the error that caused the failure
     * @param response  the response of the server that caused the failure, it might be null.
     * @return true if the connection should be retried after the defined retry time, false to avoid the retry, this will close the SSE.
     */
    override fun onRetryError(sse: ServerSentEvent, throwable: Throwable, response: Response): Boolean

    /**
     * Notify that the connection was closed.
     *
     * @param sse the instance of [ServerSentEvent]
     */
    override fun onClosed(sse: ServerSentEvent)

    /**
     * Notifies client before retrying to connect. At this point listener may decide to return
     * `originalRequest` to repeat last request or another one to alternate
     *
     * @param sse             the instance of [ServerSentEvent]
     * @param originalRequest the request to be retried
     * @return call to be executed or `null` to cancel retry and close the SSE channel
     */
    override fun onPreRetry(sse: ServerSentEvent, originalRequest: Request): Request

}
