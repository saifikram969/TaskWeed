package com.saif.scheduleapp.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saif.scheduleapp.domain.model.Category
import org.jetbrains.compose.resources.painterResource
import taskweed.shared.generated.resources.Res
import taskweed.shared.generated.resources.ic_category_jobs
import taskweed.shared.generated.resources.ic_category_learning
import taskweed.shared.generated.resources.ic_category_prayer
import taskweed.shared.generated.resources.ic_category_rest

@Composable
fun CategoryIcon(
    category: Category,
    modifier: Modifier = Modifier,
    tint: Color = categoryColor(category),
    size: Dp = 20.dp,
    contentDescription: String? = category.name
) {
    val drawableRes = when (category) {
        Category.LEARNING -> Res.drawable.ic_category_learning
        Category.JOBS -> Res.drawable.ic_category_jobs
        Category.PRAYER -> Res.drawable.ic_category_prayer
        Category.REST -> Res.drawable.ic_category_rest
    }

    Icon(
        painter = painterResource(drawableRes),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun CategoryTile(
    category: Category,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val catColor = categoryColor(category)
    val displayName = category.name.lowercase().replaceFirstChar { it.uppercase() }
    val shape = RoundedCornerShape(12.dp)

    val background = if (isSelected) {
        catColor.copy(alpha = 0.2f)
    } else {
        ScheduleTheme.colors.surface
    }

    val borderModifier = if (isSelected) {
        Modifier.border(1.dp, catColor, shape)
    } else {
        Modifier.border(1.dp, ScheduleTheme.colors.surfaceVariant, shape)
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(borderModifier)
            .background(background)
            .then(clickModifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                category = category,
                tint = if (isSelected) catColor else ScheduleTheme.colors.textMuted,
                size = 18.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = displayName,
                color = if (isSelected) catColor else ScheduleTheme.colors.textMuted,
                fontSize = 13.sp
            )
        }
    }
}

