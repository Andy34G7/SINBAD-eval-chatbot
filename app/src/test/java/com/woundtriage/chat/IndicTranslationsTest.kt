package com.woundtriage.chat

import com.woundtriage.chat.domain.model.Language
import com.woundtriage.chat.domain.model.SinbadDimension
import com.woundtriage.chat.domain.translation.IndicTranslations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IndicTranslationsTest {

    @Test
    fun testGreetingGenerationAcrossAllSupportedLanguages() {
        for (lang in Language.entries) {
            val greeting = IndicTranslations.getGreeting(lang)
            assertNotNull(greeting)
            assertFalse("Greeting should not be blank for ${lang.englishName}", greeting.isBlank())
        }

        // Verify Malayalam & Gujarati greetings are in their respective scripts
        val mlGreeting = IndicTranslations.getGreeting(Language.MALAYALAM)
        assertTrue(mlGreeting.contains("നമസ്കാരം"))

        val guGreeting = IndicTranslations.getGreeting(Language.GUJARATI)
        assertTrue(guGreeting.contains("નમસ્તે"))
    }

    @Test
    fun testQuestionsGenerationAcrossAllDimensionsAndLanguages() {
        for (dim in SinbadDimension.ORDERED_DIMENSIONS) {
            for (lang in Language.entries) {
                val q = IndicTranslations.getQuestion(dim, lang)
                assertNotNull(q)
                assertFalse("Question for ${dim.name} in ${lang.englishName} should not be blank", q.isBlank())
            }
        }
    }

    @Test
    fun testPatientResponseParsingEnglish() {
        // Site: Toes -> 0, Heel -> 1
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.SITE, "On my front toes").first)
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.SITE, "On my heel").first)

        // Ischemia: Cold/Cramping -> 1, Warm -> 0
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.ISCHEMIA, "Yes, very cold").first)
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.ISCHEMIA, "No, it is warm and normal").first)

        // Neuropathy: Numb -> 1, Sensation fine -> 0
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.NEUROPATHY, "Yes, numbness and tingling").first)
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.NEUROPATHY, "No numbness, I feel everything").first)

        // Bacterial infection: Pus -> 1, None -> 0
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.BACTERIAL_INFECTION, "Yes, yellow pus and bad smell").first)
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.BACTERIAL_INFECTION, "No signs of infection").first)

        // Area: Bigger -> 1, Smaller -> 0
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.AREA, "It is quite large, bigger than a coin").first)
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.AREA, "Very small, smaller than a coin").first)

        // Depth: Bone -> 1, Superficial -> 0
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.DEPTH, "Deep, I think bone is exposed").first)
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.DEPTH, "Superficial surface scratch").first)
    }

    @Test
    fun testPatientResponseParsingHindi() {
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.SITE, "पैर की उंगली पर है").first)
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.SITE, "एड़ी पर है").first)

        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.ISCHEMIA, "हाँ पैर बहुत ठंडा है").first)
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.ISCHEMIA, "नहीं, सामान्य गर्म है").first)

        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.NEUROPATHY, "हाँ सुन्न हो गया है").first)
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.BACTERIAL_INFECTION, "हाँ मवाद निकल रहा है").first)
    }

    @Test
    fun testPatientResponseParsingMalayalamAndGujarati() {
        // Malayalam
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.SITE, "കാൽവിരലുകളിൽ ആണ്").first)
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.SITE, "ഉപ്പൂറ്റിയിൽ ആണ്").first)
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.ISCHEMIA, "അതെ തണുപ്പാണ്").first)
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.BACTERIAL_INFECTION, "പഴുപ്പ് വരുന്നുണ്ട്").first)

        // Gujarati
        assertEquals(0, IndicTranslations.parsePatientResponse(SinbadDimension.SITE, "પગની આંગળી પર છે").first)
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.SITE, "એડી પર છે").first)
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.ISCHEMIA, "હા ઠંડુ રહે છે").first)
        assertEquals(1, IndicTranslations.parsePatientResponse(SinbadDimension.BACTERIAL_INFECTION, "પરુ નીકળે છે").first)
    }
}
