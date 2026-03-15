package com.maggom.member.adapter.`in`.web

import com.maggom.member.adapter.`in`.web.dto.SubscriptionCountResponse
import com.maggom.member.adapter.`in`.web.dto.SubscriptionResponse
import com.maggom.member.adapter.`in`.web.dto.UpdateSubscriptionRequest
import com.maggom.member.port.`in`.SubscriptionCountUseCase
import com.maggom.member.port.`in`.SubscriptionDeleteUseCase
import com.maggom.member.port.`in`.SubscriptionQueryUseCase
import com.maggom.member.port.`in`.SubscriptionUpdateUseCase
import com.maggom.member.port.`in`.UpdateSubscriptionCommand
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/subscriptions")
class SubscriptionController(
    private val subscriptionQueryUseCase: SubscriptionQueryUseCase,
    private val subscriptionUpdateUseCase: SubscriptionUpdateUseCase,
    private val subscriptionDeleteUseCase: SubscriptionDeleteUseCase,
    private val subscriptionCountUseCase: SubscriptionCountUseCase,
) {

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    fun getMySubscription(request: HttpServletRequest): SubscriptionResponse {
        val email = request.getAttribute("authenticatedEmail") as String
        val result = subscriptionQueryUseCase.getByEmail(email)

        return SubscriptionResponse(
            receiveDays = result.receiveDays,
            receiveTime = result.receiveTime,
            prefRegions = result.prefRegions,
            prefDistances = result.prefDistances,
            includeSmall = result.includeSmall,
        )
    }

    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    fun updateMySubscription(
        @RequestBody request: UpdateSubscriptionRequest,
        servletRequest: HttpServletRequest,
    ) {
        val email = servletRequest.getAttribute("authenticatedEmail") as String
        subscriptionUpdateUseCase.update(
            UpdateSubscriptionCommand(
                email = email,
                receiveDays = request.receiveDays,
                receiveTime = request.receiveTime,
                prefRegions = request.prefRegions,
                prefDistances = request.prefDistances,
                includeSmall = request.includeSmall,
            )
        )
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    fun deleteMySubscription(request: HttpServletRequest) {
        val email = request.getAttribute("authenticatedEmail") as String
        subscriptionDeleteUseCase.delete(email)
    }

    @GetMapping("/count")
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionCount(): SubscriptionCountResponse {
        return SubscriptionCountResponse(subscriptionCountUseCase.count())
    }
}
