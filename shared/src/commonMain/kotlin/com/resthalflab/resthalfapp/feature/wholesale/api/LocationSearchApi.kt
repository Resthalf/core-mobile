package com.resthalflab.resthalfapp.feature.wholesale.api

import com.resthalflab.resthalfapp.core.domain.AppResult

/**
 * Public surface of the wholesale feature. Other features (e.g. search's home planner) depend only
 * on this, per the architecture's feature-boundary rule.
 */
interface LocationSearchApi {
    /** Location autosuggest for the given [term]. Returns all types; callers filter as needed. */
    suspend fun autosuggest(term: String): AppResult<List<LocationSuggestion>>
}
