package com.maggom.auth.port.out

interface WelcomeMailPort {
    fun sendWelcomeMail(to: String)
}
