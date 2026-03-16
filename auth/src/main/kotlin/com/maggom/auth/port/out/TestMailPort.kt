package com.maggom.auth.port.out

interface TestMailPort {
    fun sendTestMail(to: String, templateType: String)
}
