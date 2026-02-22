package com.hooiv.habitflow.core.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// HabitFlow Brand Palette
// Built on Material 3 tonal system.
// Primary:   Indigo  — focus, trust, professionalism
// Secondary: Teal    — growth, sustainability, calm
// Tertiary:  Amber   — energy, achievement, warmth
// ---------------------------------------------------------------------------

// ── Primary (Indigo) ────────────────────────────────────────────────────────
val Indigo10  = Color(0xFF0A0657)
val Indigo20  = Color(0xFF1A1780)
val Indigo30  = Color(0xFF2B2882)
val Indigo40  = Color(0xFF3D3A8C)   // brand primary (light)
val Indigo80  = Color(0xFFC5C2FF)   // brand primary (dark)
val Indigo90  = Color(0xFFE6E4FF)   // primaryContainer (light)
val Indigo95  = Color(0xFFF3F2FF)

// ── Secondary (Teal) ────────────────────────────────────────────────────────
val Teal10    = Color(0xFF003D36)
val Teal20    = Color(0xFF00564E)
val Teal30    = Color(0xFF006B5E)
val Teal40    = Color(0xFF00796B)   // brand secondary (light)
val Teal80    = Color(0xFF80CBC4)   // brand secondary (dark)
val Teal90    = Color(0xFFB2DFDB)   // secondaryContainer (light)

// ── Tertiary (Amber) ────────────────────────────────────────────────────────
val Amber10   = Color(0xFF431407)
val Amber20   = Color(0xFF7A4100)
val Amber30   = Color(0xFF9A5300)
val Amber40   = Color(0xFFB45309)   // brand tertiary (light)
val Amber80   = Color(0xFFFCD34D)   // brand tertiary (dark)
val Amber90   = Color(0xFFFEF3C7)   // tertiaryContainer (light)

// ── Neutral (Indigo-tinted) ─────────────────────────────────────────────────
val Neutral10 = Color(0xFF1C1B2B)
val Neutral20 = Color(0xFF2A2940)
val Neutral90 = Color(0xFFE6E5F4)
val Neutral95 = Color(0xFFF3F2FA)
val Neutral99 = Color(0xFFFAFAFF)

// ── Neutral Variant ─────────────────────────────────────────────────────────
val NeutralVar30 = Color(0xFF46455A)
val NeutralVar50 = Color(0xFF78767E)
val NeutralVar80 = Color(0xFFC9C7D0)
val NeutralVar90 = Color(0xFFF0EFF9)

// ── Semantic: background / surface ──────────────────────────────────────────
val SurfaceLight  = Color(0xFFFFFFFF)
val SurfaceDark   = Color(0xFF1A1929)   // indigo-tinted dark surface
val BackgroundLight = Neutral99          // #FAFAFF
val BackgroundDark  = Color(0xFF0F0E1A) // very dark indigo-tinted black

// ── Semantic: streak / achievement ──────────────────────────────────────────
// Used only for meaningful data-ink — streak fire, leaderboard medals
val StreakLow     = Color(0xFFF59E0B)   // Amber 500   — 1–4 day streak
val StreakMedium  = Color(0xFFFB923C)   // Orange 400  — 5–9 day streak
val StreakHigh    = Color(0xFFEF4444)   // Red 500     — 10+ day streak

val AchievementGold   = Color(0xFFE8C842)  // warm gold
val AchievementSilver = Color(0xFFB0BEC5)  // cool silver
val AchievementBronze = Color(0xFFB87333)  // warm bronze

// ── Semantic: data-visualisation correlation ─────────────────────────────────
val CorrelationPositive = Color(0xFF22C55E)  // green-500
val CorrelationWeak     = Color(0xFFF59E0B)  // amber-500
val CorrelationNeutral  = Color(0xFF94A3B8)  // slate-400
val CorrelationNegative = Color(0xFFEF4444)  // red-500

// ── Status ────────────────────────────────────────────────────────────────────
val StatusSuccess = Color(0xFF22C55E)
val StatusWarning = Color(0xFFF59E0B)
val StatusError   = Color(0xFFEF4444)
val StatusInfo    = Color(0xFF3B82F6)
