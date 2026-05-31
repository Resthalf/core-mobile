package com.resthalflab.resthalfapp.feature.search.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.search.data.dto.toDomain
import com.resthalflab.resthalfapp.feature.search.domain.SearchRepository
import com.resthalflab.resthalfapp.feature.search.domain.model.Listing
import com.resthalflab.resthalfapp.feature.search.domain.model.SearchQuery

class DefaultSearchRepository(
    private val remote: SearchRemote,
) : SearchRepository {
    override suspend fun search(query: SearchQuery): AppResult<List<Listing>> = safeApiCall {
        remote.search(query.destination, query.guests).map { it.toDomain() }
    }
}
