package me.mauricee.pontoon.repository.util.paging

enum class PagingState {
    //Loading state
    InitialFetch,
    Fetching,
    //Completed states
    Fetched,
    Completed,
    //Error states
    Empty,
    Error
}