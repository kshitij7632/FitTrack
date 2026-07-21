package com.fittrack.app

data class BodyMeasurement(
    val id: Int = 0,
    val username: String = "",
    val date: String = "",
    val time: String = "",
    val bodyWeight: Double = 0.0,
    val chest: Double = 0.0,
    val waist: Double = 0.0,
    val hips: Double = 0.0,
    val leftArm: Double = 0.0,
    val rightArm: Double = 0.0,
    val leftForearm: Double = 0.0,
    val rightForearm: Double = 0.0,
    val leftThigh: Double = 0.0,
    val rightThigh: Double = 0.0,
    val leftCalf: Double = 0.0,
    val rightCalf: Double = 0.0,
    val neck: Double = 0.0,
    val shoulderWidth: Double = 0.0,
    val bodyFat: Double = 0.0,      // Optional, 0 = not recorded
    val notes: String = ""
)
