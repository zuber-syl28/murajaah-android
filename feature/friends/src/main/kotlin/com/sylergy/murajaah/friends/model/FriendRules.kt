package com.sylergy.murajaah.friends.model

import com.sylergy.murajaah.database.entity.FriendEntity

object FriendRules {

    fun dueFor(friend: FriendEntity): Int {
        return if (friend.youReviewStatus == "active") friend.youReviewDueToday else 0
    }

    /** They listen to you and may grade: you can recite to them by voice. */
    fun canRecite(friend: FriendEntity): Boolean {
        return friend.reviewsYouStatus == "active" && friend.reviewsYouPermission == "assess"
    }

    /** Can you listen to them: you review them and it's active. */
    fun canListen(friend: FriendEntity): Boolean {
        return friend.youReviewStatus == "active"
    }

    /** Whether relationship is mutual (both review each other). */
    fun isMutual(friend: FriendEntity): Boolean {
        return friend.reviewsYouStatus == "active" && friend.youReviewStatus == "active"
    }

    /**
     * How you are joined first — mutual tasmīʿ or one way only — then what is due to hear.
     */
    fun rowLine(friend: FriendEntity): String {
        val due = dueFor(friend)
        val dueText = if (due > 0) "$due due to hear" else "nothing due"
        val you = friend.youReviewStatus == "active"
        val they = friend.reviewsYouStatus == "active"

        return when {
            you && they -> "Mutual tasmīʿ · $dueText"
            you -> "You hear only · $dueText"
            friend.youReviewStatus == "pending" -> "Waiting for them"
            they -> if (friend.reviewsYouPermission == "assess") "Hears you only" else "Can see your progress"
            else -> "Not connected"
        }
    }

    fun relation(friend: FriendEntity): String {
        val they = friend.reviewsYouStatus == "active"
        val you = friend.youReviewStatus == "active"

        return when {
            they && you -> "You and ${friend.name} review each other"
            they -> "${friend.name} listens to you"
            you -> "You listen to ${friend.name}"
            else -> "Not reviewing together"
        }
    }
}
