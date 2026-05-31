package com.resthalflab.resthalfapp.feature.listing.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetail

class GetListingUseCase(
    private val repository: ListingRepository,
) {
    suspend operator fun invoke(id: String): AppResult<ListingDetail> = repository.getListing(id)
}
