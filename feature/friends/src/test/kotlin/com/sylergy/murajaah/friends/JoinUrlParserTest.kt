package com.sylergy.murajaah.friends

import com.sylergy.murajaah.friends.scanner.JoinUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class JoinUrlParserTest {

    @Test
    fun parseDirectCustomDeepLinkQuery() {
        val raw = "murajaah://join?token=inv_test_token_123"
        val parsed = JoinUrlParser.parseToken(raw)
        assertEquals("inv_test_token_123", parsed)
    }

    @Test
    fun parseDirectCustomDeepLinkPath() {
        val raw = "murajaah://join/inv_test_token_456"
        val parsed = JoinUrlParser.parseToken(raw)
        assertEquals("inv_test_token_456", parsed)
    }

    @Test
    fun parseHttpsWebUrl() {
        val raw = "https://murajaah.sylergy-ltd.workers.dev/join/inv_789xyz"
        val parsed = JoinUrlParser.parseToken(raw)
        assertEquals("inv_789xyz", parsed)
    }

    @Test
    fun parseHttpsWebUrlWithTrailingSlash() {
        val raw = "https://murajaah.sylergy-ltd.workers.dev/join/inv_789xyz/"
        val parsed = JoinUrlParser.parseToken(raw)
        assertEquals("inv_789xyz", parsed)
    }

    @Test
    fun parseRawToken() {
        val raw = "inv_abc_def_123"
        val parsed = JoinUrlParser.parseToken(raw)
        assertEquals("inv_abc_def_123", parsed)
    }

    @Test
    fun rejectInvalidUrl() {
        assertNull(JoinUrlParser.parseToken(""))
        assertNull(JoinUrlParser.parseToken("   "))
        assertNull(JoinUrlParser.parseToken("https://malicious-phishing.com/steal"))
        assertNull(JoinUrlParser.parseToken("short"))
    }

    @Test
    fun buildUrls() {
        val token = "token_123"
        val webUrl = JoinUrlParser.buildInviteUrl(token)
        assertEquals("https://murajaah.sylergy-ltd.workers.dev/join/token_123", webUrl)

        val deepLink = JoinUrlParser.buildDeepLinkUri(token)
        assertEquals("murajaah://join?token=token_123", deepLink)
    }
}
