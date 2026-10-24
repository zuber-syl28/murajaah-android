package com.sylergy.murajaah.auth

import com.sylergy.murajaah.auth.model.AuthState
import com.sylergy.murajaah.auth.storage.InMemorySessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthTest {

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
    fun testGuestSessionInitialization() = runTest(testDispatcher) {
        val storage = InMemorySessionStorage()
        val viewModel = AuthViewModel(storage)

        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)

        viewModel.signInAsGuest("Test Hafidh")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Guest)
        val session = (state as AuthState.Guest).session
        assertEquals("Test Hafidh", session.name)
        assertTrue(session.isGuest)
        assertFalse(session.onboarded)
        assertEquals("hafs", session.narration)
        assertEquals(5, session.dailyPages)
    }

    @Test
    fun testCloudAccountAuthentication() = runTest(testDispatcher) {
        val storage = InMemorySessionStorage()
        val viewModel = AuthViewModel(storage)

        viewModel.signInWithCloud("hafidh@example.com", "Brother Zuber")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Authenticated)
        val session = (state as AuthState.Authenticated).session
        assertEquals("Brother Zuber", session.name)
        assertEquals("hafidh@example.com", session.email)
        assertFalse(session.isGuest)
        assertNotNull(session.token)
    }

    @Test
    fun testUpdateSettingsAndSignOut() = runTest(testDispatcher) {
        val storage = InMemorySessionStorage()
        val viewModel = AuthViewModel(storage)

        viewModel.signInAsGuest()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSettings(
            narration = "warsh",
            revisionStyle = "ordered",
            dailyPages = 10,
            cardSize = "page"
        )
        viewModel.markOnboarded()

        val state = viewModel.authState.value as AuthState.Guest
        assertEquals("warsh", state.session.narration)
        assertEquals("ordered", state.session.revisionStyle)
        assertEquals(10, state.session.dailyPages)
        assertEquals("page", state.session.cardSize)
        assertTrue(state.session.onboarded)

        viewModel.signOut()
        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)
    }
}
