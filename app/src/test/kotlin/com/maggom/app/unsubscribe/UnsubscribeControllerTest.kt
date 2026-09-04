package com.maggom.app.unsubscribe

import com.maggom.app.adapter.`in`.web.UnsubscribeController
import com.maggom.member.port.`in`.UnsubscribeUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.IContext
import kotlin.test.assertEquals

class UnsubscribeControllerTest {

    private val unsubscribeUseCase: UnsubscribeUseCase = mockk()
    private val templateEngine: TemplateEngine = mockk()

    private val controller = UnsubscribeController(unsubscribeUseCase, templateEngine)

    @Test
    @DisplayName("링크 클릭만으로는 해지되지 않고 확인 페이지를 응답")
    fun get_request_does_not_unsubscribe() {
        // given
        every { templateEngine.process("unsubscribe/confirm", any<IContext>()) } returns "confirm"

        // when
        val body = controller.confirmPage("token")

        // then
        assertEquals("confirm", body)
        verify(exactly = 0) { unsubscribeUseCase.unsubscribe(any()) }
    }

    @Test
    @DisplayName("확인 버튼 제출 시 해지하고 결과 페이지를 응답")
    fun post_request_unsubscribes() {
        // given
        every { unsubscribeUseCase.unsubscribe("token") } returns true
        every { templateEngine.process("unsubscribe/result", any<IContext>()) } returns "result"

        // when
        val body = controller.unsubscribe("token")

        // then
        assertEquals("result", body)
        verify(exactly = 1) { unsubscribeUseCase.unsubscribe("token") }
    }
}
