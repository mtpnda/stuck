package com.mtpnda.stuck.model

data class Profile(
    val name: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val faceId: Boolean = true,
    val provider: String = "local",
    val createdAt: Long = System.currentTimeMillis()
) {
    val initials: String
        get() {
            val parts = name.split(" ").filter { it.isNotBlank() }
            if (parts.isEmpty()) return "U"
            if (parts.size == 1) return parts[0].take(2).uppercase()
            return (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
}
