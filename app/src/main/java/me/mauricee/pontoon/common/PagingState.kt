package me.mauricee.pontoon.common

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