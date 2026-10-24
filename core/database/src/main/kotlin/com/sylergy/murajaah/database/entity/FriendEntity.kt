package com.sylergy.murajaah.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    
    // reviewsYou (they listen to you: you are the student)
    val reviewsYouPartnershipId: String? = null,
    val reviewsYouStatus: String? = null, // "pending" | "active"
    val reviewsYouPermission: String? = null, // "assess" | "view"
    val reviewsYouRole: String? = null, // "teacher" | "peer"
    val reviewsYouSince: String? = null,
    val reviewsYouCount: Int = 0,
    val reviewsYouLastReviewedAt: String? = null,
    
    // youReview (you listen to them: you are the examiner)
    val youReviewPartnershipId: String? = null,
    val youReviewStatus: String? = null, // "pending" | "active"
    val youReviewPermission: String? = null, // "assess" | "view"
    val youReviewRole: String? = null,
    val youReviewDueToday: Int = 0,
    val youReviewPlannedCards: Int = 0,
    val youReviewRemind: Boolean = false,
    val youReviewSince: String? = null,
    val youReviewCount: Int = 0,
    val youReviewLastReviewedAt: String? = null,

    val lastActiveAt: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
