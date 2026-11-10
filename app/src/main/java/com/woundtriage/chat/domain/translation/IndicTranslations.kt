package com.woundtriage.chat.domain.translation

import com.woundtriage.chat.domain.model.Language
import com.woundtriage.chat.domain.model.SinbadDimension

object IndicTranslations {

    /**
     * Empathetic initial greeting in the chosen language.
     */
    fun getGreeting(language: Language): String = when (language) {
        Language.HINDI ->
            "नमस्ते। मैं आपका घाव देखभाल ट्राइएज सहायक हूँ। आपके घाव की तस्वीर मिल गई है। डॉक्टर के लिए एक संपूर्ण केस शीट तैयार करने हेतु मैं आपसे 6 छोटे और सरल प्रश्न पूछूँगा। चिंता न करें, हम इसे साथ मिलकर करेंगे।"
        Language.TAMIL ->
            "வணக்கம். நான் உங்கள் காயம் ட்ரையേജ് உதவியாளர். உங்கள் காயத்தின் புகைப்படம் பெறப்பட்டது. மருத்துவருக்கான அறிக்கையைத் தயாரிக்க 6 எளிய கேள்விகளைக் கேட்கிறேன்."
        Language.TELUGU ->
            "నమస్కారం. నేను మీ గాయం సంరక్షణ సహాయకుడిని. మీ గాయం ఫోటో అందింది. డాక్టర్ కోసం కేస్ షీట్ సిద్ధం చేయడానికి నేను 6 సాధారణ ప్రశ్నలు అడుగుతాను."
        Language.KANNADA ->
            "ನಮಸ್ಕಾರ. ನಾನು ನಿಮ್ಮ ಗಾಯದ ಆರೈಕೆ ಸಹಾಯಕ. ನಿಮ್ಮ ಗಾಯದ ಫೋಟೋ ಲಭ್ಯವಾಗಿದೆ. ವೈದ್ಯರಿಗಾಗಿ ಕೇಸ್ ಶೀಟ್ ಸಿದ್ಧಪಡಿಸಲು ನಾನು 6 ಸರಳ ಪ್ರಶ್ನೆಗಳನ್ನು ಕೇಳುತ್ತೇನೆ."
        Language.BENGALI ->
            "নমস্কার। আমি আপনার ক্ষত যত্ন ট্রায়াজ সহকারী। আপনার ক্ষতের ছবি পেয়েছি। ডাক্তারের জন্য সম্পূর্ণ রিপোর্ট তৈরির জন্য আমি ৬টি সহজ প্রশ্ন জিজ্ঞাসা করব।"
        Language.MARATHI ->
            "नमस्कार. मी तुमचा जखम काळजी ट्रायज सहाय्यक आहे. तुमच्या जखमेचा फोटो प्राप्त झाला आहे. डॉक्टरांसाठी केस शीट तयार करण्यासाठी मी 6 सोपे प्रश्न विचारणार आहे."
        Language.MALAYALAM ->
            "നമസ്കാരം. ഞാൻ നിങ്ങളുടെ മുറിവ് പരിചരണ ട്രയാജ് സഹായിയാണ്. നിങ്ങളുടെ മുറിവിന്റെ ഫോട്ടോ ലഭിച്ചു. ഡോക്ടർക്ക് കാണിക്കാൻ ഒരു കേസ് ഷീറ്റ് തയ്യാറാക്കാൻ ഞാൻ 6 ലളിതമായ ചോദ്യങ്ങൾ ചോദിക്കാം."
        Language.GUJARATI ->
            "નમસ્તે. હું તમારો ઘા સંભાળ ટ્રાયઝ સહાયક છું. તમારા ઘાનો ફોટો મળી ગયો છે. ડૉક્ટર માટે સંપૂર્ણ કેસ શીટ તૈયાર કરવા માટે હું તમને 6 સરળ પ્રશ્નો પૂછીશ."
        Language.ENGLISH ->
            "Hello. I am your wound triage assistant. I have received your wound image. To help prepare a thorough Case Sheet for your doctor, I will ask you 6 quick, straightforward questions about the wound. Let's begin."
    }

    /**
     * Conversational question for a specific SINBAD dimension in the selected language.
     */
    fun getQuestion(dimension: SinbadDimension, language: Language, @Suppress("UNUSED_PARAMETER") classifierHint: String? = null): String {
        return when (dimension) {
            SinbadDimension.SITE -> when (language) {
                Language.HINDI -> "यह घाव आपके पैर के किस हिस्से में है? क्या यह पैर की उंगलियों/सामने के हिस्से पर है, या तलवे के बीच/एड़ी पर?"
                Language.TAMIL -> "இந்த காயம் உங்கள் காலில் எங்குள்ளது? விரல்கள் அல்லது முன்பகுதியிலா, அல்லது நடுப்பகுதி/குதிக்காலிலா?"
                Language.TELUGU -> "ఈ గాయం మీ కాలుపై ఎక్కడ ఉంది? కాలి వేళ్ళు/ముందు భాగంలోనా, లేదా మడమ/అరికాలు మధ్య భాగంలోనా?"
                Language.KANNADA -> "ಈ ಗಾಯ ನಿಮ್ಮ ಕಾಲಿನಲ್ಲಿ ಎಲ್ಲಿದೆ? ಕಾಲ್ಬೆರಳುಗಳಲ್ಲೇ ಅಥವಾ ಹಿಮ್ಮಡಿ/ಅಂಗಾಲಿನಲ್ಲೇ?"
                Language.BENGALI -> "এই ক্ষতটি আপনার পায়ের ঠিক কোথায়? পায়ের আঙুল/সামনে, নাকি পায়ের পাতা/গোড়ালিতে?"
                Language.MARATHI -> "ही जखम तुमच्या पायावर नेमकी कुठे आहे? पायाच्या बोटांवर/पुढील भागात की टाच/तळपायाच्या मध्यभागी?"
                Language.MALAYALAM -> "ഈ മുറിവ് നിങ്ങളുടെ കാലിൽ എവിടെയാണ്? കാൽവിരലുകളിലോ മുൻഭാഗത്തോ ആണോ, അതോ ഉപ്പൂറ്റിയിലോ പാദത്തിന്റെ അടിയിലോ ആണോ?"
                Language.GUJARATI -> "આ ઘા તમારા પગ પર ક્યાં છે? પગની આંગળીઓ પર/આગળના ભાગમાં છે, કે પછી એડી/તળિયાના ભાગમાં છે?"
                Language.ENGLISH -> "Where exactly is this wound situated on your foot? Is it on your toes or front of the foot, or is it on the arch, sole, or heel?"
            }

            SinbadDimension.ISCHEMIA -> when (language) {
                Language.HINDI -> "क्या आपका वह पैर या पंजा अक्सर ठंडा महसूस होता है, या चलते समय आपकी पिंडलियों में दर्द या ऐंठन होती है?"
                Language.TAMIL -> "உங்கள் கால் அடிக்கடி வழக்கத்திற்கு மாறாக குளிர்ந்து போகிறதா, அல்லது நடக்கும்போது கெண்டைக்கால் வலி ஏற்படுகிறதா?"
                Language.TELUGU -> "మీ కాలు తరచుగా చల్లగా అనిపిస్తుందా, లేదా నడిచేటప్పుడు పిక్కలలో నొప్పి/తిమ్మిరి వస్తుందా?"
                Language.KANNADA -> "ನಿಮ್ಮ ಕಾಲು ಅತಿಯಾಗಿ ತಂಪಾಗಿದೆಯೇ ಅಥವಾ ನಡೆಯುವಾಗ ಕಾಲಿನ ಸ್ನಾಯುಗಳಲ್ಲಿ ನೋವು ಬರುತ್ತಿದೆಯೇ?"
                Language.BENGALI -> "আপনার পা কি অস্বাভাবিক ঠান্ডা লাগে, অথবা হাঁটার সময় পায়ে টান বা তীব্র ব্যথা অনুভূত হয়?"
                Language.MARATHI -> "तुमचा तो पाय नेहमीपेक्षा थंड पडतो का, किंवा चालताना पोटरीमध्ये पेटके किंवा वेदना होतात का?"
                Language.MALAYALAM -> "നിങ്ങളുടെ ആ കാൽ സാധാരണയേക്കാൾ തണുപ്പായി തോന്നുന്നുണ്ടോ, അല്ലെങ്കിൽ നടക്കുമ്പോൾ പേശികളിൽ വേദനയോ കോച്ചിലോ ഉണ്ടാകാറുണ്ടോ?"
                Language.GUJARATI -> "શું તમારો તે પગ સામાન્ય કરતાં ઠંડો લાગે છે, અથવા ચાલતી વખતે પિંડીઓમાં દુખાવો કે ખેંચાણ થાય છે?"
                Language.ENGLISH -> "Does your foot often feel noticeably cold to touch, or do you experience cramping or pain in your calf muscles when walking?"
            }

            SinbadDimension.NEUROPATHY -> when (language) {
                Language.HINDI -> "क्या घाव वाले पैर में आपको सुन्नपन, झनझनाहट (सुई चुभने जैसा अहसास) या छूने पर महसूस न होना जैसा लगता है?"
                Language.TAMIL -> "காயமடைந்த காலில் மரத்துப்போதல், ஊசி குத்துவது போன்ற உணர்வு அல்லது தொடு உணர்வு குறைந்துள்ளதா?"
                Language.TELUGU -> "గాయం ఉన్న పాదంలో తిమ్మిరి, సూదులు గుచ్చినట్లు ఉండటం లేదా స్పర్శ సరిగా తెలియకపోవడం జరుగుతోందా?"
                Language.KANNADA -> "ಗಾಯವಿರುವ ಕಾಲಿನಲ್ಲಿ ಮರಗಟ್ಟುವಿಕೆ, ಮುಳ್ಳು ಚುಚ್ಚಿದಂತೆ ಅನಿಸುವುದು ಅಥವಾ ಸ್ಪರ್ಶ ತಿಳಿಯದಿರುವುದು ಇದೆಯೇ?"
                Language.BENGALI -> "ক্ষতযুক্ত পায়ে কি অসাড়তা, ঝিঁঝিঁ ধরা বা স্পর্শ করলে সঠিকভাবে অনুভব করতে পারছেন না?"
                Language.MARATHI -> "जखम असलेल्या पायात बधिरपणा, मुंग्या येणे किंवा स्पर्श न जाणवणे असा त्रास होतो का?"
                Language.MALAYALAM -> "മുറിവുള്ള കാലിൽ മരവിപ്പ്, തരിപ്പ് (സൂചി കുത്തുന്നതുപോലെയുള്ള അനുഭവം), അല്ലെങ്കിൽ സ്പർശനം അറിയാത്ത അവസ്ഥ ഉണ്ടോ?"
                Language.GUJARATI -> "શું ઘા વાળા પગમાં ખાલી ચડવી, ઝણઝણાટી કે સ્પર્શ ન સમજાવા જેવી સમસ્યા છે?"
                Language.ENGLISH -> "In that foot, do you experience any numbness, burning, pins-and-needles, or reduced ability to feel sensations when touched?"
            }

            SinbadDimension.BACTERIAL_INFECTION -> when (language) {
                Language.HINDI -> "क्या घाव के आसपास तेज लाली, सूजन, बहुत गर्मी, दुर्गंध या मवाद (पीव) निकल रहा है?"
                Language.TAMIL -> "காயத்தைச் சுற்றி கடுமையான சிவத்தல், வீக்கம், வெப்பம், துர்நாற்றம் அல்லது சீழ் வடிகிறதா?"
                Language.TELUGU -> "గాయం చుట్టూ ఎరుపుదనం, వాపు, వేడి, దుర్వాసన లేదా చీము కారడం లాంటివి ఉన్నాయా?"
                Language.KANNADA -> "ಗಾಯದ ಸುತ್ತಲೂ ಕೆಂಪಾಗುವುದು, ಊತ, ವಿಪರೀತ ಬಿಸಿ, ವಾಸನೆ ಅಥವಾ ಕೀವು ಬರುತ್ತಿದೆಯೇ?"
                Language.BENGALI -> "ক্ষতের চারপাশে কি অতিরিক্ত লালচে ভাব, ফোলা, গরম অনুভূতি, দুর্গন্ধ বা পুঁজ বের হচ্ছে?"
                Language.MARATHI -> "जखमेभोवती खूप लालसरपणा, सूज, उष्णता, दुर्गंधी किंवा पू येत आहे का?"
                Language.MALAYALAM -> "മുറിവിന് ചുറ്റും കടുത്ത ചുവപ്പ്, വീക്കം, ചൂട്, ദുർഗന്ധം അല്ലെങ്കിൽ പഴുപ്പ് കാണപ്പെടുന്നുണ്ടോ?"
                Language.GUJARATI -> "શું ઘાની આસપાસ ખૂબ લાલાશ, સોજો, ગરમી, દુર્ગંધ કે પરુ વહી રહ્યું છે?"
                Language.ENGLISH -> "Do you notice swelling, significant warmth, expanding redness, a bad odor, or any pus or fluid oozing from the wound?"
            }

            SinbadDimension.AREA -> when (language) {
                Language.HINDI -> "घाव का आकार लगभग कितना है? क्या यह 1 रुपये के सिक्के या उंगली के पोर से छोटा है, या उससे बड़ा है?"
                Language.TAMIL -> "காயத்தின் அளவு தோராயமாக எவ்வளவு? ஒரு நாணயத்தை விட சிறியதா அல்லது பெரியதா?"
                Language.TELUGU -> "గాయం పరిమాణం సుమారు ఎంత ఉంటుంది? నాణెం లేదా వేలికొన కంటే చిన్నదా, లేదా పెద్దదా?"
                Language.KANNADA -> "ಗಾಯದ ಗಾತ್ರ ಸರಿಸುಮಾರು ಎಷ್ಟಿದೆ? ಒಂದು ನಾಣ್ಯಕ್ಕಿಂತ ಚಿಕ್ಕದಾಗಿದೆಯೇ ಅಥವಾ ದೊಡ್ಡದಾಗಿದೆಯೇ?"
                Language.BENGALI -> "ক্ষতের আকার আনুমানিক কতটা? একটি ছোট মুদ্রার চেয়ে ছোট, নাকি তার চেয়ে বড়?"
                Language.MARATHI -> "जखमेचा आकार साधारण किती आहे? एका नाण्यापेक्षा किंवा बोटाच्या पेऱ्यापेक्षा लहान आहे की मोठा?"
                Language.MALAYALAM -> "മുറിവിന്റെ വലിപ്പം ഏകദേശം എത്രയാണ്? ഒരു ചെറിയ നാണയത്തേക്കാൾ ചെറുതാണോ, അതോ വലുതാണോ?"
                Language.GUJARATI -> "ઘાનું કદ અંદાજે કેટલું છે? 1 રૂપિયાના સિક્કા કરતાં નાનું છે, કે તેનાથી મોટું છે?"
                Language.ENGLISH -> "Roughly how large is the open wound? Is it smaller than a standard coin (fingertip size / under 1 cm²), or is it larger?"
            }

            SinbadDimension.DEPTH -> when (language) {
                Language.HINDI -> "क्या यह केवल त्वचा की ऊपरी सतह पर एक खरोंच जैसा घाव है, या यह काफी गहरा है जहां अंदर का मांस, नस या हड्डी दिखाई दे रही है?"
                Language.TAMIL -> "இது தோலின் மேற்பரப்பில் மட்டுமே உள்ளதா, அல்லது ஆழமாக எலும்பு அல்லது தசை வரை தெரிகிறதா?"
                Language.TELUGU -> "ఇది కేవలం పై చర్మంపై మాత్రమే ఉందా, లేదా లోతుగా ఎముక లేదా కండరాలు కనిపించేలా ఉందా?"
                Language.KANNADA -> "ಇದು ಚರ್ಮದ ಮೇಲ್ಭಾಗದಲ್ಲಿ ಮಾತ್ರವಿದೆಯೇ, ಅಥವಾ ಆಳವಾಗಿ ಮೂಳೆ ಅಥವಾ ಸ್ನಾಯು ಕಾಣಿಸುತ್ತಿದೆಯೇ?"
                Language.BENGALI -> "এটি কি কেবল চামড়ার ওপরের ক্ষত, নাকি বেশ গভীর যেখানে হাড় বা গভীর টিস্যু দেখা যাচ্ছে?"
                Language.MARATHI -> "ही फक्त त्वचेवरची वरवरची जखम आहे, की खूप खोल जाऊन आतले स्नायू किंवा हाड दिसत आहे?"
                Language.MALAYALAM -> "ഇത് തൊലിയുടെ മുകൾഭാഗത്തുള്ള മുറിവാണോ, അതോ മാംസവും അസ്ഥിയും കാണത്തക്കവിധം ആഴത്തിലുള്ളതാണോ?"
                Language.GUJARATI -> "શું આ ફક્ત ચામડી પરનો સપાટીનો ઘા છે, કે ખૂબ ઊંડો છે જેમાં માંસ અથવા હાડકું દેખાય છે?"
                Language.ENGLISH -> "Is this a surface-level skin sore, or does it feel deep into the flesh, potentially exposing tendon or bone?"
            }
        }
    }

    /**
     * Interprets a patient's response into a clinical SINBAD score (0 or 1).
     * Returns a Pair(score: Int, explanation: String).
     */
    fun parsePatientResponse(dimension: SinbadDimension, reply: String): Pair<Int, String> {
        val lower = reply.lowercase().trim()

        val positiveSigns = listOf(
            "yes", "yeah", "yup", "yep", "true", "ha", "haan", "correct", "right",
            "cold", "numb", "tingling", "burn", "pus", "swelling", "red", "smell",
            "big", "large", "bigger", "deep", "bone", "tendon", "heel", "sole", "arch",
            "हाँ", "हाँजी", "ठंडा", "सुन्न", "मवाद", "बड़ा", "गहरा", "हड्डी", "एड़ी", "तलवा",
            "ஆம்", "ஆமாம்", "குளிர்", "மரத்து", "சீழ்", "பெரிய", "ஆழம்", "எலும்பு", "குதிக்கால்",
            "అవును", "చల్లగా", "తిమ్మిరి", "చీము", "పెద్దది", "లోతు", "ఎముక", "మడమ",
            "ಹೌದು", "ತಂಪು", "ಮರಗಟ್ಟುವಿಕೆ", "ಕೀವು", "ದೊಡ್ಡದು", "ಆಳ", "ಮೂಳೆ", "ಹಿಮ್ಮಡಿ",
            "হ্যাঁ", "ঠান্ডা", "অসাড়", "পুঁজ", "বড়", "গভীর", "হাড়", "গোড়ালি",
            "होय", "थंड", "बधिर", "पू", "मोठी", "खोल", "हाड", "टाच",
            "അതെ", "ഉണ്ട്", "തണുപ്പ്", "മരവിപ്പ്", "പഴുപ്പ്", "വലുത്", "ആഴം", "അസ്ഥി", "ഉപ്പൂറ്റി",
            "હા", "હાજી", "ઠંડુ", "ખાલી", "પરુ", "મોટો", "ઊંડો", "હાડકું", "એડી"
        )

        val negativeSigns = listOf(
            "no", "nah", "nope", "not", "warm", "normal", "feel fine", "surface", "small", "toes", "front",
            "नहीं", "गर्म", "सामान्य", "छोटा", "उंगली", "सतही",
            "இல்லை", "இயல்பு", "சிறிய", "விரல்",
            "లేదు", "సాధారణ", "చిన్నది", "వేలు",
            "ಇಲ್ಲ", "ಸಾಮಾನ್ಯ", "ಸಣ್ಣದು", "ಬೆರಳು",
            "না", "স্বাভাবিক", "ছোট", "আঙুল",
            "नाही", "सामान्य", "लहान", "बोट",
            "ഇല്ല", "ചൂട്", "സാധാരണ", "ചെറുത്", "വിരൽ",
            "ના", "નથી", "ગરમ", "સામાન્ય", "નાનો", "આંગળી", "સપાટી"
        )

        val posMatches = positiveSigns.count { lower.contains(it) }
        val negMatches = negativeSigns.count { lower.contains(it) }

        return when (dimension) {
            SinbadDimension.SITE -> {
                // 0 = Forefoot (toes), 1 = Midfoot/Hindfoot (heel, sole)
                if (lower.contains("toe") || lower.contains("front") || lower.contains("उंगली") || lower.contains("விரல்") || lower.contains("వేలు") || lower.contains("വിരൽ") || lower.contains("આંગળી")) {
                    Pair(0, "Forefoot (toes/metatarsals) confirmed by patient")
                } else if (lower.contains("heel") || lower.contains("sole") || lower.contains("arch") || lower.contains("एड़ी") || lower.contains("तलवा") || lower.contains("குதிக்கால்") || lower.contains("మడమ") || lower.contains("ഉപ്പൂറ്റി") || lower.contains("એડી")) {
                    Pair(1, "Midfoot / Hindfoot location confirmed by patient")
                } else if (posMatches > negMatches) {
                    Pair(1, "Patient indicated midfoot / hindfoot area")
                } else {
                    Pair(0, "Patient indicated forefoot location")
                }
            }

            SinbadDimension.ISCHEMIA -> {
                // 0 = Intact (warm), 1 = Impaired (cold, claudication)
                if (posMatches > negMatches) {
                    Pair(1, "Symptoms of decreased peripheral perfusion / coldness reported")
                } else {
                    Pair(0, "No symptoms of ischemia reported; warmth intact")
                }
            }

            SinbadDimension.NEUROPATHY -> {
                // 0 = Intact, 1 = Sensory loss / numbness
                if (posMatches > negMatches) {
                    Pair(1, "Peripheral sensory loss / numbness / tingling confirmed")
                } else {
                    Pair(0, "Protective sensation reported intact")
                }
            }

            SinbadDimension.BACTERIAL_INFECTION -> {
                // 0 = None, 1 = Infection signs
                if (posMatches > negMatches) {
                    Pair(1, "Signs of local bacterial infection (erythema/swelling/pus) reported")
                } else {
                    Pair(0, "No prominent clinical signs of infection reported")
                }
            }

            SinbadDimension.AREA -> {
                // 0 = < 1cm², 1 = ≥ 1cm²
                if (lower.contains("small") || lower.contains("coin") && lower.contains("smaller") || lower.contains("छोटा") || lower.contains("சிறிய") || lower.contains("చిన్న") || lower.contains("ചെറുത്") || lower.contains("નાનો")) {
                    Pair(0, "Ulcer area reported under 1 cm²")
                } else if (lower.contains("big") || lower.contains("large") || lower.contains("बड़ा") || lower.contains("பெரிய") || lower.contains("పెద్ద") || lower.contains("വലുത്") || lower.contains("મોટો")) {
                    Pair(1, "Ulcer area reported ≥ 1 cm²")
                } else if (posMatches > negMatches) {
                    Pair(1, "Ulcer area reported ≥ 1 cm²")
                } else {
                    Pair(0, "Ulcer area reported under 1 cm²")
                }
            }

            SinbadDimension.DEPTH -> {
                // 0 = Superficial, 1 = Deep (tendon/bone)
                if (lower.contains("deep") || lower.contains("bone") || lower.contains("tendon") || lower.contains("गहरा") || lower.contains("हड्डी") || lower.contains("ஆழம்") || lower.contains("లోతు") || lower.contains("ആഴം") || lower.contains("ઊંડો")) {
                    Pair(1, "Deep ulcer involvement (tendon/bone/subcutis) reported")
                } else if (lower.contains("surface") || lower.contains("superficial") || lower.contains("skin") || lower.contains("सतही") || lower.contains("સપાટી")) {
                    Pair(0, "Superficial ulcer confined to epidermis/dermis")
                } else if (posMatches > negMatches) {
                    Pair(1, "Deep ulcer characteristics indicated")
                } else {
                    Pair(0, "Superficial ulcer characteristics indicated")
                }
            }
        }
    }
}
