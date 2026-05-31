package com.resthalflab.resthalfapp.feature.search.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.search.domain.model.Listing
import com.resthalflab.resthalfapp.feature.search.domain.model.SearchQuery

class SearchListingsUseCase(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(query: SearchQuery): AppResult<List<Listing>> =
        repository.search(query)
}
