package com.sylergy.murajaah.voice

import com.sylergy.murajaah.voice.crypto.VoiceCipher
import com.sylergy.murajaah.voice.model.EndReason
import com.sylergy.murajaah.voice.model.VoiceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import javax.crypto.AEADBadTagException

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testVoiceCipherEncryptionAndDecryption() {
        val secret = "test-session-secret-12345"
        val cipher = VoiceCipher(secret)

        val originalData = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ - Raw audio PCM buffer".toByteArray(Charsets.UTF_8)
        val encryptedFrame = cipher.encrypt(originalData, sequenceNumber = 1L)

        // Verify ciphertext is different from original
        assertNotEquals(originalData.toList(), encryptedFrame.cipherText.toList())
        assertEquals(1L, encryptedFrame.sequenceNumber)
        assertEquals(12, encryptedFrame.iv.size)

        // Decrypt and assert equality
        val decryptedData = cipher.decrypt(encryptedFrame)
        assertArrayEquals(originalData, decryptedData)
    }

    @Test(expected = AEADBadTagException::class)
    fun testVoiceCipherTamperDetection() {
        val secret = "test-session-secret-12345"
        val cipher = VoiceCipher(secret)

        val originalData = "Audio buffer packet".toByteArray(Charsets.UTF_8)
        val encryptedFrame = cipher.encrypt(originalData, sequenceNumber = 42L)

        // Tamper with ciphertext
        encryptedFrame.cipherText[0] = (encryptedFrame.cipherText[0].toInt() xor 0xFF).toByte()

        // Should throw AEADBadTagException due to authentication tag mismatch
        cipher.decrypt(encryptedFrame)
    }

    @Test
    fun testSecurityFingerprintFormat() {
        val cipher = VoiceCipher("friendship-pairing-hash")
        val fingerprint = cipher.securityFingerprint

        assertTrue("Fingerprint should be non-empty", fingerprint.isNotBlank())
        // Format should be 6 groups separated by spaces
        val groups = fingerprint.split(" ")
        assertEquals("Fingerprint should have 6 groups", 6, groups.size)
        groups.forEach { group ->
            assertEquals("Each group should be 4 hex chars", 4, group.length)
        }
    }

    @Test
    fun testVoiceSessionLifecycleReciter() = runTest(testDispatcher) {
        val viewModel = VoiceSessionViewModel()

        assertEquals(VoiceStatus.Idle, viewModel.status.value)

        // Recite to friend
        viewModel.reciteTo(friendId = "friend_bilal", friendName = "Bilal Ibn Rabah")
        assertTrue(viewModel.status.value is VoiceStatus.Starting)

        // Advance to waiting
        advanceTimeBy(500)
        val waiting = viewModel.status.value as VoiceStatus.Waiting
        assertEquals("friend_bilal", waiting.friendId)
        assertEquals("Bilal Ibn Rabah", waiting.name)
        assertTrue(waiting.isReciter)
        assertEquals(120, waiting.secondsLeft)

        // Friend answers
        viewModel.simulateFriendAnswers()
        advanceTimeBy(700)

        val connected = viewModel.status.value as VoiceStatus.Connected
        assertEquals("Bilal Ibn Rabah", connected.name)
        assertTrue(connected.isReciter)
        assertEquals(0, connected.durationSeconds)
        assertTrue(connected.e2eVerified)
        assertTrue(connected.fingerprint.isNotBlank())

        // Mute toggle
        viewModel.toggleMute()
        val muted = viewModel.status.value as VoiceStatus.Connected
        assertTrue(muted.isMuted)

        // Share passage
        viewModel.sharePassage("Al-Mulk", "الملك", 1, 10)
        val passageSync = viewModel.status.value as VoiceStatus.Connected
        assertEquals("Al-Mulk", passageSync.currentPassage?.surahName)
        assertEquals(1, passageSync.currentPassage?.fromAyah)
        assertEquals(10, passageSync.currentPassage?.toAyah)

        // End call
        viewModel.endCall()
        val ended = viewModel.status.value as VoiceStatus.Ended
        assertEquals("Bilal Ibn Rabah", ended.name)
        assertEquals(EndReason.HUNG_UP, ended.reason)

        // Dismiss
        viewModel.dismiss()
        assertEquals(VoiceStatus.Idle, viewModel.status.value)
    }

    @Test
    fun testIncomingCallAndJoin() = runTest(testDispatcher) {
        val viewModel = VoiceSessionViewModel()

        viewModel.notifyIncomingCall(
            callId = "call_999",
            friendId = "friend_ali",
            friendName = "Ali Ibn Abi Talib",
            toHear = true
        )

        val incoming = viewModel.status.value as VoiceStatus.Incoming
        assertEquals("call_999", incoming.callId)
        assertEquals("Ali Ibn Abi Talib", incoming.name)
        assertTrue(incoming.toHear)

        // Join call
        viewModel.joinCall("call_999")
        advanceTimeBy(700)

        val connected = viewModel.status.value as VoiceStatus.Connected
        assertEquals("Ali Ibn Abi Talib", connected.name)
        // If they want to hear us, we are reciter
        assertFalse(connected.isReciter) // listener role

        viewModel.endCall()
        viewModel.dismiss()
    }
}
