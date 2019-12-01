package com.example.activeledgersdk.utility

object ApiURL {

    private val subscribe = "/api/activity/subscribe"
    private val event = "/api/events"

    fun subscribeURL(): String {
        return subscribe
    }

    fun subscribeURL(stream: String): String {
        return "$subscribe/$stream"
    }

    fun eventsURL(): String {
        return event
    }

    fun eventsURL(cotract: String): String {
        return "$event/$cotract"
    }

    fun eventsURL(cotract: String, event: String): String {
        return "$event/$cotract/$event"
    }

}
