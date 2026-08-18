package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.resthalflab.resthalfapp.core.design.RhSpacing

private val BUTTON_SIZE = 32.dp
private val ICON_SIZE = 16.dp

/**
 * A labelled −/value/+ counter, clamped to [[min], [max]]. Used by the guest-count picker.
 */
@Composable
fun RhStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    min: Int = 0,
    max: Int = 99,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RhSpacing.sm),
        ) {
            OutlinedIconButton(
                onClick = { onValueChange((value - 1).coerceAtLeast(min)) },
                enabled = value > min,
                modifier = Modifier.size(BUTTON_SIZE),
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = "Decrease $label", modifier = Modifier.size(ICON_SIZE))
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 24.dp),
            )
            OutlinedIconButton(
                onClick = { onValueChange((value + 1).coerceAtMost(max)) },
                enabled = value < max,
                modifier = Modifier.size(BUTTON_SIZE),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Increase $label", modifier = Modifier.size(ICON_SIZE))
            }
        }
    }
}
