package com.resthalflab.resthalfapp.feature.listing.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.listing.api.ListingDetail
import com.resthalflab.resthalfapp.feature.listing.data.dto.toDomain
import com.resthalflab.resthalfapp.feature.listing.domain.ListingRepository

class DefaultListingRepository(
    private val remote: ListingRemote,
) : ListingRepository {
    override suspend fun getListing(id: String): AppResult<ListingDetail> = safeApiCall {
        remote.getListing(id).toDomain()
    }
}
