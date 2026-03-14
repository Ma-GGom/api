package com.maggom.app.notification

import com.maggom.event.domain.MarathonEvent

interface NotificationMailPort {
    fun sendNotification(to: String, events: List<MarathonEvent>)
}
