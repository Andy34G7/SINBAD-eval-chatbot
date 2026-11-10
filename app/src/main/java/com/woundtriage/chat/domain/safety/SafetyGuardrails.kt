package com.woundtriage.chat.domain.safety

import com.woundtriage.chat.domain.model.Language
import java.util.Locale

sealed class SafetyCheckResult {
    data object Safe : SafetyCheckResult()
    data class Emergency(val reason: String, val advisory: String) : SafetyCheckResult()
    data class NonDiagnosticDeflection(val responseText: String) : SafetyCheckResult()
}

/**
 * Robust medical safety guardrails to enforce non-diagnostic policy,
 * detect emergency red-flag conditions, and resist adversarial prompts.
 */
object SafetyGuardrails {

    // Red flag trigger keywords across English, Romanized Indic, and native Indic scripts
    private val EMERGENCY_KEYWORDS = listOf(
        // English
        "fever", "chills", "high temp", "102", "103", "104", "shivering",
        "red streaks", "streaking", "spreading red", "spreading fast",
        "black skin", "black tissue", "turned black", "gangrene", "blackened", "dying skin",
        "foul smell", "rotting smell", "gas", "bubbles",
        "dizzy", "passed out", "fainting", "confusion", "unconscious", "breathless",
        "sepsis", "septic", "septic shock",
        // Romanized Indic
        "tez bukhar", "bukhar", "bukhaar", "kaala pad", "kala pad", "black ho gaya",
        "kaala ho gaya", "sad raha hai", "badbu", "behoshi", "behos", "chakkad",
        "kaal karuppu", "rotting", "necrotic",
        // Indic native scripts
        "बुखार", "कांपना", "काली त्वचा", "गैंग्रीन", "बेहोश", // Hindi
        "காய்ச்சல்", "நடுக்கம்", "கருப்பு தோல்", "கேங்க்ரீன்", // Tamil
        "జ్వరం", "వణుకు", "నల్లటి చర్మం", "గ్యాంగ్రీన్", // Telugu
        "ಜ್ವರ", "ನಡುಕ", "ಕಪ್ಪು ಚರ್ಮ", "ಗ್ಯಾಂಗ್ರೀನ್", // Kannada
        "জ্বর", "কাঁপুনি", "কালো চামড়া", "গ্যাংগ্রিন", // Bengali
        "ताप", "थंडी", "काळी त्वचा", "गँगरीन", // Marathi
        "പനി", "വിറയൽ", "കറുത്ത തൊലി", "ഗാംഗ്രീൻ", // Malayalam
        "તાવ", "ધ્રૂજારી", "કાળી ચામડી", "ગેંગરીન" // Gujarati
    )

    private val DIAGNOSIS_KEYWORDS = listOf(
        // English
        "diagnose", "diagnosis", "what disease", "is it cancer", "is this gangrene",
        "will i lose my foot", "amputation", "amputate", "will they cut my leg",
        "tell me if it is", "what is wrong with me", "what condition",
        // Romanized Indic
        "kya cancer", "cancer hai", "pair katega", "pair kaatna", "leg cut",
        "amputation hoga", "amputate karna", "kya bimari", "bimari batao",
        "rog kya hai", "kaal vettuvangala", "diagnose karo", "batao kya hua hai",
        // Adversarial prompt injection defense
        "ignore previous instructions", "ignore all instructions", "system prompt",
        "developer instructions", "jailbreak", "act as a doctor", "pretend to be a doctor",
        "you are now a doctor", "diagnose me anyway",
        // Indic native scripts
        "निदान", "क्या यह कैंसर है", "क्या पैर कटेगा", // Hindi
        "நோய் கண்டறிதல்", "கால் வெட்டப்படுமா", "புற்றுநோயா", // Tamil
        "రోగ నిర్ధారణ", "కాల్ తీసివేస్తారా", "క్యాన్సరా", // Telugu
        "ರೋಗನಿರ್ಣಯ", "ಕಾಲು ಕತ್ತರಿಸಬೇಕಾಗುತ್ತಾ", "ಕ್ಯಾನ್ಸರ್ ಇದೆಯೇ", // Kannada
        "রোগ নির্ণয়", "পা কাটতে হবে", "ক্যান্সার কি", // Bengali
        "वैद्यकीय निदान", "पाय कापावा लागेल का", "कॅन्सर आहे का", // Marathi
        "രോഗനിർണയം", "ക്യാൻസർ ആണോ", "കാൽ മുറിക്കുമോ", // Malayalam
        "નિદાન", "કેન્સર છે", "પગ કાપવો પડશે" // Gujarati
    )

    private val PRESCRIPTION_KEYWORDS = listOf(
        // English
        "antibiotic", "prescription", "prescribe", "what medicine", "what tablet",
        "which ointment", "home remedy", "turmeric", "honey", "dettol",
        "what pills", "how to cure", "cure it myself", "pain killer", "painkiller",
        // Romanized Indic
        "dawa", "dawai", "goli", "kaun si dawa", "kya dawai lu", "kya dava lu",
        "dawa batao", "antibiotic du", "marunthu", "mandulu", "haldi lagau",
        "turmeric lagau", "dettol lagau", "gharelu nuskha", "totka", "malham",
        "konsi goli", "kya karu", "tang me dard bahut jyada hai kya karu",
        "kaal romba valikudhu",
        // Indic native scripts
        "दवा", "एंटीबायोटिक", "इलाज", "गोली", "हल्दी", "मरहम", // Hindi
        "மருந்து", "மாத்திரை", "ஆன்டிபயாடிக்", "மஞ்சள்", // Tamil
        "మందు", "టాబ్లెట్", "యాంటీబయాటిక్", "పసుపు", // Telugu
        "ಔಷಧಿ", "ಮಾತ್ರೆ", "ಆಂಟಿಬಯೋಟಿಕ್", "ಅರಿಶಿನ", // Kannada
        "ওষুধ", "ট্যাবলেট", "অ্যান্টিবায়োটিক", "হলুদ", // Bengali
        "औषध", "गोळी", "अँटिबायोटिक", "हળદ", // Marathi
        "മരുന്ന്", "ഗുളിക", "ആന്റിബയോട്ടിക്", "മഞ്ഞൾ", // Malayalam
        "દવા", "ગોળી", "એન્ટિબાયોટિક", "હળદર" // Gujarati
    )

    /**
     * Inspects incoming user input for medical emergencies or non-diagnostic violations.
     */
    fun evaluate(input: String, language: Language): SafetyCheckResult {
        val lower = input.lowercase(Locale.ROOT).trim()

        // 1. Check for Emergency Red Flags
        for (keyword in EMERGENCY_KEYWORDS) {
            if (lower.contains(keyword.lowercase(Locale.ROOT))) {
                return SafetyCheckResult.Emergency(
                    reason = "Possible severe systemic or rapidly progressing infection sign detected ($keyword).",
                    advisory = getEmergencyAdvisory(language)
                )
            }
        }

        // 2. Check for Diagnosis Requests
        for (keyword in DIAGNOSIS_KEYWORDS) {
            if (lower.contains(keyword.lowercase(Locale.ROOT))) {
                return SafetyCheckResult.NonDiagnosticDeflection(
                    responseText = getDiagnosisDeflection(language)
                )
            }
        }

        // 3. Check for Prescription / Medication Inquiries
        for (keyword in PRESCRIPTION_KEYWORDS) {
            if (lower.contains(keyword.lowercase(Locale.ROOT))) {
                return SafetyCheckResult.NonDiagnosticDeflection(
                    responseText = getPrescriptionDeflection(language)
                )
            }
        }

        return SafetyCheckResult.Safe
    }

    fun getEmergencyAdvisory(language: Language): String = when (language) {
        Language.HINDI ->
            "आपातकालीन चेतावनी: गंभीर संक्रमण या बुखार के लक्षण पाए गए हैं। कृपया तुरंत निकटतम आपातकालीन कक्ष (ER) या अस्पताल जाएं।"
        Language.TAMIL ->
            "அவசர எச்சரிக்கை: கடுமையான தொற்று அல்லது காய்ச்சல் அறிகுறிகள் காணப்படுகின்றன. உடனடியாக அருகிலுள்ள அவசர மருத்துவமனைக்குச் செல்லவும்."
        Language.TELUGU ->
            "అత్యవసర హెచ్చరిక: తీవ్రమైన ఇన్ఫెక్షన్ లేదా జ్వరం లక్షణాలు గుర్తించబడ్డాయి. దయచేసి వెంటనే సమీప అత్యవసర ఆసుపత్రికి వెళ్లండి."
        Language.KANNADA ->
            "ತುರ್ತು ಎಚ್ಚರಿಕೆ: ತೀವ್ರ ಸೋಂಕು ಅಥವಾ ಜ್ವರದ ಲಕ್ಷಣಗಳು ಕಂಡುಬಂದಿವೆ. ದಯವಿಟ್ಟು ತಕ್ಷಣವೇ ಹತ್ತಿರದ ತುರ್ತು ಆಸ್ಪತ್ರೆಗೆ ಭೇಟಿ ನೀಡಿ."
        Language.BENGALI ->
            "জরুরী সতর্কতা: গুরুতর সংক্রমণ বা জ্বরের লক্ষণ সনাক্ত হয়েছে। অবিলম্বে নিকটস্থ জরুরি বিভাগে বা হাসপাতালে যান।"
        Language.MARATHI ->
            "तातडीचा इशारा: गंभीर संसर्ग किंवा तापाची लक्षणे आढळली आहेत. कृपया त्वरित जवळच्या रुग्णालयात किंवा आपत्कालीन विभागात जावे."
        Language.MALAYALAM ->
            "അടിയന്തിര മുന്നറിയിപ്പ്: ഗുരുതരമായ അണുബാധ അല്ലെങ്കിൽ ഉയർന്ന പനിയുടെ ലക്ഷണങ്ങൾ കണ്ടെത്തിയിട്ടുണ്ട്. ദയവായി ഉടൻ തന്നെ അടുത്തുള്ള അത്യാഹിത വിഭാഗത്തിലോ ആശുപത്രിയിലോ ചികിത്സ തേടുക."
        Language.GUJARATI ->
            "તાકીદની ચેતવણી: ગંભીર ચેપ અથવા તાવના લક્ષણો જણાયા છે. કૃપા કરીને તાત્કાલિક નજીકની ઇમરજન્સી હોસ્પિટલમાં જાવ."
        Language.ENGLISH ->
            "EMERGENCY WARNING: Signs of systemic or rapidly progressing infection detected (such as fever or severe spread). Please seek immediate urgent medical care or visit the nearest emergency room immediately."
    }

    fun getDiagnosisDeflection(language: Language): String = when (language) {
        Language.HINDI ->
            "मैं एक डिजिटल ट्राइएज सहायक हूँ और चिकित्सा निदान (Diagnosis) नहीं कर सकता। केवल एक योग्य डॉक्टर ही आपके घाव की सही जांच कर सकते हैं। कृपया इस केस शीट को डॉक्टर को दिखाएं।"
        Language.TAMIL ->
            "நான் ஒரு கணினி ட்ரையേജ് உதவியாளர், மருத்துவ நோயறிதலை வழங்க முடியாது. தகுதியான மருத்துவர் மட்டுமே உங்கள் காயத்தை முழுமையாக பரிசோதிக்க முடியும்."
        Language.TELUGU ->
            "నేను ట్రయాజ్ సహాయకుడిని మాత్రమే, వైద్య నిర్ధారణ చేయలేను. అర్హత కలిగిన వైద్యుడు మాత్రమే మీ గాయాన్ని పరిశీలించి సరైన సలహా ఇవ్వగలరు."
        Language.KANNADA ->
            "ನಾನು ಟ್ರಯಾಜ್ ಸಹಾಯಕ ಮಾತ್ರ, ವೈದ್ಯಕೀಯ ರೋಗನಿರ್ಣಯ ನೀಡಲು ಸಾಧ್ಯವಿಲ್ಲ. ಅರ್ಹ ವೈದ್ಯರು ಮಾತ್ರ ನಿಮ್ಮ ಗಾಯವನ್ನು ಪರೀಕ್ಷಿಸಲು ಸಾಧ್ಯ."
        Language.BENGALI ->
            "আমি কেবল একটি প্রাথমিক ট্রায়াজ সহায়ক এবং কোনো রোগ নির্ণয় করতে পারি না। দয়া করে একজন অভিজ্ঞ চিকিৎসকের পরামর্শ নিন।"
        Language.MARATHI ->
            "मी केवळ एक ट्रायज सहाय्यक आहे आणि वैद्यकीय निदान करू शकत नाही. कृपया अचूक तपासणीसाठी योग्य डॉक्टरांचा सल्ला घ्या."
        Language.MALAYALAM ->
            "ഞാൻ ഒരു പ്രാഥമിക ട്രയാജ് സഹായി മാത്രമാണ്, രോഗനിർണയം നടത്താൻ കഴിയില്ല. ഒരു വിദഗ്ദ്ധ ഡോക്ടർക്ക് മാത്രമേ നിങ്ങളുടെ മുറിവ് ശരിയായി പരിശോധിക്കാൻ സാധിക്കൂ."
        Language.GUJARATI ->
            "હું માત્ર એક ટ્રાયઝ સહાયક છું અને તબીબી નિદાન કરી શકતો નથી. ફક્ત લાયક ડૉક્ટર જ તમારા ઘાની યોગ્ય તપાસ કરી શકે છે."
        Language.ENGLISH ->
            "I am an automated triage assistant and cannot diagnose medical conditions or predict clinical outcomes. Only a licensed doctor can assess your wound. We will generate a Case Sheet for your doctor's review."
    }

    fun getPrescriptionDeflection(language: Language): String = when (language) {
        Language.HINDI ->
            "मैं कोई दवा या एंटीबायोटिक नहीं लिख सकता और न ही घरेलू उपचार की सलाह दे सकता हूँ। बिना डॉक्टर की सलाह के दवा लेना खतरनाक हो सकता है। कृपया किसी डॉक्टर से मिलें।"
        Language.TAMIL ->
            "நான் மருந்துகள் அல்லது வீட்டு வைத்தியங்களை பரிந்துரைக்க முடியாது. மருத்துவரின் வழிகாட்டுதல் இன்றி மருந்துகளை உட்கொள்வது ஆபத்தானது."
        Language.TELUGU ->
            "నేను ఎలాంటి మందులు లేదా యాంటీబయాటిక్స్ సూచించలేను. దయచేసి డాక్టర్ ప్రిస్క్రిప్షన్ లేకుండా మందులు వాడవద్దు."
        Language.KANNADA ->
            "ನಾನು ಔಷಧಿಗಳನ್ನು ಅಥವಾ ಮನೆಮದ್ದುಗಳನ್ನು ಶಿಫಾರಸು ಮಾಡಲು ಸಾಧ್ಯವಿಲ್ಲ. ದಯವಿಟ್ಟು ವೈದ್ಯರನ್ನು ಭೇಟಿಯಾಗಿ ಸೂಕ್ತ ಚಿಕಿತ್ಸೆ ಪಡೆಯಿರಿ."
        Language.BENGALI ->
            "আমি কোনো অ্যান্টিবায়োটিক বা ওষুধ প্রেসক্রাইব করতে পারি না। ভুল ওষুধ মারাত্মক ক্ষতি করতে পারে, অবিলম্বে চিকিৎসকের পরামর্শ নিন।"
        Language.MARATHI ->
            "मी कोणतीही औषधे किंवा घरगुती उपाय सुचवू शकत नाही. कृपया डॉक्टरांच्या सल्ल्याशिवाय कोणतीही औषधे घेऊ नका."
        Language.MALAYALAM ->
            "എനിക്ക് മരുന്നുകളോ ആൻറിബയോട്ടിക്കുകളോ നിർദ്ദേശിക്കാൻ കഴിയില്ല. ഡോക്ടറുടെ നിർദ്ദേശമില്ലാതെ മരുന്നുകൾ കഴിക്കുന്നത് അപകടകരമാണ്."
        Language.GUJARATI ->
            "હું કોઈ દવા અથવા એન્ટિબાયોટિક લખી શકતો નથી. ડૉક્ટરની સલાહ વિના દવા લેવી જોખમી સાબિત થઈ શકે છે."
        Language.ENGLISH ->
            "I cannot prescribe antibiotics, medications, or home remedies. Self-treating diabetic wounds carries high risk of complications. Please consult a qualified doctor for appropriate treatment."
    }

    fun getUniversalReferralNotice(language: Language): String = when (language) {
        Language.HINDI ->
            "सार्वभौमिक अनुशंसा: यह ट्राइएज मूल्यांकन डॉक्टर के परामर्श का विकल्प नहीं है। हम दृढ़तापूर्वक अनुशंसा करते हैं कि आप व्यक्तिगत जांच और उपचार के लिए इस केस शीट के साथ एक योग्य चिकित्सक या पैर देखभाल विशेषज्ञ (Podiatrist) से मिलें।"
        Language.TAMIL ->
            "உலகளாவிய பரிந்துரை: இந்த ட்ரையേജ് மதிப்பீடு மருத்துவரின் பரிசோதனைக்கு மாற்றாகாது. விரிவான சிகிச்சைக்காக இந்த அறிக்கையுடன் உடனடியாக தகுதியான மருத்துவரை அணுகவும்."
        Language.TELUGU ->
            "సార్వత్రిక సిఫార్సు: ఈ ట్రయాజ్ సాధనం వైద్యుడి స్థానాన్ని భర్తీ చేయదు. వ్యక్తిగత పరిశీలన కొరకు ఈ కేస్ షీట్‌తో అర్హత గల వైద్యుడిని లేదా పాదాల సంరక్షణ నిపుణుడిని సంప్రదించండి."
        Language.KANNADA ->
            "ಸಾರ್ವತ್ರಿಕ ಶಿಫಾರಸು: ಈ ಟ್ರಯಾಜ್ ಮೌಲ್ಯಮಾಪನವು ವೈದ್ಯರ ಪರ್ಯಾಯವಲ್ಲ. ಸಂಪೂರ್ಣ ತಪಾಸಣೆಗಾಗಿ ಈ ಕೇಸ್ ಶೀಟ್‌ನೊಂದಿಗೆ ವೈದ್ಯರನ್ನು ಭೇಟಿ ಮಾಡಿ."
        Language.BENGALI ->
            "সার্বজনীন সুপারিশ: এই মূল্যায়ন ডাক্তারের বিকল্প নয়। সঠিক পরীক্ষা ও চিকিৎসার জন্য এই কেস শীট নিয়ে অবিলম্বে একজন ডাক্তারের সাথে পরামর্শ করুন।"
        Language.MARATHI ->
            "सर्वसमावेशक शिफारस: हे मूल्यांकन डॉक्टरांच्या सल्ल्याचा पर्याय नाही. प्रत्यक्ष तपासणीसाठी या केस शीटसह पात्र डॉक्टरांना भेटावे."
        Language.MALAYALAM ->
            "സാര്‍വത്രിക ശുപാർശ: ഈ ട്രയാജ് മൂല്യനിർണ്ണയം ഡോക്ടറുടെ പരിശോധനയ്ക്ക് പകരമാവില്ല. ശരിയായ പരിശോധനയ്ക്കായി ഈ കേസ് ഷീറ്റുമായി എത്രയും വേഗം ഡോക്ടറെ കാണുക."
        Language.GUJARATI ->
            "સાર્વત્રિક ભલામણ: આ મૂલ્યાંકન ડૉક્ટરની સલાહનો વિકલ્પ નથી. યોગ્ય તપાસ માટે આ કેસ શીટ સાથે લાયક ડૉક્ટરને મળો."
        Language.ENGLISH ->
            "UNIVERSAL REFERRAL: This triage evaluation is strictly informational and does not replace professional medical judgment. Regardless of score, we recommend presenting this Case Sheet and wound image to a qualified physician or foot care specialist for an in-person clinical examination."
    }
}
