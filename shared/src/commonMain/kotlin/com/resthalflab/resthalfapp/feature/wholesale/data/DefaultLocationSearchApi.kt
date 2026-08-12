package com.resthalflab.resthalfapp.feature.wholesale.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSearchApi
import com.resthalflab.resthalfapp.feature.wholesale.api.LocationSuggestion
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultLocationSearchApi(
    private val remote: LocationRemote,
) : LocationSearchApi {
    override suspend fun autosuggest(term: String): AppResult<List<LocationSuggestion>> = safeApiCall {
        val response = remote.autosuggest(term)
        withContext(Dispatchers.Default) {
            response.locationSuggestions.map { it.toDomain() }
        }
    }
}
