package com.maggom.app.adapter

import com.maggom.member.port.out.UnsubscribeTokenPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 메일 수신 거부용 서명 토큰 어댑터.
 *
 * 형식: `base64url(email).base64url(HMAC-SHA256(base64url(email)))`
 * 메일은 발송 후 한참 뒤에 열릴 수 있으므로 만료를 두지 않는다.
 */
@Component
class UnsubscribeTokenAdapter(
    @Value("\${maggom.auth.unsubscribe-secret:\${maggom.auth.jwt-secret}}") private val secret: String,
) : UnsubscribeTokenPort {

    /** JWT 서명키와 같은 시크릿을 쓰더라도 키 재질이 겹치지 않도록 용도별로 파생한다. */
    private val key by lazy {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), HMAC_ALGORITHM))

        SecretKeySpec(mac.doFinal(KEY_INFO.toByteArray(Charsets.UTF_8)), HMAC_ALGORITHM)
    }

    override fun generate(email: String): String {
        val payload = encode(email.toByteArray(Charsets.UTF_8))

        return "$payload.${sign(payload)}"
    }

    override fun extractEmail(token: String): String? {
        val parts = token.split(".")
        if (parts.size != 2) return null

        val payload = parts[0]
        val signature = parts[1]
        if (!isValidSignature(payload, signature)) return null

        return try {
            String(Base64.getUrlDecoder().decode(payload), Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun isValidSignature(payload: String, signature: String): Boolean {
        return MessageDigest.isEqual(
            sign(payload).toByteArray(Charsets.UTF_8),
            signature.toByteArray(Charsets.UTF_8),
        )
    }

    private fun sign(payload: String): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(key)

        return encode(mac.doFinal(payload.toByteArray(Charsets.UTF_8)))
    }

    private fun encode(bytes: ByteArray): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    companion object {
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val KEY_INFO = "maggom:unsubscribe:v1"
    }
}
