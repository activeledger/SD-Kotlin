package com.example.activeledgersdk.event

import com.here.oksse.ServerSentEvent


class Event(val sse: ServerSentEvent, val id: String, val event: String, val message: String)
