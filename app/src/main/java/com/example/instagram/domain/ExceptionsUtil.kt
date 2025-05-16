package com.example.instagram.domain

val InvalidUserException = Throwable("Invalid user logged in")
val UserAlreadyExistsException = Throwable("User already exists")
val UserCreationFailedException = Throwable("Failed to create user")
val UserNotFoundException = Throwable("User not found")