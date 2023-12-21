package com.example.storyapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storyapp.data.database.StoryDatabase
import com.example.storyapp.data.response.ListStoryItem

class StoryPaging (private val token: String, private val database: StoryDatabase) :  PagingSource<Int, ListStoryItem>(){
    private companion object{
        const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = database.storyDao().getAllStory()

            return responseData.load(params)
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}