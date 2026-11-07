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
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val catColor = categoryColor(category)
    val shape = RoundedCornerShape(8.dp)

    val background = if (isSelected) {
        catColor.copy(alpha = 0.25f)
    } else {
        catColor.copy(alpha = 0.15f)
    }

    val borderModifier = if (isSelected) {
        Modifier.border(1.dp, catColor, shape)
    } else {
        Modifier.border(1.dp, catColor.copy(alpha = 0.3f), shape)
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(shape)
            .then(borderModifier)
            .background(background)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        CategoryIcon(
            category = category,
            tint = catColor,
            size = 18.dp
        )
    }
}

