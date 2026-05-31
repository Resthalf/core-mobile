package com.resthalflab.resthalfapp.feature.search.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.search.domain.model.Listing
import com.resthalflab.resthalfapp.feature.search.domain.model.SearchQuery

interface SearchRepository {
    suspend fun search(query: SearchQuery): AppResult<List<Listing>>
}
