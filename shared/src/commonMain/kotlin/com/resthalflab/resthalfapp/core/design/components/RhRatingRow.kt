package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.resthalflab.resthalfapp.core.design.RhStarGold
import com.resthalflab.resthalfapp.core.domain.formatCompactCount

/**
 * Gold star + bold rating + parenthesised review count.
 * Pass [reviewWord] (e.g. "reviews") to render "(1.2K reviews)" instead of just "(1.2K)".
 */
@Composable
fun RhRatingRow(
    rating: Double,
    reviews: Int,
    modifier: Modifier = Modifier,
    reviewWord: String? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = RhStarGold, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(rating.toString()) }
                val suffix = reviewWord?.let { " $it" } ?: ""
                append("  (${formatCompactCount(reviews)}$suffix)")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
