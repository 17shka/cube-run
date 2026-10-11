package cube.run.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import android.graphics.fonts.FontStyle
import android.os.Build

/**
 * The candy-arcade look, in one place: colour tokens, the display font and
 * the tiny colour maths every widget shares. Bright pages sit on a
 * pink-to-sky gradient; anything on the 3D stage is white with an ink
 * outline; buttons are chunky slabs with a darker "lip" underneath.
 */
object Theme {
    // ---- surfaces
    const val PAGE_TOP = 0xFFFFE4F3.toInt()      // cotton candy…
    const val PAGE_BOTTOM = 0xFFD9F2FF.toInt()   // …down to baby blue
    const val CARD = 0xFFFFFFFF.toInt()
    const val SETTINGS_BLUE = 0xFFD9F2FF.toInt()
    const val CARD_ALT = 0xFFF4F0FF.toInt()
    const val SCRIM = 0x99140B3A.toInt()         // the pause / sheets: the run stays visible, dimmed

    // ---- ink
    const val INK = 0xFF2B1F5E.toInt()
    const val INK_SOFT = 0xFF5B4F8F.toInt()
    const val MUTED = 0xFF8F87B8.toInt()
    const val WHITE = 0xFFFFFFFF.toInt()

    // ---- candy
    const val PINK = 0xFFFF4FA3.toInt()
    const val BERRY = 0xFFFF3B6B.toInt()
    const val ORANGE = 0xFFFF8A3D.toInt()
    const val YELLOW = 0xFFFFD23F.toInt()
    const val GOLD = 0xFFFFB800.toInt()
    const val LIME = 0xFF7BE041.toInt()
    const val MINT = 0xFF2EE6A6.toInt()
    const val SKY = 0xFF3EC6FF.toInt()
    const val CYAN = 0xFF22D3EE.toInt()
    const val GRAPE = 0xFF9B5CFF.toInt()
    const val LAVENDER = 0xFFC9B3FF.toInt()

    // ---- what things are: one colour per concept, used everywhere it appears
    const val COIN = GOLD
    const val BUBBLE = CYAN
    const val BOX = GRAPE
    const val MAGNET = BERRY
    const val MULT = PINK
    const val JET = SKY
    const val PLAY = MINT
    const val RECORD = YELLOW

    /** Darken by [f] (0..1). */
    fun darken(c: Int, f: Float): Int = Color.argb(
        Color.alpha(c), (Color.red(c) * (1f - f)).toInt(), (Color.green(c) * (1f - f)).toInt(), (Color.blue(c) * (1f - f)).toInt(),
    )

    /** Lighten toward white by [f] (0..1). */
    fun lighten(c: Int, f: Float): Int = Color.argb(
        Color.alpha(c),
        (Color.red(c) + (255 - Color.red(c)) * f).toInt(),
        (Color.green(c) + (255 - Color.green(c)) * f).toInt(),
        (Color.blue(c) + (255 - Color.blue(c)) * f).toInt(),
    )

    fun alpha(c: Int, a: Int): Int = (c and 0x00FFFFFF) or (a.coerceIn(0, 255) shl 24)

    fun lerp(a: Int, b: Int, t: Float): Int {
        val k = t.coerceIn(0f, 1f)
        fun ch(x: Int, y: Int) = (x + (y - x) * k).toInt()
        return Color.argb(ch(Color.alpha(a), Color.alpha(b)), ch(Color.red(a), Color.red(b)), ch(Color.green(a), Color.green(b)), ch(Color.blue(a), Color.blue(b)))
    }

    /** Ink or white, whichever reads on [bg]. */
    fun onColor(bg: Int): Int {
        val l = (0.299f * Color.red(bg) + 0.587f * Color.green(bg) + 0.114f * Color.blue(bg)) / 255f
        return if (l > 0.62f) INK else WHITE
    }

    fun hsv(h: Float, s: Float = 0.78f, v: Float = 1f): Int =
        Color.HSVToColor(floatArrayOf(((h % 360f) + 360f) % 360f, s, v))
}

/** Fredoka for Latin/Hebrew, with bundled Fredoka One Cyrillic for Cyrillic glyphs. */
object Fonts {
    private val cache = HashMap<Pair<Boolean, Int>, Typeface>()

    /** [weight] 300..700. Android 9 needs a language-specific face instead of custom fallback. */
    fun get(ctx: Context, weight: Int, language: String = ctx.resources.configuration.locales[0].language): Typeface {
        val legacyCyrillic = Build.VERSION.SDK_INT < 29 && (language == "ru" || language == "uk")
        return cache.getOrPut(legacyCyrillic to weight) {
            runCatching {
                if (Build.VERSION.SDK_INT >= 29) withCyrillicFallback(ctx, weight) else {
                    val builder = Typeface.Builder(ctx.assets,
                        if (legacyCyrillic) "fonts/FredokaOneCyrillic-Regular.ttf" else "fonts/Fredoka.ttf")
                    if (!legacyCyrillic) builder.setFontVariationSettings("'wght' $weight, 'wdth' 100")
                    builder.build()
                }
            }.getOrNull() ?: if (weight >= 600) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    @android.annotation.TargetApi(29)
    private fun withCyrillicFallback(ctx: Context, weight: Int): Typeface {
        val primary = Font.Builder(ctx.assets, "fonts/Fredoka.ttf")
            .setFontVariationSettings("'wght' $weight, 'wdth' 100")
            .setWeight(weight).build()
        // The Cyrillic extension has one fixed style; use its original outlines at every UI weight.
        val cyrillic = Font.Builder(ctx.assets, "fonts/FredokaOneCyrillic-Regular.ttf")
            .setWeight(weight).build()
        return Typeface.CustomFallbackBuilder(FontFamily.Builder(primary).build())
            .addCustomFallback(FontFamily.Builder(cyrillic).build())
            .setStyle(FontStyle(weight, FontStyle.FONT_SLANT_UPRIGHT))
            .build()
    }
}
