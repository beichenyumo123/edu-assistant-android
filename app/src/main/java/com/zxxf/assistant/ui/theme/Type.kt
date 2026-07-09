package com.zxxf.assistant.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Responsive typography scale — 针对移动端（~340dp 气泡宽度）优化
// 字号相比 Material3 默认值收窄，避免小屏标题挤压换行
val Typography = Typography(
    // ── Headlines (h1–h3 等价) ──────────────────────────────────────────
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,         // 默认 32.sp → 24.sp，小屏不挤压
        lineHeight = 30.sp,       // 默认 40.sp → 30.sp
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,         // 默认 28.sp → 20.sp
        lineHeight = 26.sp,       // 默认 36.sp → 26.sp
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,         // 默认 24.sp → 17.sp
        lineHeight = 22.sp,       // 默认 32.sp → 22.sp
        letterSpacing = 0.sp
    ),

    // ── Titles (h4–h6 等价) ─────────────────────────────────────────────
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,         // 默认 22.sp → 18.sp
        lineHeight = 24.sp,       // 默认 28.sp → 24.sp
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,         // 默认 16.sp（保持，适合气泡）
        lineHeight = 22.sp,       // 默认 24.sp → 22.sp
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,         // 默认 14.sp（保持）
        lineHeight = 20.sp,       // 默认 20.sp
        letterSpacing = 0.sp
    ),

    // ── Body ─────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
