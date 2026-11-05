package com.saif.scheduleapp.core.designsystem

import androidx.compose.ui.unit.dp
import com.saif.scheduleapp.domain.model.Category
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesignSystemTest {

    @Test
    fun categoryColor_maps_all_categories_correctly() {
        assertEquals(CategoryLearning, categoryColor(Category.LEARNING))
        assertEquals(CategoryJobs, categoryColor(Category.JOBS))
        assertEquals(CategoryPrayer, categoryColor(Category.PRAYER))
        assertEquals(CategoryRest, categoryColor(Category.REST))
    }

    @Test
    fun scheduleColors_contains_required_tokens() {
        assertTrue(DarkScheduleColors.isDark)
        assertFalse(LightScheduleColors.isDark)

        assertEquals(DarkBackground, DarkScheduleColors.background)
        assertEquals(DarkSurface, DarkScheduleColors.surface)
        assertEquals(DarkSurfaceVariant, DarkScheduleColors.surfaceVariant)
        assertEquals(DarkPrimary, DarkScheduleColors.primary)
        assertEquals(DarkPrimaryMuted, DarkScheduleColors.primaryMuted)
        assertEquals(DarkAccent, DarkScheduleColors.accent)
        assertEquals(DarkWarning, DarkScheduleColors.warning)
        assertEquals(DarkText, DarkScheduleColors.text)
        assertEquals(DarkTextMuted, DarkScheduleColors.textMuted)
        assertEquals(DarkDanger, DarkScheduleColors.danger)
        assertEquals(DarkDangerContainer, DarkScheduleColors.dangerContainer)

        assertEquals(LightBackground, LightScheduleColors.background)
        assertEquals(LightSurface, LightScheduleColors.surface)
        assertEquals(LightSurfaceVariant, LightScheduleColors.surfaceVariant)
        assertEquals(LightPrimary, LightScheduleColors.primary)
        assertEquals(LightPrimaryMuted, LightScheduleColors.primaryMuted)
        assertEquals(LightAccent, LightScheduleColors.accent)
        assertEquals(LightWarning, LightScheduleColors.warning)
        assertEquals(LightText, LightScheduleColors.text)
        assertEquals(LightTextMuted, LightScheduleColors.textMuted)
        assertEquals(LightDanger, LightScheduleColors.danger)
        assertEquals(LightDangerContainer, LightScheduleColors.dangerContainer)
    }

    @Test
    fun scheduleSpacing_has_exact_values() {
        val spacing = ScheduleSpacing()
        assertEquals(4.dp, spacing.space4)
        assertEquals(8.dp, spacing.space8)
        assertEquals(12.dp, spacing.space12)
        assertEquals(16.dp, spacing.space16)
        assertEquals(24.dp, spacing.space24)
        assertEquals(32.dp, spacing.space32)

        assertEquals(4.dp, Space4)
        assertEquals(8.dp, Space8)
        assertEquals(12.dp, Space12)
        assertEquals(16.dp, Space16)
        assertEquals(24.dp, Space24)
        assertEquals(32.dp, Space32)
    }
}

