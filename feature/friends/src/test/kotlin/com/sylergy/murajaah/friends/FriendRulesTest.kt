package com.sylergy.murajaah.friends

import com.sylergy.murajaah.database.entity.FriendEntity
import com.sylergy.murajaah.friends.model.FriendRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FriendRulesTest {

    @Test
    fun testDueForActiveReview() {
        val friendWithDue = FriendEntity(
            userId = "u1",
            name = "Bilal",
            youReviewStatus = "active",
            youReviewDueToday = 5
        )
        assertEquals(5, FriendRules.dueFor(friendWithDue))

        val friendInactive = FriendEntity(
            userId = "u2",
            name = "Omar",
            youReviewStatus = "pending",
            youReviewDueToday = 5
        )
        assertEquals(0, FriendRules.dueFor(friendInactive))
    }

    @Test
    fun testRecitePermission() {
        val friendAssess = FriendEntity(
            userId = "u1",
            name = "Zaid",
            reviewsYouStatus = "active",
            reviewsYouPermission = "assess"
        )
        assertTrue(FriendRules.canRecite(friendAssess))

        val friendViewOnly = FriendEntity(
            userId = "u2",
            name = "Tariq",
            reviewsYouStatus = "active",
            reviewsYouPermission = "view"
        )
        assertFalse(FriendRules.canRecite(friendViewOnly))
    }

    @Test
    fun testMutualRelation() {
        val mutualFriend = FriendEntity(
            userId = "u1",
            name = "Ahmad",
            reviewsYouStatus = "active",
            youReviewStatus = "active",
            youReviewDueToday = 3
        )
        assertTrue(FriendRules.isMutual(mutualFriend))
        assertEquals("You and Ahmad review each other", FriendRules.relation(mutualFriend))
        assertEquals("Mutual tasmīʿ · 3 due to hear", FriendRules.rowLine(mutualFriend))
    }

    @Test
    fun testOneWayRelation() {
        val listenerOnly = FriendEntity(
            userId = "u2",
            name = "Ali",
            reviewsYouStatus = "active",
            reviewsYouPermission = "assess",
            youReviewStatus = null
        )
        assertFalse(FriendRules.isMutual(listenerOnly))
        assertEquals("Ali listens to you", FriendRules.relation(listenerOnly))
        assertEquals("Hears you only", FriendRules.rowLine(listenerOnly))
    }
}
