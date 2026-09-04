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
@RequestMapping(UnsubscribeController.PATH)
class UnsubscribeController(
    private val unsubscribeUseCase: UnsubscribeUseCase,
    private val templateEngine: TemplateEngine,
) {

    /**
     * 메일 본문의 해지 링크 (사람이 클릭).
     *
     * 실수로 눌렀을 수 있으므로 여기서는 해지하지 않고 확인 페이지만 보여준다.
     */
    @GetMapping(produces = [HTML_CONTENT_TYPE])
    @ResponseStatus(HttpStatus.OK)
    fun confirmPage(@RequestParam token: String): String {
        val context = Context(Locale.KOREAN).apply {
            setVariable("token", token)
            setVariable("actionPath", PATH)
        }

        return templateEngine.process("unsubscribe/confirm", context)
    }

    /**
     * 실제 해지 처리.
     *
     * 확인 페이지의 버튼과 List-Unsubscribe-Post 원클릭 해지(RFC 8058)가 함께 사용한다.
     * 원클릭은 추가 확인을 요구하면 안 되므로 이 경로에서 곧바로 처리한다.
     */
    @PostMapping(produces = [HTML_CONTENT_TYPE])
    @ResponseStatus(HttpStatus.OK)
    fun unsubscribe(@RequestParam token: String): String {
        val success = unsubscribeUseCase.unsubscribe(token)
        val context = Context(Locale.KOREAN).apply {
            setVariable("success", success)
        }

        return templateEngine.process("unsubscribe/result", context)
    }

    companion object {
        const val PATH = "/api/v1/subscriptions/unsubscribe"
        private const val HTML_CONTENT_TYPE = "text/html;charset=UTF-8"
    }
}
