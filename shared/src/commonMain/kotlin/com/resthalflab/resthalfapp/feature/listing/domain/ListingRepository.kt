package com.resthalflab.resthalfapp.feature.listing.domain

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetail

interface ListingRepository {
    suspend fun getListing(id: String): AppResult<ListingDetail>
}
