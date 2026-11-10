package com.woundtriage.chat.domain.model

/**
 * SINBAD Classification System for Diabetic Foot Ulcers:
 * - Site: Forefoot (0) vs. Midfoot / Hindfoot (1)
 * - Ischemia: Intact blood flow / warm (0) vs. Reduced blood flow / cold / claudication (1)
 * - Neuropathy: Intact sensation (0) vs. Sensory loss / numbness / tingling (1)
 * - Bacterial Infection: None (0) vs. Clinical infection / purulence / cellulitis (1)
 * - Area: Ulcer < 1 cm² (0) vs. Ulcer ≥ 1 cm² (1)
 * - Depth: Superficial (0) vs. Deep / tendon / bone / joint (1)
 *
 * Total Score: 0 to 6 points.
 */
enum class SinbadDimension(
    val key: String,
    val title: String,
    val zeroDesc: String,
    val oneDesc: String,
    val clinicalImportance: String
) {
    SITE(
        key = "S",
        title = "Site",
        zeroDesc = "Forefoot (toes, metatarsal heads)",
        oneDesc = "Midfoot / Hindfoot (arch, heel)",
        clinicalImportance = "Ulcers on the hindfoot have higher risk of delayed healing and biomechanical complications."
    ),
    ISCHEMIA(
        key = "I",
        title = "Ischemia",
        zeroDesc = "Intact blood flow (warm foot, palpable pedal pulses)",
        oneDesc = "Reduced blood flow (cold foot, calf pain upon walking, weak pulses)",
        clinicalImportance = "Inadequate arterial perfusion drastically impairs tissue oxygenation and wound repair."
    ),
    NEUROPATHY(
        key = "N",
        title = "Neuropathy",
        zeroDesc = "Protective sensation intact",
        oneDesc = "Loss of protective sensation (numbness, pins-and-needles, inability to feel injury)",
        clinicalImportance = "Peripheral neuropathy prevents patients from feeling repetitive trauma or pressure necrosis."
    ),
    BACTERIAL_INFECTION(
        key = "B",
        title = "Bacterial Infection",
        zeroDesc = "No clinical signs of infection",
        oneDesc = "Infection present (purulent discharge, warmth, redness > 0.5cm, foul odor)",
        clinicalImportance = "Infection can rapidly spread to deeper tissue compartments and lead to systemic sepsis."
    ),
    AREA(
        key = "A",
        title = "Area",
        zeroDesc = "Ulcer surface area < 1 cm²",
        oneDesc = "Ulcer surface area ≥ 1 cm²",
        clinicalImportance = "Larger wounds have exponentially longer healing trajectories and increased risk of bacterial colonization."
    ),
    DEPTH(
        key = "D",
        title = "Depth",
        zeroDesc = "Superficial ulcer (confined to epidermis and dermis)",
        oneDesc = "Deep ulcer (reaching subcutis, tendon, muscle, joint capsule, or bone)",
        clinicalImportance = "Deep tissue involvement risks osteomyelitis and tenosynovitis requiring specialized surgical debridement."
    );

    companion object {
        val ORDERED_DIMENSIONS = listOf(SITE, ISCHEMIA, NEUROPATHY, BACTERIAL_INFECTION, AREA, DEPTH)
    }
}
