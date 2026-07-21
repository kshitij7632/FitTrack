package com.fittrack.app

data class ProgressPhoto(
    val id: Int = 0,
    val username: String = "",
    val date: String = "",
    val month: String = "",          // e.g. "January 2026"
    val category: String = "front",  // front | side | back
    val filePath: String = "",
    val bodyWeight: Double = 0.0,
    val bodyFat: Double = 0.0,
    val measurementId: Int = 0,
    val notes: String = ""
)
