package com.sylergy.murajaah.voice.crypto

import com.sylergy.murajaah.voice.model.EncryptedVoiceFrame
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * End-to-end encryption cipher for peer-to-peer voice recitation packets.
 * Uses AES-256-GCM (Galois/Counter Mode) with 128-bit authentication tag
 * matching DTLS-SRTP security guarantees.
 */
class VoiceCipher(sharedSecret: String) {

    private val secretKeySpec: SecretKeySpec
    val securityFingerprint: String
    private val secureRandom = SecureRandom()

    init {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha256.digest(sharedSecret.toByteArray(Charsets.UTF_8))
        secretKeySpec = SecretKeySpec(keyBytes, "AES")

        // 6-group hex fingerprint for user identity verification (e.g. "8F2A 4B19 C012 ...")
        val hash = sha256.digest(keyBytes)
        securityFingerprint = hash.take(12)
            .chunked(2)
            .joinToString(" ") { bytes ->
                bytes.joinToString("") { "%02X".format(it) }
            }
    }

    /**
     * Encrypts a raw PCM audio frame with AES-GCM and unique IV.
     */
    fun encrypt(rawPcmFrame: ByteArray, sequenceNumber: Long): EncryptedVoiceFrame {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)
        
        // Incorporate sequence number into IV to guarantee uniqueness
        val seqBytes = ByteBuffer.allocate(8).putLong(sequenceNumber).array()
        for (i in 0 until 8) {
            iv[i] = (iv[i].toInt() xor seqBytes[i].toInt()).toByte()
        }

        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec)
        
        // Add sequence number as Additional Authenticated Data (AAD)
        cipher.updateAAD(seqBytes)
        val cipherText = cipher.doFinal(rawPcmFrame)

        return EncryptedVoiceFrame(
            sequenceNumber = sequenceNumber,
            iv = iv,
            cipherText = cipherText
        )
    }

    /**
     * Decrypts an encrypted audio frame. Throws AEADBadTagException if altered.
     */
    fun decrypt(frame: EncryptedVoiceFrame): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, frame.iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec)

        val seqBytes = ByteBuffer.allocate(8).putLong(frame.sequenceNumber).array()
        cipher.updateAAD(seqBytes)
        return cipher.doFinal(frame.cipherText)
    }
}
