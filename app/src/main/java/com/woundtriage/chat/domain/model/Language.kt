package com.woundtriage.chat.domain.model

/**
 * Supported Indic and global languages for on-device triage questioning.
 */
enum class Language(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "हिन्दी", "Hindi"),
    TAMIL("ta", "தமிழ்", "Tamil"),
    TELUGU("te", "తెలుగు", "Telugu"),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada"),
    BENGALI("bn", "বাংলা", "Bengali"),
    MARATHI("mr", "मराठी", "Marathi"),
    MALAYALAM("ml", "മലയാളം", "Malayalam"),
    GUJARATI("gu", "ગુજરાતી", "Gujarati");

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
