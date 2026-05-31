package com.resthalflab.resthalfapp.feature.search

import com.resthalflab.resthalfapp.core.domain.AppError
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.search.domain.SearchListingsUseCase
import com.resthalflab.resthalfapp.feature.search.domain.SearchRepository
import com.resthalflab.resthalfapp.feature.search.domain.model.Listing
import com.resthalflab.resthalfapp.feature.search.domain.model.SearchQuery
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

private class FakeSearchRepository(
    private val result: AppResult<List<Listing>>,
) : SearchRepository {
    var lastQuery: SearchQuery? = null
    override suspend fun search(query: SearchQuery): AppResult<List<Listing>> {
        lastQuery = query
        return result
    }
}

class SearchListingsUseCaseTest {

    private val sample = Listing("aria", "Aria Suites", "Jakarta", 850_000, "IDR", 4.6, reviewsCount = 1_240, thumbnailUrl = null)

    @Test
    fun returns_results_from_repository() = runTest {
        val repo = FakeSearchRepository(AppResult.Success(listOf(sample)))
        val useCase = SearchListingsUseCase(repo)

        val result = useCase(SearchQuery(destination = "Jakarta"))

        val success = result.shouldBeInstanceOf<AppResult.Success<List<Listing>>>()
        success.value shouldBe listOf(sample)
        repo.lastQuery shouldBe SearchQuery(destination = "Jakarta")
    }

    @Test
    fun propagates_repository_failure() = runTest {
        val repo = FakeSearchRepository(AppResult.Failure(AppError.Network.Timeout()))
        val useCase = SearchListingsUseCase(repo)

        val result = useCase(SearchQuery())

        result.shouldBeInstanceOf<AppResult.Failure>()
            .error.shouldBeInstanceOf<AppError.Network.Timeout>()
    }
}
