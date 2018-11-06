package me.mauricee.pontoon.di

import javax.inject.Named
import javax.inject.Scope

@Scope
@MustBeDocumented
annotation class AppContext : Named("AppContext")