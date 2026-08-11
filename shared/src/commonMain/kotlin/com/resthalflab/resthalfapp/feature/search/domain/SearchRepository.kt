package com.resthalflab.resthalfapp.feature.search.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.domain.model.HotelSearchResult

interface SearchRepository {
    suspend fun search(args: SearchArgs): AppResult<List<HotelSearchResult>>
}
