package com.fittrack.app

/**
 * Provides 8 professionally designed workout splits with pre-populated exercises.
 * Used in the Workout Planner Setup Wizard for new users.
 */
object PredefinedSplitsLibrary {

    data class SplitTemplate(
        val name: String,
        val description: String,
        val goal: String,
        val daysPerWeek: Int,
        val difficulty: String,
        val days: List<DayTemplate>
    )

    data class DayTemplate(
        val dayName: String,
        val muscleGroups: String,
        val estimatedDuration: Int,
        val exercises: List<ExerciseTemplate>
    )

    data class ExerciseTemplate(
        val name: String,
        val muscleGroup: String,
        val equipment: String,
        val sets: Int,
        val reps: Int
    )

    fun getAllSplits(): List<SplitTemplate> = listOf(
        beginnerFullBody(),
        pushPullLegs(),
        broSplit(),
        upperLower(),
        arnoldSplit(),
        powerbuilding(),
        strengthProgram(),
        hypertrophyProgram()
    )

    // ─── 1. Beginner Full Body (3 Days) ─────────────────────────────────────
    private fun beginnerFullBody() = SplitTemplate(
        name = "Beginner Full Body",
        description = "Perfect for beginners. Train your entire body 3 times per week with fundamental compound movements. Focus on learning proper form and building a base.",
        goal = "Build Foundation",
        daysPerWeek = 3,
        difficulty = "Beginner",
        days = listOf(
            DayTemplate("Full Body A", "Chest, Back, Legs, Shoulders", 50, listOf(
                ExerciseTemplate("Squat", "Legs", "Barbell", 3, 8),
                ExerciseTemplate("Bench Press", "Chest", "Barbell", 3, 8),
                ExerciseTemplate("Bent Over Row", "Back", "Barbell", 3, 8),
                ExerciseTemplate("Overhead Press", "Shoulders", "Barbell", 3, 8),
                ExerciseTemplate("Romanian Deadlift", "Legs", "Barbell", 3, 10),
                ExerciseTemplate("Plank", "Core", "Bodyweight", 3, 30)
            )),
            DayTemplate("Full Body B", "Back, Chest, Legs, Core", 50, listOf(
                ExerciseTemplate("Deadlift", "Back", "Barbell", 3, 5),
                ExerciseTemplate("Incline Dumbbell Press", "Chest", "Dumbbell", 3, 10),
                ExerciseTemplate("Lat Pulldown", "Back", "Cable", 3, 10),
                ExerciseTemplate("Goblet Squat", "Legs", "Dumbbell", 3, 12),
                ExerciseTemplate("Dumbbell Lateral Raise", "Shoulders", "Dumbbell", 3, 12),
                ExerciseTemplate("Leg Raise", "Core", "Bodyweight", 3, 12)
            )),
            DayTemplate("Full Body C", "Chest, Legs, Back, Arms", 55, listOf(
                ExerciseTemplate("Front Squat", "Legs", "Barbell", 3, 8),
                ExerciseTemplate("Push-Up", "Chest", "Bodyweight", 3, 15),
                ExerciseTemplate("Cable Row", "Back", "Cable", 3, 10),
                ExerciseTemplate("Dumbbell Curl", "Biceps", "Dumbbell", 3, 12),
                ExerciseTemplate("Tricep Pushdown", "Triceps", "Cable", 3, 12),
                ExerciseTemplate("Calf Raise", "Calves", "Machine", 3, 15)
            ))
        )
    )

    // ─── 2. Push Pull Legs (6 Days) ──────────────────────────────────────────
    private fun pushPullLegs() = SplitTemplate(
        name = "Push Pull Legs",
        description = "The most popular split among intermediate lifters. Each muscle group trained twice a week. Push days target chest/shoulders/triceps. Pull days target back/biceps. Leg days target the entire lower body.",
        goal = "Muscle Hypertrophy",
        daysPerWeek = 6,
        difficulty = "Intermediate",
        days = listOf(
            DayTemplate("Push Day 1", "Chest, Shoulders, Triceps", 75, listOf(
                ExerciseTemplate("Bench Press", "Chest", "Barbell", 4, 8),
                ExerciseTemplate("Incline Dumbbell Press", "Chest", "Dumbbell", 3, 10),
                ExerciseTemplate("Overhead Press", "Shoulders", "Barbell", 4, 8),
                ExerciseTemplate("Dumbbell Lateral Raise", "Shoulders", "Dumbbell", 4, 15),
                ExerciseTemplate("Cable Fly", "Chest", "Cable", 3, 12),
                ExerciseTemplate("Tricep Pushdown", "Triceps", "Cable", 4, 12),
                ExerciseTemplate("Overhead Tricep Extension", "Triceps", "Cable", 3, 12)
            )),
            DayTemplate("Pull Day 1", "Back, Biceps, Rear Delts", 75, listOf(
                ExerciseTemplate("Deadlift", "Back", "Barbell", 4, 5),
                ExerciseTemplate("Barbell Row", "Back", "Barbell", 4, 8),
                ExerciseTemplate("Lat Pulldown", "Back", "Cable", 3, 10),
                ExerciseTemplate("Cable Row", "Back", "Cable", 3, 12),
                ExerciseTemplate("Barbell Curl", "Biceps", "Barbell", 4, 10),
                ExerciseTemplate("Hammer Curl", "Biceps", "Dumbbell", 3, 12),
                ExerciseTemplate("Rear Delt Fly", "Shoulders", "Dumbbell", 3, 15)
            )),
            DayTemplate("Leg Day 1", "Quads, Hamstrings, Glutes, Calves", 80, listOf(
                ExerciseTemplate("Squat", "Legs", "Barbell", 4, 8),
                ExerciseTemplate("Romanian Deadlift", "Legs", "Barbell", 4, 10),
                ExerciseTemplate("Leg Press", "Legs", "Machine", 4, 12),
                ExerciseTemplate("Leg Curl", "Legs", "Machine", 3, 12),
                ExerciseTemplate("Leg Extension", "Legs", "Machine", 3, 15),
                ExerciseTemplate("Standing Calf Raise", "Calves", "Machine", 5, 15)
            )),
            DayTemplate("Push Day 2", "Chest, Shoulders, Triceps", 70, listOf(
                ExerciseTemplate("Incline Bench Press", "Chest", "Barbell", 4, 8),
                ExerciseTemplate("Cable Chest Press", "Chest", "Cable", 3, 12),
                ExerciseTemplate("Dumbbell Shoulder Press", "Shoulders", "Dumbbell", 4, 10),
                ExerciseTemplate("Front Raise", "Shoulders", "Dumbbell", 3, 15),
                ExerciseTemplate("Pec Deck", "Chest", "Machine", 3, 15),
                ExerciseTemplate("Skull Crushers", "Triceps", "Barbell", 4, 10),
                ExerciseTemplate("Tricep Kickback", "Triceps", "Dumbbell", 3, 15)
            )),
            DayTemplate("Pull Day 2", "Back, Biceps, Forearms", 70, listOf(
                ExerciseTemplate("Pull-Up", "Back", "Bodyweight", 4, 8),
                ExerciseTemplate("T-Bar Row", "Back", "Barbell", 4, 10),
                ExerciseTemplate("Single Arm Row", "Back", "Dumbbell", 3, 12),
                ExerciseTemplate("Face Pull", "Shoulders", "Cable", 3, 15),
                ExerciseTemplate("Preacher Curl", "Biceps", "Barbell", 4, 10),
                ExerciseTemplate("Incline Dumbbell Curl", "Biceps", "Dumbbell", 3, 12),
                ExerciseTemplate("Wrist Curl", "Forearms", "Barbell", 3, 15)
            )),
            DayTemplate("Leg Day 2", "Quads, Glutes, Calves, Core", 75, listOf(
                ExerciseTemplate("Front Squat", "Legs", "Barbell", 4, 8),
                ExerciseTemplate("Bulgarian Split Squat", "Legs", "Dumbbell", 3, 10),
                ExerciseTemplate("Hip Thrust", "Glutes", "Barbell", 4, 12),
                ExerciseTemplate("Hack Squat", "Legs", "Machine", 3, 12),
                ExerciseTemplate("Seated Calf Raise", "Calves", "Machine", 4, 20),
                ExerciseTemplate("Hanging Leg Raise", "Core", "Bodyweight", 3, 15)
            ))
        )
    )

    // ─── 3. Bro Split (5 Days) ───────────────────────────────────────────────
    private fun broSplit() = SplitTemplate(
        name = "Bro Split",
        description = "The classic bodybuilding split. One muscle group per day with maximum volume. Great for intermediate bodybuilders who want to dedicate full sessions to each muscle.",
        goal = "Muscle Hypertrophy",
        daysPerWeek = 5,
        difficulty = "Intermediate",
        days = listOf(
            DayTemplate("Chest Day", "Chest", 70, listOf(
                ExerciseTemplate("Flat Bench Press", "Chest", "Barbell", 4, 8),
                ExerciseTemplate("Incline Bench Press", "Chest", "Barbell", 4, 10),
                ExerciseTemplate("Decline Bench Press", "Chest", "Barbell", 3, 10),
                ExerciseTemplate("Dumbbell Fly", "Chest", "Dumbbell", 3, 12),
                ExerciseTemplate("Cable Crossover", "Chest", "Cable", 3, 15),
                ExerciseTemplate("Push-Up", "Chest", "Bodyweight", 3, 20)
            )),
            DayTemplate("Back Day", "Back", 75, listOf(
                ExerciseTemplate("Deadlift", "Back", "Barbell", 4, 5),
                ExerciseTemplate("Wide Grip Pull-Up", "Back", "Bodyweight", 4, 8),
                ExerciseTemplate("Barbell Row", "Back", "Barbell", 4, 8),
                ExerciseTemplate("T-Bar Row", "Back", "Barbell", 3, 10),
                ExerciseTemplate("Lat Pulldown", "Back", "Cable", 3, 12),
                ExerciseTemplate("Seated Cable Row", "Back", "Cable", 3, 12)
            )),
            DayTemplate("Shoulder Day", "Shoulders", 65, listOf(
                ExerciseTemplate("Overhead Press", "Shoulders", "Barbell", 4, 8),
                ExerciseTemplate("Arnold Press", "Shoulders", "Dumbbell", 3, 10),
                ExerciseTemplate("Dumbbell Lateral Raise", "Shoulders", "Dumbbell", 4, 15),
                ExerciseTemplate("Front Raise", "Shoulders", "Dumbbell", 3, 12),
                ExerciseTemplate("Rear Delt Fly", "Shoulders", "Dumbbell", 4, 15),
                ExerciseTemplate("Barbell Shrug", "Shoulders", "Barbell", 4, 15)
            )),
            DayTemplate("Leg Day", "Quads, Hamstrings, Glutes, Calves", 80, listOf(
                ExerciseTemplate("Squat", "Legs", "Barbell", 5, 8),
                ExerciseTemplate("Leg Press", "Legs", "Machine", 4, 12),
                ExerciseTemplate("Romanian Deadlift", "Legs", "Barbell", 4, 10),
                ExerciseTemplate("Leg Curl", "Legs", "Machine", 4, 12),
                ExerciseTemplate("Leg Extension", "Legs", "Machine", 3, 15),
                ExerciseTemplate("Standing Calf Raise", "Calves", "Machine", 5, 20)
            )),
            DayTemplate("Arms Day", "Biceps, Triceps", 60, listOf(
                ExerciseTemplate("Barbell Curl", "Biceps", "Barbell", 4, 10),
                ExerciseTemplate("Incline Dumbbell Curl", "Biceps", "Dumbbell", 3, 12),
                ExerciseTemplate("Preacher Curl", "Biceps", "Barbell", 3, 12),
                ExerciseTemplate("Skull Crushers", "Triceps", "Barbell", 4, 10),
                ExerciseTemplate("Tricep Pushdown", "Triceps", "Cable", 3, 12),
                ExerciseTemplate("Overhead Tricep Extension", "Triceps", "Dumbbell", 3, 12)
            ))
        )
    )

    // ─── 4. Upper Lower (4 Days) ─────────────────────────────────────────────
    private fun upperLower() = SplitTemplate(
        name = "Upper Lower",
        description = "Trains upper body and lower body on alternating days. Each muscle group is hit twice per week. Ideal for intermediate lifters wanting strength and size.",
        goal = "Strength & Size",
        daysPerWeek = 4,
        difficulty = "Intermediate",
        days = listOf(
            DayTemplate("Upper A (Strength)", "Chest, Back, Shoulders, Arms", 70, listOf(
                ExerciseTemplate("Bench Press", "Chest", "Barbell", 4, 5),
                ExerciseTemplate("Barbell Row", "Back", "Barbell", 4, 5),
                ExerciseTemplate("Overhead Press", "Shoulders", "Barbell", 3, 8),
                ExerciseTemplate("Lat Pulldown", "Back", "Cable", 3, 10),
                ExerciseTemplate("Barbell Curl", "Biceps", "Barbell", 3, 10),
                ExerciseTemplate("Skull Crushers", "Triceps", "Barbell", 3, 10)
            )),
            DayTemplate("Lower A (Strength)", "Quads, Hamstrings, Glutes, Calves", 75, listOf(
                ExerciseTemplate("Squat", "Legs", "Barbell", 4, 5),
                ExerciseTemplate("Romanian Deadlift", "Legs", "Barbell", 4, 8),
                ExerciseTemplate("Leg Press", "Legs", "Machine", 3, 10),
                ExerciseTemplate("Leg Curl", "Legs", "Machine", 3, 12),
                ExerciseTemplate("Standing Calf Raise", "Calves", "Machine", 4, 15),
                ExerciseTemplate("Leg Raise", "Core", "Bodyweight", 3, 15)
            )),
            DayTemplate("Upper B (Hypertrophy)", "Chest, Back, Shoulders, Arms", 75, listOf(
                ExerciseTemplate("Incline Dumbbell Press", "Chest", "Dumbbell", 4, 10),
                ExerciseTemplate("Cable Row", "Back", "Cable", 4, 12),
                ExerciseTemplate("Dumbbell Shoulder Press", "Shoulders", "Dumbbell", 3, 12),
                ExerciseTemplate("Pull-Up", "Back", "Bodyweight", 3, 10),
                ExerciseTemplate("Cable Fly", "Chest", "Cable", 3, 15),
                ExerciseTemplate("Hammer Curl", "Biceps", "Dumbbell", 3, 12),
                ExerciseTemplate("Tricep Pushdown", "Triceps", "Cable", 3, 15)
            )),
            DayTemplate("Lower B (Hypertrophy)", "Quads, Glutes, Calves, Core", 75, listOf(
                ExerciseTemplate("Front Squat", "Legs", "Barbell", 4, 8),
                ExerciseTemplate("Hip Thrust", "Glutes", "Barbell", 4, 12),
                ExerciseTemplate("Hack Squat", "Legs", "Machine", 3, 12),
                ExerciseTemplate("Leg Extension", "Legs", "Machine", 3, 15),
                ExerciseTemplate("Seated Calf Raise", "Calves", "Machine", 4, 20),
                ExerciseTemplate("Cable Crunch", "Core", "Cable", 3, 15)
            ))
        )
    )

    // ─── 5. Arnold Split (6 Days) ────────────────────────────────────────────
    private fun arnoldSplit() = SplitTemplate(
        name = "Arnold Split",
        description = "Arnold Schwarzenegger's legendary 6-day split. Chest+Back on Monday/Thursday, Shoulders+Arms on Tuesday/Friday, Legs on Wednesday/Saturday. High volume, high intensity.",
        goal = "Classic Bodybuilding",
        daysPerWeek = 6,
        difficulty = "Advanced",
        days = listOf(
            DayTemplate("Chest + Back", "Chest, Back", 90, listOf(
                ExerciseTemplate("Bench Press", "Chest", "Barbell", 5, 8),
                ExerciseTemplate("Incline Bench Press", "Chest", "Barbell", 4, 10),
                ExerciseTemplate("Dumbbell Fly", "Chest", "Dumbbell", 3, 12),
                ExerciseTemplate("Wide Grip Pull-Up", "Back", "Bodyweight", 5, 10),
                ExerciseTemplate("Barbell Row", "Back", "Barbell", 5, 8),
                ExerciseTemplate("Lat Pulldown", "Back", "Cable", 4, 12),
                ExerciseTemplate("T-Bar Row", "Back", "Barbell", 3, 10)
            )),
            DayTemplate("Shoulders + Arms", "Shoulders, Biceps, Triceps", 85, listOf(
                ExerciseTemplate("Seated Dumbbell Press", "Shoulders", "Dumbbell", 4, 10),
                ExerciseTemplate("Barbell Upright Row", "Shoulders", "Barbell", 4, 10),
                ExerciseTemplate("Dumbbell Lateral Raise", "Shoulders", "Dumbbell", 4, 15),
                ExerciseTemplate("Barbell Curl", "Biceps", "Barbell", 5, 10),
                ExerciseTemplate("Incline Curl", "Biceps", "Dumbbell", 4, 12),
                ExerciseTemplate("Close Grip Bench Press", "Triceps", "Barbell", 5, 10),
                ExerciseTemplate("Tricep Pushdown", "Triceps", "Cable", 4, 12)
            )),
            DayTemplate("Leg Day", "Quads, Hamstrings, Calves", 80, listOf(
                ExerciseTemplate("Squat", "Legs", "Barbell", 5, 8),
                ExerciseTemplate("Leg Press", "Legs", "Machine", 4, 12),
                ExerciseTemplate("Leg Curl", "Legs", "Machine", 4, 12),
                ExerciseTemplate("Leg Extension", "Legs", "Machine", 4, 15),
                ExerciseTemplate("Standing Calf Raise", "Calves", "Machine", 6, 20),
                ExerciseTemplate("Seated Calf Raise", "Calves", "Machine", 4, 20)
            ))
        )
    )

    // ─── 6. Powerbuilding (5 Days) ───────────────────────────────────────────
    private fun powerbuilding() = SplitTemplate(
        name = "Powerbuilding",
        description = "Combines powerlifting strength work with bodybuilding hypertrophy. Each session starts with a heavy compound movement, then transitions to higher-rep accessory work.",
        goal = "Strength + Size",
        daysPerWeek = 5,
        difficulty = "Intermediate",
        days = listOf(
            DayTemplate("Bench Focus", "Chest, Triceps, Shoulders", 75, listOf(
                ExerciseTemplate("Bench Press", "Chest", "Barbell", 5, 3),
                ExerciseTemplate("Incline Dumbbell Press", "Chest", "Dumbbell", 4, 10),
                ExerciseTemplate("Overhead Press", "Shoulders", "Barbell", 3, 8),
                ExerciseTemplate("Cable Fly", "Chest", "Cable", 3, 15),
                ExerciseTemplate("Tricep Pushdown", "Triceps", "Cable", 4, 12),
                ExerciseTemplate("Lateral Raise", "Shoulders", "Dumbbell", 3, 15)
            )),
            DayTemplate("Squat Focus", "Quads, Hamstrings, Glutes", 80, listOf(
                ExerciseTemplate("Squat", "Legs", "Barbell", 5, 3),
                ExerciseTemplate("Pause Squat", "Legs", "Barbell", 3, 5),
                ExerciseTemplate("Leg Press", "Legs", "Machine", 4, 12),
                ExerciseTemplate("Romanian Deadlift", "Legs", "Barbell", 3, 10),
                ExerciseTemplate("Leg Curl", "Legs", "Machine", 3, 15),
                ExerciseTemplate("Standing Calf Raise", "Calves", "Machine", 4, 20)
            )),
            DayTemplate("Deadlift Focus", "Back, Hamstrings, Core", 80, listOf(
                ExerciseTemplate("Deadlift", "Back", "Barbell", 5, 3),
                ExerciseTemplate("Romanian Deadlift", "Legs", "Barbell", 3, 8),
                ExerciseTemplate("Barbell Row", "Back", "Barbell", 4, 8),
                ExerciseTemplate("Lat Pulldown", "Back", "Cable", 4, 10),
                ExerciseTemplate("Cable Row", "Back", "Cable", 3, 12),
                ExerciseTemplate("Plank", "Core", "Bodyweight", 3, 60)
            )),
            DayTemplate("Upper Hypertrophy", "Chest, Back, Shoulders, Arms", 75, listOf(
                ExerciseTemplate("Dumbbell Press", "Chest", "Dumbbell", 4, 12),
                ExerciseTemplate("Pull-Up", "Back", "Bodyweight", 4, 10),
                ExerciseTemplate("Dumbbell Shoulder Press", "Shoulders", "Dumbbell", 3, 12),
                ExerciseTemplate("Barbell Curl", "Biceps", "Barbell", 3, 12),
                ExerciseTemplate("Skull Crushers", "Triceps", "Barbell", 3, 12),
                ExerciseTemplate("Face Pull", "Shoulders", "Cable", 3, 20)
            )),
            DayTemplate("Lower Hypertrophy", "Quads, Glutes, Calves", 70, listOf(
                ExerciseTemplate("Front Squat", "Legs", "Barbell", 4, 8),
                ExerciseTemplate("Bulgarian Split Squat", "Legs", "Dumbbell", 3, 12),
                ExerciseTemplate("Hip Thrust", "Glutes", "Barbell", 4, 12),
                ExerciseTemplate("Leg Extension", "Legs", "Machine", 3, 15),
                ExerciseTemplate("Seated Calf Raise", "Calves", "Machine", 4, 20),
                ExerciseTemplate("Cable Crunch", "Core", "Cable", 3, 20)
            ))
        )
    )

    // ─── 7. Strength Program (4 Days) ────────────────────────────────────────
    private fun strengthProgram() = SplitTemplate(
        name = "Strength Program",
        description = "Focused on the 4 main lifts: Squat, Bench Press, Overhead Press, Deadlift. Low rep ranges (1-5), high intensity. Designed to build raw strength over 12-16 weeks.",
        goal = "Maximum Strength",
        daysPerWeek = 4,
        difficulty = "Intermediate",
        days = listOf(
            DayTemplate("Squat Day", "Legs, Core", 75, listOf(
                ExerciseTemplate("Squat", "Legs", "Barbell", 5, 5),
                ExerciseTemplate("Front Squat", "Legs", "Barbell", 3, 3),
                ExerciseTemplate("Romanian Deadlift", "Legs", "Barbell", 3, 8),
                ExerciseTemplate("Leg Curl", "Legs", "Machine", 3, 10),
                ExerciseTemplate("Hanging Leg Raise", "Core", "Bodyweight", 3, 15)
            )),
            DayTemplate("Bench Press Day", "Chest, Triceps, Shoulders", 70, listOf(
                ExerciseTemplate("Bench Press", "Chest", "Barbell", 5, 5),
                ExerciseTemplate("Paused Bench Press", "Chest", "Barbell", 3, 3),
                ExerciseTemplate("Incline Bench Press", "Chest", "Barbell", 3, 8),
                ExerciseTemplate("Dumbbell Fly", "Chest", "Dumbbell", 3, 12),
                ExerciseTemplate("Tricep Pushdown", "Triceps", "Cable", 3, 12)
            )),
            DayTemplate("Deadlift Day", "Back, Hamstrings", 75, listOf(
                ExerciseTemplate("Deadlift", "Back", "Barbell", 5, 3),
                ExerciseTemplate("Sumo Deadlift", "Legs", "Barbell", 3, 3),
                ExerciseTemplate("Barbell Row", "Back", "Barbell", 4, 6),
                ExerciseTemplate("Pull-Up", "Back", "Bodyweight", 4, 8),
                ExerciseTemplate("Lat Pulldown", "Back", "Cable", 3, 10)
            )),
            DayTemplate("Overhead Press Day", "Shoulders, Triceps, Back", 65, listOf(
                ExerciseTemplate("Overhead Press", "Shoulders", "Barbell", 5, 5),
                ExerciseTemplate("Push Press", "Shoulders", "Barbell", 3, 3),
                ExerciseTemplate("Lateral Raise", "Shoulders", "Dumbbell", 3, 15),
                ExerciseTemplate("Face Pull", "Shoulders", "Cable", 3, 20),
                ExerciseTemplate("Tricep Pushdown", "Triceps", "Cable", 3, 12),
                ExerciseTemplate("Barbell Curl", "Biceps", "Barbell", 3, 10)
            ))
        )
    )

    // ─── 8. Hypertrophy Program (5 Days) ─────────────────────────────────────
    private fun hypertrophyProgram() = SplitTemplate(
        name = "Hypertrophy Program",
        description = "Science-based hypertrophy program. High volume, moderate intensity (8-15 reps), progressive overload. Targets each muscle group with 12-20 sets per week for maximum muscle growth.",
        goal = "Maximum Muscle Growth",
        daysPerWeek = 5,
        difficulty = "Intermediate",
        days = listOf(
            DayTemplate("Chest + Triceps", "Chest, Triceps", 75, listOf(
                ExerciseTemplate("Incline Bench Press", "Chest", "Barbell", 4, 10),
                ExerciseTemplate("Flat Dumbbell Press", "Chest", "Dumbbell", 4, 12),
                ExerciseTemplate("Cable Fly", "Chest", "Cable", 3, 15),
                ExerciseTemplate("Pec Deck", "Chest", "Machine", 3, 15),
                ExerciseTemplate("Skull Crushers", "Triceps", "Barbell", 4, 12),
                ExerciseTemplate("Overhead Tricep Extension", "Triceps", "Cable", 3, 15),
                ExerciseTemplate("Tricep Pushdown", "Triceps", "Cable", 3, 15)
            )),
            DayTemplate("Back + Biceps", "Back, Biceps", 80, listOf(
                ExerciseTemplate("Pull-Up", "Back", "Bodyweight", 4, 10),
                ExerciseTemplate("Barbell Row", "Back", "Barbell", 4, 10),
                ExerciseTemplate("Cable Row", "Back", "Cable", 4, 12),
                ExerciseTemplate("Lat Pulldown", "Back", "Cable", 3, 12),
                ExerciseTemplate("Single Arm Row", "Back", "Dumbbell", 3, 12),
                ExerciseTemplate("Barbell Curl", "Biceps", "Barbell", 4, 12),
                ExerciseTemplate("Incline Dumbbell Curl", "Biceps", "Dumbbell", 3, 15)
            )),
            DayTemplate("Shoulder + Neck", "Shoulders, Neck", 70, listOf(
                ExerciseTemplate("Dumbbell Shoulder Press", "Shoulders", "Dumbbell", 4, 12),
                ExerciseTemplate("Lateral Raise", "Shoulders", "Dumbbell", 5, 15),
                ExerciseTemplate("Cable Lateral Raise", "Shoulders", "Cable", 3, 15),
                ExerciseTemplate("Face Pull", "Shoulders", "Cable", 4, 20),
                ExerciseTemplate("Rear Delt Fly", "Shoulders", "Dumbbell", 4, 15),
                ExerciseTemplate("Dumbbell Shrug", "Shoulders", "Dumbbell", 4, 20)
            )),
            DayTemplate("Leg Day", "Quads, Hamstrings, Glutes, Calves", 85, listOf(
                ExerciseTemplate("Squat", "Legs", "Barbell", 4, 10),
                ExerciseTemplate("Romanian Deadlift", "Legs", "Barbell", 4, 10),
                ExerciseTemplate("Leg Press", "Legs", "Machine", 4, 15),
                ExerciseTemplate("Bulgarian Split Squat", "Legs", "Dumbbell", 3, 12),
                ExerciseTemplate("Leg Curl", "Legs", "Machine", 4, 15),
                ExerciseTemplate("Hip Thrust", "Glutes", "Barbell", 4, 15),
                ExerciseTemplate("Standing Calf Raise", "Calves", "Machine", 5, 20)
            )),
            DayTemplate("Biceps + Triceps + Core", "Biceps, Triceps, Core", 65, listOf(
                ExerciseTemplate("Preacher Curl", "Biceps", "Barbell", 4, 12),
                ExerciseTemplate("Hammer Curl", "Biceps", "Dumbbell", 3, 15),
                ExerciseTemplate("Concentration Curl", "Biceps", "Dumbbell", 3, 15),
                ExerciseTemplate("Close Grip Bench Press", "Triceps", "Barbell", 4, 12),
                ExerciseTemplate("Dips", "Triceps", "Bodyweight", 3, 15),
                ExerciseTemplate("Cable Crunch", "Core", "Cable", 4, 20),
                ExerciseTemplate("Plank", "Core", "Bodyweight", 3, 60)
            ))
        )
    )

    /**
     * Seeds the ExerciseLibrary table with a comprehensive professional exercise list.
     * Called once when the library is empty.
     */
    fun seedExerciseLibrary(db: DatabaseHelper) {
        if (db.isExerciseLibrarySeeded()) return

        val exercises = listOf(
            // CHEST
            ExerciseInfo(name = "Bench Press", muscleGroup = "Chest", equipment = "Barbell", difficulty = "Intermediate", category = "Compound", description = "The king of chest exercises. Lie on a flat bench and press a barbell from chest to lockout."),
            ExerciseInfo(name = "Incline Bench Press", muscleGroup = "Chest", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Decline Bench Press", muscleGroup = "Chest", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Dumbbell Press", muscleGroup = "Chest", equipment = "Dumbbell", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Incline Dumbbell Press", muscleGroup = "Chest", equipment = "Dumbbell", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Dumbbell Fly", muscleGroup = "Chest", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Cable Fly", muscleGroup = "Chest", equipment = "Cable", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Cable Crossover", muscleGroup = "Chest", equipment = "Cable", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Pec Deck", muscleGroup = "Chest", equipment = "Machine", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Push-Up", muscleGroup = "Chest", equipment = "Bodyweight", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Chest Press Machine", muscleGroup = "Chest", equipment = "Machine", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Paused Bench Press", muscleGroup = "Chest", equipment = "Barbell", difficulty = "Advanced", category = "Compound"),

            // BACK
            ExerciseInfo(name = "Deadlift", muscleGroup = "Back", equipment = "Barbell", difficulty = "Advanced", category = "Compound", description = "The most fundamental strength exercise. Hinge at the hips and lift a loaded barbell from the floor."),
            ExerciseInfo(name = "Barbell Row", muscleGroup = "Back", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Pull-Up", muscleGroup = "Back", equipment = "Bodyweight", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Chin-Up", muscleGroup = "Back", equipment = "Bodyweight", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Lat Pulldown", muscleGroup = "Back", equipment = "Cable", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Cable Row", muscleGroup = "Back", equipment = "Cable", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "T-Bar Row", muscleGroup = "Back", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Single Arm Row", muscleGroup = "Back", equipment = "Dumbbell", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Sumo Deadlift", muscleGroup = "Back", equipment = "Barbell", difficulty = "Advanced", category = "Compound"),
            ExerciseInfo(name = "Romanian Deadlift", muscleGroup = "Back", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Good Morning", muscleGroup = "Back", equipment = "Barbell", difficulty = "Advanced", category = "Compound"),

            // SHOULDERS
            ExerciseInfo(name = "Overhead Press", muscleGroup = "Shoulders", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Dumbbell Shoulder Press", muscleGroup = "Shoulders", equipment = "Dumbbell", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Arnold Press", muscleGroup = "Shoulders", equipment = "Dumbbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Lateral Raise", muscleGroup = "Shoulders", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Cable Lateral Raise", muscleGroup = "Shoulders", equipment = "Cable", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Front Raise", muscleGroup = "Shoulders", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Rear Delt Fly", muscleGroup = "Shoulders", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Face Pull", muscleGroup = "Shoulders", equipment = "Cable", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Barbell Shrug", muscleGroup = "Shoulders", equipment = "Barbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Upright Row", muscleGroup = "Shoulders", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),

            // BICEPS
            ExerciseInfo(name = "Barbell Curl", muscleGroup = "Biceps", equipment = "Barbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Dumbbell Curl", muscleGroup = "Biceps", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Hammer Curl", muscleGroup = "Biceps", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Incline Dumbbell Curl", muscleGroup = "Biceps", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Preacher Curl", muscleGroup = "Biceps", equipment = "Barbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Concentration Curl", muscleGroup = "Biceps", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Cable Curl", muscleGroup = "Biceps", equipment = "Cable", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "EZ Bar Curl", muscleGroup = "Biceps", equipment = "Barbell", difficulty = "Beginner", category = "Isolation"),

            // TRICEPS
            ExerciseInfo(name = "Tricep Pushdown", muscleGroup = "Triceps", equipment = "Cable", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Overhead Tricep Extension", muscleGroup = "Triceps", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Skull Crushers", muscleGroup = "Triceps", equipment = "Barbell", difficulty = "Intermediate", category = "Isolation"),
            ExerciseInfo(name = "Close Grip Bench Press", muscleGroup = "Triceps", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Dips", muscleGroup = "Triceps", equipment = "Bodyweight", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Tricep Kickback", muscleGroup = "Triceps", equipment = "Dumbbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Rope Pushdown", muscleGroup = "Triceps", equipment = "Cable", difficulty = "Beginner", category = "Isolation"),

            // FOREARMS
            ExerciseInfo(name = "Wrist Curl", muscleGroup = "Forearms", equipment = "Barbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Reverse Wrist Curl", muscleGroup = "Forearms", equipment = "Barbell", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Farmer's Walk", muscleGroup = "Forearms", equipment = "Dumbbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Dead Hang", muscleGroup = "Forearms", equipment = "Bodyweight", difficulty = "Beginner", category = "Isolation"),

            // LEGS / QUADS / HAMSTRINGS
            ExerciseInfo(name = "Squat", muscleGroup = "Legs", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Front Squat", muscleGroup = "Legs", equipment = "Barbell", difficulty = "Advanced", category = "Compound"),
            ExerciseInfo(name = "Hack Squat", muscleGroup = "Legs", equipment = "Machine", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Leg Press", muscleGroup = "Legs", equipment = "Machine", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Leg Extension", muscleGroup = "Legs", equipment = "Machine", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Leg Curl", muscleGroup = "Legs", equipment = "Machine", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Romanian Deadlift", muscleGroup = "Legs", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Bulgarian Split Squat", muscleGroup = "Legs", equipment = "Dumbbell", difficulty = "Intermediate", category = "Compound"),
            ExerciseInfo(name = "Lunge", muscleGroup = "Legs", equipment = "Dumbbell", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Goblet Squat", muscleGroup = "Legs", equipment = "Dumbbell", difficulty = "Beginner", category = "Compound"),
            ExerciseInfo(name = "Sumo Squat", muscleGroup = "Legs", equipment = "Barbell", difficulty = "Intermediate", category = "Compound"),

            // GLUTES
            ExerciseInfo(name = "Hip Thrust", muscleGroup = "Glutes", equipment = "Barbell", difficulty = "Intermediate", category = "Isolation"),
            ExerciseInfo(name = "Glute Bridge", muscleGroup = "Glutes", equipment = "Bodyweight", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Cable Kickback", muscleGroup = "Glutes", equipment = "Cable", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Donkey Kick", muscleGroup = "Glutes", equipment = "Bodyweight", difficulty = "Beginner", category = "Isolation"),

            // CALVES
            ExerciseInfo(name = "Standing Calf Raise", muscleGroup = "Calves", equipment = "Machine", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Seated Calf Raise", muscleGroup = "Calves", equipment = "Machine", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Donkey Calf Raise", muscleGroup = "Calves", equipment = "Machine", difficulty = "Beginner", category = "Isolation"),

            // CORE / ABS
            ExerciseInfo(name = "Plank", muscleGroup = "Core", equipment = "Bodyweight", difficulty = "Beginner", category = "Isometric"),
            ExerciseInfo(name = "Cable Crunch", muscleGroup = "Core", equipment = "Cable", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Hanging Leg Raise", muscleGroup = "Core", equipment = "Bodyweight", difficulty = "Intermediate", category = "Isolation"),
            ExerciseInfo(name = "Leg Raise", muscleGroup = "Core", equipment = "Bodyweight", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Crunch", muscleGroup = "Core", equipment = "Bodyweight", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Russian Twist", muscleGroup = "Core", equipment = "Bodyweight", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Ab Wheel Rollout", muscleGroup = "Core", equipment = "Other", difficulty = "Advanced", category = "Isolation"),
            ExerciseInfo(name = "Side Plank", muscleGroup = "Core", equipment = "Bodyweight", difficulty = "Beginner", category = "Isometric"),

            // NECK
            ExerciseInfo(name = "Neck Curl", muscleGroup = "Neck", equipment = "Bodyweight", difficulty = "Beginner", category = "Isolation"),
            ExerciseInfo(name = "Neck Extension", muscleGroup = "Neck", equipment = "Bodyweight", difficulty = "Beginner", category = "Isolation"),

            // CARDIO
            ExerciseInfo(name = "Treadmill", muscleGroup = "Cardio", equipment = "Machine", difficulty = "Beginner", category = "Cardio"),
            ExerciseInfo(name = "Rowing Machine", muscleGroup = "Cardio", equipment = "Machine", difficulty = "Beginner", category = "Cardio"),
            ExerciseInfo(name = "Stationary Bike", muscleGroup = "Cardio", equipment = "Machine", difficulty = "Beginner", category = "Cardio"),
            ExerciseInfo(name = "Jump Rope", muscleGroup = "Cardio", equipment = "Other", difficulty = "Beginner", category = "Cardio"),
            ExerciseInfo(name = "Box Jump", muscleGroup = "Cardio", equipment = "Bodyweight", difficulty = "Intermediate", category = "Cardio"),
            ExerciseInfo(name = "Burpee", muscleGroup = "Cardio", equipment = "Bodyweight", difficulty = "Intermediate", category = "Cardio"),

            // MOBILITY / RECOVERY
            ExerciseInfo(name = "Foam Rolling", muscleGroup = "Mobility", equipment = "Other", difficulty = "Beginner", category = "Recovery"),
            ExerciseInfo(name = "Hip Flexor Stretch", muscleGroup = "Mobility", equipment = "Bodyweight", difficulty = "Beginner", category = "Mobility"),
            ExerciseInfo(name = "Thoracic Rotation", muscleGroup = "Mobility", equipment = "Bodyweight", difficulty = "Beginner", category = "Mobility")
        )

        for (exercise in exercises) {
            db.insertExerciseToLibrary(exercise.copy(username = ""))
        }
    }
}
