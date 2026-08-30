package com.maggom.member.application

import com.maggom.common.exception.MemberNotFoundException
import com.maggom.member.port.`in`.SubscriptionDeleteUseCase
import com.maggom.member.port.`in`.UnsubscribeUseCase
import com.maggom.member.port.out.UnsubscribeTokenPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UnsubscribeService(
    private val unsubscribeTokenPort: UnsubscribeTokenPort,
    private val subscriptionDeleteUseCase: SubscriptionDeleteUseCase,
) : UnsubscribeUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun unsubscribe(token: String): Boolean {
        val email = unsubscribeTokenPort.extractEmail(token)
        if (email == null) {
            log.debug("유효하지 않은 구독 해지 토큰")

            return false
        }

        return try {
            subscriptionDeleteUseCase.delete(email)
            true
        } catch (e: MemberNotFoundException) {
            // 이미 해지된 경우도 성공으로 처리 (원클릭 해지는 멱등해야 함)
            log.debug("이미 해지된 구독 - email: {}", email)
            true
        }
    }
}
