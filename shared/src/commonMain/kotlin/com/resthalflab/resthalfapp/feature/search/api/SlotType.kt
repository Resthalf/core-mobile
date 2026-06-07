package com.resthalflab.resthalfapp.feature.search.api

import kotlinx.serialization.Serializable

/** Stay-window options. HALF_DAY = 12h night stay (00:00–12:00); FULL_DAY = 24h (12:00–12:00 +1). */
@Serializable
enum class SlotType { HALF_DAY, FULL_DAY }
