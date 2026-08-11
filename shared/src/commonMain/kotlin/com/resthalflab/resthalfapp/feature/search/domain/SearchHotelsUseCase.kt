package com.resthalflab.resthalfapp.feature.search.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.domain.model.HotelSearchResult

class SearchHotelsUseCase(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(args: SearchArgs): AppResult<List<HotelSearchResult>> =
        repository.search(args)
}
