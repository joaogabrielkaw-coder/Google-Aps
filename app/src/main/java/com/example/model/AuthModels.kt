package com.example.model

/**
 * Método de autenticação utilizado pelo usuário.
 */
enum class AuthMethod(val labelPt: String, val labelEn: String) {
    GOOGLE("Google", "Google"),
    PHONE("Telefone", "Phone Number")
}

/**
 * Informações completas do usuário autenticado (Google ou Número de Telefone).
 */
data class UserAccount(
    val userId: String,
    val displayName: String,
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val authMethod: AuthMethod = AuthMethod.GOOGLE,
    val totalScore: Int = 0,
    val levelNumber: Int = 1,
    val levelTitlePt: String = "Aspirante a Dev",
    val levelTitleEn: String = "Aspiring Dev",
    val streakDays: Int = 0,
    val completedActivitiesCount: Int = 0,
    val badgesCount: Int = 0,
    val isLoggedIn: Boolean = true
) {
    val maskedPhone: String
        get() {
            if (phoneNumber.length < 8) return phoneNumber
            val clean = phoneNumber.replace(Regex("[^0-9]"), "")
            return if (clean.length >= 10) {
                val ddd = clean.substring(0, 2)
                val last4 = clean.takeLast(4)
                "($ddd) 9****-$last4"
            } else {
                "****-${phoneNumber.takeLast(4)}"
            }
        }
}

/**
 * Entrada no placar geral de desenvolvedores (Leaderboard).
 */
data class LeaderboardEntry(
    val userId: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val authMethod: String = "google",
    val totalScore: Int = 0,
    val levelNumber: Int = 1,
    val levelTitlePt: String = "Aspirante a Dev",
    val levelTitleEn: String = "Aspiring Dev",
    val streakDays: Int = 0,
    val badgesCount: Int = 0,
    val rank: Int = 0,
    val isCurrentUser: Boolean = false
) {
    val maskedPhone: String
        get() {
            if (phoneNumber.isBlank()) return ""
            val clean = phoneNumber.replace(Regex("[^0-9]"), "")
            return if (clean.length >= 10) {
                val ddd = clean.substring(0, 2)
                val last4 = clean.takeLast(4)
                "($ddd) 9****-$last4"
            } else {
                "****-${phoneNumber.takeLast(4)}"
            }
        }
}
