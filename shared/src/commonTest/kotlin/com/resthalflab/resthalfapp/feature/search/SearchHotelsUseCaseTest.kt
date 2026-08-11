package com.resthalflab.resthalfapp.feature.search

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.search.api.SlotType
import com.resthalflab.resthalfapp.feature.search.domain.SearchHotelsUseCase
import com.resthalflab.resthalfapp.feature.search.domain.SearchRepository
import com.resthalflab.resthalfapp.feature.search.domain.model.HotelSearchResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

private class FakeSearchRepository(
    private val result: AppResult<List<HotelSearchResult>>,
) : SearchRepository {
    var lastArgs: SearchArgs? = null
    override suspend fun search(args: SearchArgs): AppResult<List<HotelSearchResult>> {
        lastArgs = args
        return result
    }
}

class SearchHotelsUseCaseTest {

    private val args = SearchArgs(city = "Malang", date = "2026-06-10", slotType = SlotType.HALF_DAY)
    private val sample = HotelSearchResult(
        hotelId = "h1",
        hotelName = "Grand RestHalf Malang",
        city = "Malang",
        slotType = SlotType.HALF_DAY,
        slotLabel = "Half Day Stay",
        badge = "RestHalf Exclusive",
        windowLabel = "12:00 AM – 12:00 PM",
        fromPrice = 250_000,
        currency = "IDR",
        roomCount = 2,
    )

    @Test
    fun returns_results_and_forwards_args() = runTest {
        val repo = FakeSearchRepository(AppResult.Success(listOf(sample)))
        val useCase = SearchHotelsUseCase(repo)

        val result = useCase(args)

        result.shouldBeInstanceOf<AppResult.Success<List<HotelSearchResult>>>().value shouldBe listOf(sample)
        repo.lastArgs shouldBe args
    }

    @Test
    fun propagates_repository_failure() = runTest {
        val repo = FakeSearchRepository(AppResult.Failure(AppError.Network.Timeout()))
        val useCase = SearchHotelsUseCase(repo)

        useCase(args).shouldBeInstanceOf<AppResult.Failure>()
            .error.shouldBeInstanceOf<AppError.Network.Timeout>()
    }
}
