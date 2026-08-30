package com.maggom.app.adapter.`in`.web

import com.maggom.member.port.`in`.UnsubscribeUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.Locale

@RestController
@RequestMapping("/api/v1/subscriptions/unsubscribe")
class UnsubscribeController(
    private val unsubscribeUseCase: UnsubscribeUseCase,
    private val templateEngine: TemplateEngine,
) {

    /** 메일 본문의 해지 링크 (사람이 클릭) - 결과 안내 페이지를 그대로 응답한다. */
    @GetMapping(produces = ["text/html;charset=UTF-8"])
    @ResponseStatus(HttpStatus.OK)
    fun unsubscribeByLink(@RequestParam token: String): String {
        val success = unsubscribeUseCase.unsubscribe(token)
        val context = Context(Locale.KOREAN).apply {
            setVariable("success", success)
        }

        return templateEngine.process("unsubscribe/result", context)
    }

    /** List-Unsubscribe-Post 원클릭 해지 (RFC 8058) - 메일 클라이언트가 호출한다. */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun unsubscribeOneClick(@RequestParam token: String) {
        unsubscribeUseCase.unsubscribe(token)
    }
}
