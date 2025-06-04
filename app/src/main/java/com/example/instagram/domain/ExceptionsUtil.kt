package com.example.instagram.domain

val InvalidUserException = Throwable("Invalid user logged in")
val UserCreationFailedException = Throwable("Failed to create user")
val UserNotFoundException = Throwable("User not found")

val UserNotLoggedInExceptions: Set<Throwable> = setOf(InvalidUserException, UserNotFoundException)