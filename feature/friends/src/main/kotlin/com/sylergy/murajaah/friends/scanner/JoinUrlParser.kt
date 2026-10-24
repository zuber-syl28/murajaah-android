package com.sylergy.murajaah.friends.scanner

import java.net.URI

object JoinUrlParser {
    private val JOIN_PATH_REGEX = Regex("""^/join/([^/]+)/?$""")
    private val TOKEN_QUERY_REGEX = Regex("""[?&]token=([^&#]+)""")

    /**
     * Extracts and validates an invite token from a scanned QR string, deep link URI, or raw token.
     * Returns the token if valid, or null if the URL / text is invalid.
     */
    fun parseToken(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        // 1. Direct custom deep link: murajaah://join?token=XYZ or murajaah://join/XYZ
        if (trimmed.startsWith("murajaah://", ignoreCase = true)) {
            val queryMatch = TOKEN_QUERY_REGEX.find(trimmed)
            if (queryMatch != null) {
                val token = queryMatch.groupValues[1]
                if (token.isNotBlank()) return token.trim()
            }
            val pathPart = trimmed.removePrefix("murajaah://").removePrefix("murajaah:/")
            val match = Regex("""^join/([^/?#]+)""").find(pathPart)
            if (match != null) {
                val token = match.groupValues[1]
                if (token.isNotBlank()) return token.trim()
            }
        }

        // 2. HTTP / HTTPS link: e.g. https://murajaah.sylergy-ltd.workers.dev/join/{token}
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            try {
                val uri = URI(trimmed)
                val path = uri.path ?: ""
                val match = JOIN_PATH_REGEX.find(path)
                if (match != null) {
                    val token = match.groupValues[1]
                    if (token.isNotBlank()) return token.trim()
                }
            } catch (_: Exception) {
                // fall through
            }
        }

        // 3. Raw token: alphanumeric with dashes/underscores, between 6 and 128 characters
        val cleanToken = trimmed.removePrefix("join/").removePrefix("/join/")
        if (cleanToken.matches(Regex("""^[a-zA-Z0-9_\-]{6,128}$"""))) {
            return cleanToken
        }

        return null
    }

    /**
     * Builds standard shareable invite URL from a token.
     */
    fun buildInviteUrl(token: String, baseUrl: String = "https://murajaah.sylergy-ltd.workers.dev"): String {
        val base = baseUrl.trimEnd('/')
        return "$base/join/$token"
    }

    /**
     * Builds deep link URI for native app intent from a token.
     */
    fun buildDeepLinkUri(token: String): String {
        return "murajaah://join?token=$token"
    }
}
