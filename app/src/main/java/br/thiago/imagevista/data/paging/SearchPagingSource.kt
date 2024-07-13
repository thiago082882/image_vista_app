package br.thiago.imagevista.data.paging


import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import br.thiago.imagevista.data.mapper.toDomainModelList
import br.thiago.imagevista.data.remote.UnsplashApiService
import br.thiago.imagevista.data.util.Constants
import br.thiago.imagevista.domain.model.UnsplashImage

class SearchPagingSource(
    private val query: String,
    private val unsplashApi: UnsplashApiService
): PagingSource<Int, UnsplashImage>() {

    companion object {
        private const val STARTING_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, UnsplashImage>): Int? {
        Log.d(Constants.IV_LOG_TAG, "getRefreshKey: ${state.anchorPosition}")
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UnsplashImage> {
        val currentPage = params.key ?: STARTING_PAGE_INDEX
        Log.d(Constants.IV_LOG_TAG, "currentPage: $currentPage")
        return try {
            val response = unsplashApi.searchImages(
                query = query,
                page = currentPage,
                perPage = params.loadSize
            )
            val endOfPaginationReached = response.images.isEmpty()
            Log.d(Constants.IV_LOG_TAG, "Load Result response: ${response.images.toDomainModelList()}")
            Log.d(Constants.IV_LOG_TAG, "endOfPaginationReached: $endOfPaginationReached")

            LoadResult.Page(
                data = response.images.toDomainModelList(),
                prevKey = if (currentPage == STARTING_PAGE_INDEX) null else currentPage - 1,
                nextKey = if (endOfPaginationReached) null else currentPage + 1
            )
        } catch (e: Exception) {
            Log.d(Constants.IV_LOG_TAG, "LoadResultError: ${e.message}")
            LoadResult.Error(e)
        }
    }
}