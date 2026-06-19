package com.resthalflab.resthalfapp.feature.staff.ui

import com.resthalflab.resthalfapp.feature.search.api.SlotType

/** Localized stay-window label shared by the staff lists. */
val SlotType.label: String get() = when (this) {
    SlotType.HALF_DAY -> "Half Day"
    SlotType.FULL_DAY -> "Full Day"
}

/** Cap a string at [max] characters, appending an ellipsis when truncated. */
fun String.ellipsize(max: Int): String = if (length > max) take(max) + "…" else this
