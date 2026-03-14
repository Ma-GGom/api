package com.maggom.event.port.out

import com.maggom.event.domain.MarathonEvent

interface NotificationMailPort {
    fun sendNotification(to: String, events: List<MarathonEvent>)
}
