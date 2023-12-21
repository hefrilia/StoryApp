package com.example.storyapp

import com.example.storyapp.data.response.ListStoryItem

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val quote = ListStoryItem(
                 "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Placeholder_view_vector.svg/310px-Placeholder_view_vector.svg.png",
                "2022-01-08T06:34:18.598Z",
                 "Story $i ",
                "Desc $i",
                "0",
                i.toString(),
                "0",
            )
            items.add(quote)
        }
        return items
    }
}