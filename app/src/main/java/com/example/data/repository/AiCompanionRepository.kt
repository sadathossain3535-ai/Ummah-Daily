package com.example.data.repository

import androidx.compose.runtime.Immutable

@Immutable
data class AiGroundingSource(
    val title: String,
    val uri: String,
    val sourceType: String // "Google Search" or "Google Maps"
)

@Immutable
data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isPlaceholderNotice: Boolean = false,
    val sources: List<AiGroundingSource> = emptyList(),
    val timestampMs: Long = System.currentTimeMillis()
)

enum class AiAssistantRole(
    val id: String,
    val titleBn: String,
    val systemInstruction: String
) {
    GENERAL_COMPANION(
        id = "general",
        titleBn = "ইসলামিক সহায়ক",
        systemInstruction = "আপনি একজন ভদ্র, প্রজ্ঞাবান বাংলা ইসলামিক সহায়ক। কুরআন ও সহিহ হাদিসের আলোকে সহজ বাংলায় উত্তর দিন।"
    ),
    FIQH_ADVISOR(
        id = "fiqh",
        titleBn = "নামাজ ও রোজা মাসআলা",
        systemInstruction = "আপনি নামাজ, রোজা, যাকাত ও দৈনন্দিন ইবাদতের মাসআলা বিষয়ে সহায়ক। রেফারেন্সসহ স্পষ্ট উত্তর দিন।"
    ),
    MOSQUE_GUIDE(
        id = "maps_guide",
        titleBn = "মসজিদ ও এলাকা গাইড",
        systemInstruction = "ব্যবহারকারীর জেলা ও এলাকার ভিত্তিতে নিকটস্থ মসজিদ ও ইসলামিক স্থানের তথ্য দিন।"
    )
}

/**
 * Single AI repository abstraction.
 * Currently backed by PlaceholderAiCompanionRepository because Firebase setup was declined.
 * Can be swapped to FirebaseAiCompanionRepository (Firebase.ai + Search/Maps grounding)
 * once Firebase AI Logic is enabled.
 */
interface AiCompanionRepository {
    val isPlaceholderMode: Boolean
    suspend fun sendMessage(
        history: List<AiChatMessage>,
        userMessage: String,
        role: AiAssistantRole,
        userLocationHint: String
    ): AiChatMessage
}

class PlaceholderAiCompanionRepository : AiCompanionRepository {

    override val isPlaceholderMode: Boolean = true

    override suspend fun sendMessage(
        history: List<AiChatMessage>,
        userMessage: String,
        role: AiAssistantRole,
        userLocationHint: String
    ): AiChatMessage {
        val lower = userMessage.lowercase()
        val (replyText, sources) = when {
            lower.contains("মসজিদ") || lower.contains("mosque") || role == AiAssistantRole.MOSQUE_GUIDE -> {
                val loc = userLocationHint.ifBlank { "ঢাকা, বাংলাদেশ" }
                buildString {
                    appendLine("[নমুনা উত্তর — Firebase AI Logic সংযুক্ত নয়]")
                    appendLine()
                    appendLine("আপনার নির্ধারিত এলাকা ($loc)-এর আশেপাশের মসজিদ খুঁজতে নিচের Google Maps বাটন ব্যবহার করুন অথবা টুলস ট্যাব থেকে 'নিকটস্থ মসজিদ' খুলুন।")
                    appendLine()
                    append("রিয়েল-টাইম Google Maps গ্রাউন্ডিং উত্তর পেতে Firebase সেটআপ সক্রিয় করুন।")
                } to listOf(
                    AiGroundingSource(
                        title = "Google Maps — $loc মসজিদ অনুসন্ধান",
                        uri = "https://www.google.com/maps/search/?api=1&query=mosque+near+$loc",
                        sourceType = "Google Maps"
                    )
                )
            }

            lower.contains("নামাজ") || lower.contains("দোয়া") || lower.contains("রোজা") || role == AiAssistantRole.FIQH_ADVISOR -> {
                buildString {
                    appendLine("[নমুনা উত্তর — Firebase AI Logic সংযুক্ত নয়]")
                    appendLine()
                    appendLine("আপনার প্রশ্ন: “$userMessage”")
                    appendLine()
                    appendLine("নমুনা পরামর্শ: প্রতিদিনের ৫ ওয়াক্ত ফরজ নামাজের পাশাপাশি সুন্নতে মুয়াক্কাদা এবং সকাল-সন্ধ্যার মাসনুন যিকির নিয়মিত আদায় করা উত্তম। এই অ্যাপের 'হোম' ট্যাবে আজকের নামাজের ওয়াক্ত ও রাকাত বিবরণী এবং 'দোয়া' ট্যাবে বিস্তারিত দোয়া রয়েছে।")
                } to listOf(
                    AiGroundingSource(
                        title = "সহিহ সূত্র রেফারেন্স (নমুনা সার্চ গ্রাউন্ডিং)",
                        uri = "https://www.google.com/search?q=islam+prayer+guide",
                        sourceType = "Google Search"
                    )
                )
            }

            else -> {
                buildString {
                    appendLine("[নমুনা উত্তর — Firebase AI Logic সংযুক্ত নয়]")
                    appendLine()
                    appendLine("আসসালামু আলাইকুম! আপনি জিজ্ঞাসা করেছেন: “$userMessage”।")
                    appendLine()
                    appendLine("বর্তমানে এই চ্যাটবটটি প্লেসহোল্ডার মোডে চলছে কারণ Firebase সেটআপ অনুমোদিত হয়নি। Firebase AI Logic চালু করলে এখানে Gemini ও Google Search গ্রাউন্ডিংয়ের মাধ্যমে লাইভ ইসলামিক প্রশ্নোত্তর পাওয়া যাবে।")
                } to listOf(
                    AiGroundingSource(
                        title = "Google Search Grounding (নমুনা)",
                        uri = "https://www.google.com/search?q=islamic+knowledge",
                        sourceType = "Google Search"
                    )
                )
            }
        }

        return AiChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            isUser = false,
            text = replyText,
            isPlaceholderNotice = true,
            sources = sources
        )
    }
}
