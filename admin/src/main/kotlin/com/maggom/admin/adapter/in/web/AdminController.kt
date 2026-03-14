package com.maggom.admin.adapter.`in`.web

import com.maggom.admin.adapter.`in`.web.dto.MailTemplateType
import com.maggom.admin.adapter.`in`.web.dto.TestMailRequest
import com.maggom.admin.adapter.`in`.web.dto.TestMailResponse
import com.maggom.auth.port.out.WelcomeMailPort
import com.maggom.event.port.out.NotificationMailPort
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val welcomeMailPort: WelcomeMailPort,
    private val notificationMailPort: NotificationMailPort,
) {

    @PostMapping("/mails/test-send")
    @ResponseStatus(HttpStatus.OK)
    fun sendTestMail(@RequestBody request: TestMailRequest): TestMailResponse {
        when (request.templateType) {
            MailTemplateType.WELCOME -> welcomeMailPort.sendWelcomeMail(request.toEmail)
            MailTemplateType.NOTIFICATION -> notificationMailPort.sendNotification(request.toEmail, emptyList())
        }

        return TestMailResponse(
            message = "테스트 메일 발송 요청이 완료되었습니다.",
            requestId = UUID.randomUUID().toString(),
        )
    }
}
