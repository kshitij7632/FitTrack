package com.fittrack.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FitTrack.db"
        private const val DATABASE_VERSION = 5

        // ── Legacy Workout table ─────────────────────────────────────────────
        private const val TABLE_WORKOUT = "Workout"
        private const val COL_ID = "id"
        private const val COL_EXERCISE_NAME = "exerciseName"
        private const val COL_MUSCLE_GROUP = "muscleGroup"
        private const val COL_WEIGHT = "weight"
        private const val COL_SETS = "sets"
        private const val COL_REPS = "reps"
        private const val COL_DURATION = "duration"
        private const val COL_DATE = "date"
        private const val COL_NOTES = "notes"
        private const val COL_IMAGE_PATH = "imagePath"
        private const val COL_USERNAME = "username"

        // ── New table names ──────────────────────────────────────────────────
        const val TABLE_SPLITS          = "WorkoutSplits"
        const val TABLE_WORKOUT_DAYS    = "WorkoutDays"
        const val TABLE_DAY_EXERCISES   = "DayExercises"
        const val TABLE_WEEKLY_SCHEDULE = "WeeklySchedule"
        const val TABLE_SESSIONS        = "WorkoutSessions"
        const val TABLE_SESSION_LOGS    = "SessionExerciseLogs"
        const val TABLE_EXERCISE_LIB    = "ExerciseLibrary"
        const val TABLE_MEASUREMENTS    = "BodyMeasurements"
        const val TABLE_PHOTOS          = "ProgressPhotos"
        const val TABLE_PLANNED         = "PlannedWorkouts"
        const val TABLE_MILESTONES      = "ExerciseMilestones"
    }

    // ════════════════════════════════════════════════════════════════════════
    // onCreate / onUpgrade
    // ════════════════════════════════════════════════════════════════════════

    override fun onCreate(db: SQLiteDatabase) {
        // Legacy tables
        db.execSQL("""
            CREATE TABLE $TABLE_WORKOUT (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EXERCISE_NAME TEXT,
                $COL_MUSCLE_GROUP TEXT,
                $COL_WEIGHT REAL,
                $COL_SETS INTEGER,
                $COL_REPS INTEGER,
                $COL_DURATION INTEGER,
                $COL_DATE TEXT,
                $COL_NOTES TEXT,
                $COL_IMAGE_PATH TEXT DEFAULT '',
                $COL_USERNAME TEXT DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE Goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                weeklyWorkoutGoal INTEGER DEFAULT 4,
                monthlyWorkoutGoal INTEGER DEFAULT 16,
                targetWeight REAL DEFAULT 0,
                targetDuration INTEGER DEFAULT 60
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE Achievements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                achievementKey TEXT,
                unlockedAt TEXT
            )
        """.trimIndent())

        createV5Tables(db)
    }

    private fun createV5Tables(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SPLITS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                name TEXT,
                description TEXT,
                goal TEXT DEFAULT '',
                isActive INTEGER DEFAULT 0,
                createdAt TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_WORKOUT_DAYS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                splitId INTEGER,
                dayName TEXT,
                muscleGroups TEXT,
                estimatedDuration INTEGER DEFAULT 60,
                notes TEXT DEFAULT '',
                sortOrder INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_DAY_EXERCISES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dayId INTEGER,
                exerciseName TEXT,
                muscleGroup TEXT,
                equipment TEXT DEFAULT '',
                defaultSets INTEGER DEFAULT 3,
                defaultReps INTEGER DEFAULT 10,
                sortOrder INTEGER DEFAULT 0,
                notes TEXT DEFAULT '',
                isFavorite INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_WEEKLY_SCHEDULE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                splitId INTEGER,
                weekday INTEGER,
                workoutDayId INTEGER,
                isRestDay INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SESSIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                workoutDayId INTEGER DEFAULT 0,
                workoutDayName TEXT DEFAULT '',
                date TEXT,
                startTime TEXT DEFAULT '',
                endTime TEXT DEFAULT '',
                durationMinutes INTEGER DEFAULT 0,
                totalVolume REAL DEFAULT 0,
                notes TEXT DEFAULT '',
                mood INTEGER DEFAULT 0,
                energyLevel INTEGER DEFAULT 0,
                sleepQuality INTEGER DEFAULT 0,
                status TEXT DEFAULT 'completed',
                caloriesBurned INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SESSION_LOGS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sessionId INTEGER,
                exerciseName TEXT,
                muscleGroup TEXT DEFAULT '',
                weight REAL DEFAULT 0,
                sets INTEGER DEFAULT 0,
                reps INTEGER DEFAULT 0,
                notes TEXT DEFAULT '',
                isNewPR INTEGER DEFAULT 0,
                sortOrder INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_EXERCISE_LIB (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                muscleGroup TEXT,
                equipment TEXT DEFAULT '',
                difficulty TEXT DEFAULT 'Intermediate',
                category TEXT DEFAULT '',
                description TEXT DEFAULT '',
                isFavorite INTEGER DEFAULT 0,
                isCustom INTEGER DEFAULT 0,
                username TEXT DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_MEASUREMENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                date TEXT,
                time TEXT DEFAULT '',
                bodyWeight REAL DEFAULT 0,
                chest REAL DEFAULT 0,
                waist REAL DEFAULT 0,
                hips REAL DEFAULT 0,
                leftArm REAL DEFAULT 0,
                rightArm REAL DEFAULT 0,
                leftForearm REAL DEFAULT 0,
                rightForearm REAL DEFAULT 0,
                leftThigh REAL DEFAULT 0,
                rightThigh REAL DEFAULT 0,
                leftCalf REAL DEFAULT 0,
                rightCalf REAL DEFAULT 0,
                neck REAL DEFAULT 0,
                shoulderWidth REAL DEFAULT 0,
                bodyFat REAL DEFAULT 0,
                notes TEXT DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_PHOTOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                date TEXT,
                month TEXT DEFAULT '',
                category TEXT DEFAULT 'front',
                filePath TEXT,
                bodyWeight REAL DEFAULT 0,
                bodyFat REAL DEFAULT 0,
                measurementId INTEGER DEFAULT 0,
                notes TEXT DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_PLANNED (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                date TEXT,
                workoutDayId INTEGER DEFAULT 0,
                workoutDayName TEXT DEFAULT '',
                status TEXT DEFAULT 'planned',
                sessionId INTEGER DEFAULT 0,
                notes TEXT DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_MILESTONES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                exerciseName TEXT,
                milestoneKey TEXT,
                unlockedAt TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_WORKOUT ADD COLUMN $COL_IMAGE_PATH TEXT DEFAULT ''")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_WORKOUT ADD COLUMN $COL_USERNAME TEXT DEFAULT ''")
        }
        if (oldVersion < 4) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS Goals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT,
                    weeklyWorkoutGoal INTEGER DEFAULT 4,
                    monthlyWorkoutGoal INTEGER DEFAULT 16,
                    targetWeight REAL DEFAULT 0,
                    targetDuration INTEGER DEFAULT 60
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS Achievements (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT,
                    achievementKey TEXT,
                    unlockedAt TEXT
                )
            """.trimIndent())
        }
        if (oldVersion < 5) {
            createV5Tables(db)
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // LEGACY WORKOUT TABLE — kept for backward compatibility
    // ════════════════════════════════════════════════════════════════════════

    fun insertWorkout(workout: Workout): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EXERCISE_NAME, workout.exerciseName)
            put(COL_MUSCLE_GROUP, workout.muscleGroup)
            put(COL_WEIGHT, workout.weight)
            put(COL_SETS, workout.sets)
            put(COL_REPS, workout.reps)
            put(COL_DURATION, workout.duration)
            put(COL_DATE, workout.date)
            put(COL_NOTES, workout.notes)
            put(COL_IMAGE_PATH, workout.imagePath)
            put(COL_USERNAME, workout.username)
        }
        val result = db.insert(TABLE_WORKOUT, null, values)
        db.close()
        return result
    }

    fun updateWorkout(workout: Workout): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EXERCISE_NAME, workout.exerciseName)
            put(COL_MUSCLE_GROUP, workout.muscleGroup)
            put(COL_WEIGHT, workout.weight)
            put(COL_SETS, workout.sets)
            put(COL_REPS, workout.reps)
            put(COL_DURATION, workout.duration)
            put(COL_DATE, workout.date)
            put(COL_NOTES, workout.notes)
            put(COL_IMAGE_PATH, workout.imagePath)
            put(COL_USERNAME, workout.username)
        }
        val result = db.update(TABLE_WORKOUT, values, "$COL_ID = ?", arrayOf(workout.id.toString()))
        db.close()
        return result
    }

    fun deleteWorkout(id: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_WORKOUT, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun getWorkoutById(id: Int): Workout? {
        val db = readableDatabase
        val cursor = db.query(TABLE_WORKOUT, null, "$COL_ID = ?", arrayOf(id.toString()), null, null, null)
        var workout: Workout? = null
        if (cursor.moveToFirst()) workout = cursorToWorkout(cursor)
        cursor.close()
        db.close()
        return workout
    }

    fun getAllWorkouts(username: String): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WORKOUT, null, "$COL_USERNAME = ?", arrayOf(username), null, null, "$COL_DATE DESC")
        if (cursor.moveToFirst()) do { workouts.add(cursorToWorkout(cursor)) } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return workouts
    }

    fun searchWorkouts(query: String, username: String): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WORKOUT, null, "$COL_USERNAME = ? AND $COL_EXERCISE_NAME LIKE ?", arrayOf(username, "%$query%"), null, null, "$COL_DATE DESC")
        if (cursor.moveToFirst()) do { workouts.add(cursorToWorkout(cursor)) } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return workouts
    }

    fun getWorkoutsOnDate(username: String, date: String): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WORKOUT, null, "$COL_USERNAME = ? AND $COL_DATE = ?", arrayOf(username, date), null, null, null)
        if (cursor.moveToFirst()) do { workouts.add(cursorToWorkout(cursor)) } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return workouts
    }

    fun getWorkoutDatesSet(username: String): Set<String> {
        val set = mutableSetOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COL_DATE FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        if (cursor.moveToFirst()) do { val d = cursor.getString(0); if (d != null) set.add(d) } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return set
    }

    fun getAllExerciseNames(username: String): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COL_EXERCISE_NAME FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        if (cursor.moveToFirst()) do { val n = cursor.getString(0); if (n != null) list.add(n) } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return list
    }

    // ════════════════════════════════════════════════════════════════════════
    // LEGACY STATS — unchanged
    // ════════════════════════════════════════════════════════════════════════

    fun getPersonalRecords(username: String): List<Pair<String, Double>> {
        val records = mutableListOf<Pair<String, Double>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_EXERCISE_NAME, MAX($COL_WEIGHT) as maxWeight FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? GROUP BY $COL_EXERCISE_NAME ORDER BY maxWeight DESC",
            arrayOf(username)
        )
        if (cursor.moveToFirst()) do {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXERCISE_NAME))
            val weight = cursor.getDouble(cursor.getColumnIndexOrThrow("maxWeight"))
            records.add(Pair(name, weight))
        } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return records
    }

    data class DetailedPR(
        val exerciseName: String,
        val maxWeight: Double,
        val maxWeightDate: String,
        val estimated1RM: Double,
        val maxVolume: Double
    )

    fun getDetailedPersonalRecords(username: String): List<DetailedPR> {
        val records = mutableListOf<DetailedPR>()
        val db = readableDatabase
        val exercises = getAllExerciseNames(username)
        for (exercise in exercises) {
            var maxWeight = 0.0; var maxWeightDate = ""; var maxVolume = 0.0; var est1RM = 0.0
            val cursor = db.rawQuery(
                "SELECT $COL_WEIGHT, $COL_REPS, $COL_SETS, $COL_DATE FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? AND $COL_EXERCISE_NAME = ?",
                arrayOf(username, exercise)
            )
            if (cursor.moveToFirst()) do {
                val w = cursor.getDouble(0); val r = cursor.getInt(1); val s = cursor.getInt(2); val d = cursor.getString(3) ?: ""
                if (w > maxWeight) { maxWeight = w; maxWeightDate = d }
                val vol = w * r * s; if (vol > maxVolume) maxVolume = vol
                val e1 = w * (1 + (r / 30.0)); if (e1 > est1RM) est1RM = e1
            } while (cursor.moveToNext())
            cursor.close()
            if (maxWeight > 0 || maxVolume > 0) records.add(DetailedPR(exercise, maxWeight, maxWeightDate, est1RM, maxVolume))
        }
        db.close()
        return records.sortedByDescending { it.maxWeight }
    }

    fun getTotalWorkoutCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        var count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close(); db.close(); return count
    }

    fun getGlobalTotalWorkoutCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_WORKOUT", null)
        var count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close(); db.close(); return count
    }

    fun getTotalWeightLifted(username: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_WEIGHT * $COL_SETS * $COL_REPS) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        var total = 0.0; if (cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getDouble(0)
        cursor.close(); db.close(); return total
    }

    fun getUniqueExerciseCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(DISTINCT $COL_EXERCISE_NAME) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        var count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close(); db.close(); return count
    }

    fun getFavouriteMuscleGroup(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_MUSCLE_GROUP, COUNT(*) as cnt FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? GROUP BY $COL_MUSCLE_GROUP ORDER BY cnt DESC LIMIT 1", arrayOf(username))
        var muscle = "N/A"; if (cursor.moveToFirst()) muscle = cursor.getString(cursor.getColumnIndexOrThrow(COL_MUSCLE_GROUP)) ?: "N/A"
        cursor.close(); db.close(); return muscle
    }

    fun getFavouriteExercise(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_EXERCISE_NAME, COUNT(*) as cnt FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? GROUP BY $COL_EXERCISE_NAME ORDER BY cnt DESC LIMIT 1", arrayOf(username))
        var exercise = "N/A"; if (cursor.moveToFirst()) exercise = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXERCISE_NAME)) ?: "N/A"
        cursor.close(); db.close(); return exercise
    }

    fun getWorkoutStreak(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COL_DATE FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? ORDER BY $COL_DATE DESC", arrayOf(username))
        if (!cursor.moveToFirst()) { cursor.close(); db.close(); return 0 }
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance(); var streak = 0
        do {
            val dateStr = cursor.getString(0) ?: continue
            val workoutDate = try { sdf.parse(dateStr) } catch (e: Exception) { continue } ?: continue
            val cal2 = java.util.Calendar.getInstance().apply { time = workoutDate }
            val daysDiff = ((calendar.timeInMillis - cal2.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff == streak) streak++ else break
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return streak
    }

    fun getLongestStreak(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COL_DATE FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? ORDER BY $COL_DATE DESC", arrayOf(username))
        if (!cursor.moveToFirst()) { cursor.close(); db.close(); return 0 }
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        var maxStreak = 0; var currentStreak = 1; var prevDateMillis = 0L
        do {
            val dateStr = cursor.getString(0) ?: continue
            val workoutDate = try { sdf.parse(dateStr) } catch (e: Exception) { continue } ?: continue
            val currentMillis = workoutDate.time
            if (prevDateMillis == 0L) { prevDateMillis = currentMillis; maxStreak = 1; continue }
            val daysDiff = ((prevDateMillis - currentMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff == 1) { currentStreak++; if (currentStreak > maxStreak) maxStreak = currentStreak }
            else if (daysDiff > 1) currentStreak = 1
            prevDateMillis = currentMillis
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return maxStreak
    }

    fun getTotalWorkoutDuration(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_DURATION) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        var total = 0; if (cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getInt(0)
        cursor.close(); db.close(); return total
    }

    fun getPersonalRecordsCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(DISTINCT $COL_EXERCISE_NAME) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        var count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close(); db.close(); return count
    }

    fun getProgressPhotoCount(username: String): Int {
        val db = readableDatabase
        // Count from new ProgressPhotos table
        val c1 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PHOTOS WHERE username = ?", arrayOf(username))
        var count = 0; if (c1.moveToFirst()) count = c1.getInt(0)
        c1.close()
        // Also count from legacy table
        val c2 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? AND $COL_IMAGE_PATH != ''", arrayOf(username))
        if (c2.moveToFirst()) count += c2.getInt(0)
        c2.close(); db.close(); return count
    }

    fun getWeeklyWorkoutCount(username: String): Int {
        val db = readableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val cursor = db.rawQuery("SELECT $COL_DATE FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        var count = 0
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0); calendar.set(java.util.Calendar.MINUTE, 0); calendar.set(java.util.Calendar.SECOND, 0)
        val startOfWeek = calendar.timeInMillis
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null && date.time >= startOfWeek) count++
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return count
    }

    fun getMonthlyWorkoutCount(username: String): Int {
        val db = readableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val cursor = db.rawQuery("SELECT $COL_DATE FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        var count = 0
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0); calendar.set(java.util.Calendar.MINUTE, 0); calendar.set(java.util.Calendar.SECOND, 0)
        val startOfMonth = calendar.timeInMillis
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null && date.time >= startOfMonth) count++
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return count
    }

    fun getAverageWorkoutDuration(username: String): Int {
        val count = getTotalWorkoutCount(username)
        if (count == 0) return 0
        return getTotalWorkoutDuration(username) / count
    }

    fun getAverageSetsPerWorkout(username: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT AVG($COL_SETS) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        var avg = 0.0; if (cursor.moveToFirst() && !cursor.isNull(0)) avg = cursor.getDouble(0)
        cursor.close(); db.close(); return avg
    }

    fun getAverageReps(username: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT AVG($COL_REPS) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        var avg = 0.0; if (cursor.moveToFirst() && !cursor.isNull(0)) avg = cursor.getDouble(0)
        cursor.close(); db.close(); return avg
    }

    fun getLeastTrainedMuscleGroup(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_MUSCLE_GROUP, COUNT(*) as cnt FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? GROUP BY $COL_MUSCLE_GROUP ORDER BY cnt ASC LIMIT 1", arrayOf(username))
        var muscle = "N/A"; if (cursor.moveToFirst()) muscle = cursor.getString(cursor.getColumnIndexOrThrow(COL_MUSCLE_GROUP)) ?: "N/A"
        cursor.close(); db.close(); return muscle
    }

    fun getMostImprovedExercise(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_EXERCISE_NAME, MAX($COL_WEIGHT) as maxWeight, MIN($COL_WEIGHT) as minWeight FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? AND $COL_WEIGHT > 0 GROUP BY $COL_EXERCISE_NAME", arrayOf(username))
        var maxImprovement = -1.0; var improvedExercise = "N/A"
        if (cursor.moveToFirst()) do {
            val name = cursor.getString(0) ?: continue
            val maxW = cursor.getDouble(1); val minW = cursor.getDouble(2)
            if (minW > 0) { val improvement = (maxW - minW) / minW; if (improvement > maxImprovement) { maxImprovement = improvement; improvedExercise = name } }
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return improvedExercise
    }

    fun getWorkoutsByWeekday(username: String): Map<Int, Int> {
        val map = mutableMapOf<Int, Int>(); for (i in 1..7) map[i] = 0
        val db = readableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val cursor = db.rawQuery("SELECT $COL_DATE FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) { val cal = java.util.Calendar.getInstance().apply { time = date }; val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK); map[dayOfWeek] = map.getOrDefault(dayOfWeek, 0) + 1 }
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return map
    }

    fun getMonthlyWorkoutCounts(username: String): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val monthFormat = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        for (i in 5 downTo 0) { val c = java.util.Calendar.getInstance(); c.add(java.util.Calendar.MONTH, -i); list.add(Pair(monthFormat.format(c.time), 0)) }
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_DATE FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ?", arrayOf(username))
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) {
                val workoutCal = java.util.Calendar.getInstance().apply { time = date }
                for (i in 0 until 6) {
                    val c = java.util.Calendar.getInstance(); c.add(java.util.Calendar.MONTH, -(5 - i))
                    if (workoutCal.get(java.util.Calendar.YEAR) == c.get(java.util.Calendar.YEAR) && workoutCal.get(java.util.Calendar.MONTH) == c.get(java.util.Calendar.MONTH)) {
                        list[i] = list[i].copy(second = list[i].second + 1)
                    }
                }
            }
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getMuscleGroupDistribution(username: String): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_MUSCLE_GROUP, COUNT(*) as cnt FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? GROUP BY $COL_MUSCLE_GROUP ORDER BY cnt DESC", arrayOf(username))
        if (cursor.moveToFirst()) do { list.add(Pair(cursor.getString(0) ?: "Unknown", cursor.getInt(1))) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getWeightProgressForExercise(username: String, exercise: String): List<Pair<String, Double>> {
        val list = mutableListOf<Pair<String, Double>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_DATE, $COL_WEIGHT FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? AND $COL_EXERCISE_NAME = ?", arrayOf(username, exercise))
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val data = mutableListOf<Pair<java.util.Date, Double>>()
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue; val weight = cursor.getDouble(1)
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) data.add(Pair(date, weight))
        } while (cursor.moveToNext())
        cursor.close(); db.close()
        data.sortBy { it.first }; data.forEach { list.add(Pair(sdf.format(it.first), it.second)) }
        return list
    }

    fun getDurationTrend(username: String): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_DATE, SUM($COL_DURATION) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? GROUP BY $COL_DATE", arrayOf(username))
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val data = mutableListOf<Pair<java.util.Date, Int>>()
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue; val duration = cursor.getInt(1)
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) data.add(Pair(date, duration))
        } while (cursor.moveToNext())
        cursor.close(); db.close()
        data.sortBy { it.first }; data.takeLast(10).forEach { list.add(Pair(sdf.format(it.first), it.second)) }
        return list
    }

    fun getVolumeTrend(username: String): List<Pair<String, Double>> {
        val list = mutableListOf<Pair<String, Double>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_DATE, SUM($COL_WEIGHT * $COL_SETS * $COL_REPS) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? GROUP BY $COL_DATE", arrayOf(username))
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val data = mutableListOf<Pair<java.util.Date, Double>>()
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue; val volume = cursor.getDouble(1)
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) data.add(Pair(date, volume))
        } while (cursor.moveToNext())
        cursor.close(); db.close()
        data.sortBy { it.first }; data.takeLast(10).forEach { list.add(Pair(sdf.format(it.first), it.second)) }
        return list
    }

    fun getConsistencyScore(username: String): Int {
        val dates = getWorkoutDatesSet(username); if (dates.isEmpty()) return 0
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val parsedDates = dates.mapNotNull { try { sdf.parse(it) } catch (e: Exception) { null } }.sorted()
        if (parsedDates.isEmpty()) return 0
        val firstDate = parsedDates.first().time; val lastDate = parsedDates.last().time
        if (firstDate == lastDate) return 100
        val weeksDiff = ((lastDate - firstDate) / (1000L * 60 * 60 * 24 * 7)).toInt() + 1
        val weekSet = mutableSetOf<Int>()
        val cal = java.util.Calendar.getInstance()
        parsedDates.forEach { cal.time = it; weekSet.add(cal.get(java.util.Calendar.WEEK_OF_YEAR) + cal.get(java.util.Calendar.YEAR) * 100) }
        return ((weekSet.size.toDouble() / weeksDiff) * 100).toInt().coerceIn(0, 100)
    }

    fun getWeeklyCompletionPercentage(username: String, weeklyGoal: Int): Int {
        if (weeklyGoal <= 0) return 0
        return ((getWeeklyWorkoutCount(username).toDouble() / weeklyGoal) * 100).toInt().coerceAtMost(100)
    }

    fun getExerciseProgress(username: String): List<ExerciseProgress> {
        val names = getAllExerciseNames(username); val progressList = mutableListOf<ExerciseProgress>()
        for (name in names) {
            val weights = getWeightProgressForExercise(username, name); if (weights.isEmpty()) continue
            val bestWeight = weights.maxByOrNull { it.second }?.second ?: 0.0
            val currentWeight = weights.last().second
            val previousWeight = if (weights.size > 1) weights[weights.size - 2].second else currentWeight
            val improvement = if (previousWeight > 0) (currentWeight - previousWeight) / previousWeight else 0.0
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT MAX($COL_WEIGHT * (1 + CAST($COL_REPS AS REAL) / 30.0)) FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? AND $COL_EXERCISE_NAME = ?", arrayOf(username, name))
            var estimated1RM = 0.0; if (cursor.moveToFirst() && !cursor.isNull(0)) estimated1RM = cursor.getDouble(0)
            cursor.close(); db.close()
            progressList.add(ExerciseProgress(name, currentWeight, previousWeight, bestWeight, improvement * 100, weights.size, estimated1RM, weights))
        }
        return progressList
    }

    fun getAllExerciseProgress(username: String): List<ExerciseProgress> = getExerciseProgress(username)

    // ════════════════════════════════════════════════════════════════════════
    // GOALS
    // ════════════════════════════════════════════════════════════════════════

    fun getGoals(username: String): Goal {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Goals WHERE username = ?", arrayOf(username))
        val goal = if (cursor.moveToFirst()) {
            Goal(
                weeklyWorkoutGoal = cursor.getInt(cursor.getColumnIndexOrThrow("weeklyWorkoutGoal")),
                monthlyWorkoutGoal = cursor.getInt(cursor.getColumnIndexOrThrow("monthlyWorkoutGoal")),
                targetWeight = cursor.getDouble(cursor.getColumnIndexOrThrow("targetWeight")),
                targetDuration = cursor.getInt(cursor.getColumnIndexOrThrow("targetDuration"))
            )
        } else Goal()
        cursor.close(); db.close(); return goal
    }

    fun saveGoals(username: String, goal: Goal) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id FROM Goals WHERE username = ?", arrayOf(username))
        val exists = cursor.moveToFirst(); cursor.close()
        val values = ContentValues().apply {
            put("username", username); put("weeklyWorkoutGoal", goal.weeklyWorkoutGoal)
            put("monthlyWorkoutGoal", goal.monthlyWorkoutGoal); put("targetWeight", goal.targetWeight); put("targetDuration", goal.targetDuration)
        }
        if (exists) db.update("Goals", values, "username = ?", arrayOf(username)) else db.insert("Goals", null, values)
        db.close()
    }

    fun saveGoals(goal: Goal, username: String) = saveGoals(username, goal)

    // ════════════════════════════════════════════════════════════════════════
    // ACHIEVEMENTS
    // ════════════════════════════════════════════════════════════════════════

    fun getUnlockedAchievements(username: String): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT achievementKey FROM Achievements WHERE username = ?", arrayOf(username))
        if (cursor.moveToFirst()) do { val key = cursor.getString(0); if (key != null) list.add(key) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun unlockAchievement(username: String, key: String) {
        if (isAchievementUnlocked(username, key)) return
        val db = writableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val values = ContentValues().apply { put("username", username); put("achievementKey", key); put("unlockedAt", sdf.format(java.util.Date())) }
        db.insert("Achievements", null, values); db.close()
    }

    fun isAchievementUnlocked(username: String, key: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM Achievements WHERE username = ? AND achievementKey = ?", arrayOf(username, key))
        val exists = cursor.moveToFirst(); cursor.close(); db.close(); return exists
    }

    fun getAllAchievements(username: String): List<Achievement> {
        val allDefs = listOf(
            Triple("first_workout", "First Workout", "Log your first workout"),
            Triple("week_warrior", "Week Warrior", "Complete 7 workouts"),
            Triple("iron_addict", "Iron Addict", "Complete 25 workouts"),
            Triple("century_club", "Century Club", "Complete 100 workouts"),
            Triple("streak_3", "3-Day Streak", "Maintain a 3-day workout streak"),
            Triple("streak_7", "7-Day Streak", "Maintain a 7-day workout streak"),
            Triple("streak_30", "30-Day Streak", "Maintain a 30-day workout streak"),
            Triple("diversified", "Diversified", "Train 5+ different muscle groups"),
            Triple("heavy_lifter", "Heavy Lifter", "Lift over 1,000 kg total volume"),
            Triple("photographer", "Progress Photographer", "Take your first progress photo"),
            Triple("goal_setter", "Goal Setter", "Set your first fitness goal"),
            Triple("pr_breaker", "PR Breaker", "Set a new personal record")
        )
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT achievementKey, unlockedAt FROM Achievements WHERE username = ?", arrayOf(username))
        val unlockedMap = mutableMapOf<String, String>()
        if (cursor.moveToFirst()) do { val key = cursor.getString(0); val date = cursor.getString(1) ?: ""; if (key != null) unlockedMap[key] = date } while (cursor.moveToNext())
        cursor.close(); db.close()
        return allDefs.map { (key, title, desc) ->
            Achievement(key = key, name = title, description = desc, icon = "ic_achievement", isUnlocked = unlockedMap.containsKey(key), unlockedDate = unlockedMap[key] ?: "")
        }.sortedByDescending { it.isUnlocked }
    }

    // ════════════════════════════════════════════════════════════════════════
    // LEGACY PROGRESS PHOTOS
    // ════════════════════════════════════════════════════════════════════════

    fun getProgressPhotos(username: String): List<Pair<String, String>> {
        val photos = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WORKOUT, arrayOf(COL_DATE, COL_IMAGE_PATH), "$COL_USERNAME = ? AND $COL_IMAGE_PATH != ''", arrayOf(username), null, null, "$COL_DATE DESC")
        if (cursor.moveToFirst()) do {
            val date = cursor.getString(0) ?: ""; val path = cursor.getString(1) ?: ""
            if (path.isNotEmpty()) photos.add(Pair(date, path))
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return photos
    }

    fun saveProgressPhoto(username: String, date: String, path: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EXERCISE_NAME, "Progress Photo"); put(COL_MUSCLE_GROUP, "Other"); put(COL_WEIGHT, 0.0)
            put(COL_SETS, 0); put(COL_REPS, 0); put(COL_DURATION, 0)
            put(COL_DATE, date); put(COL_NOTES, "Progress photo"); put(COL_IMAGE_PATH, path); put(COL_USERNAME, username)
        }
        db.insert(TABLE_WORKOUT, null, values); db.close()
    }

    // ════════════════════════════════════════════════════════════════════════
    // WORKOUT SPLITS
    // ════════════════════════════════════════════════════════════════════════

    fun insertSplit(username: String, name: String, description: String, goal: String): Long {
        val db = writableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val values = ContentValues().apply {
            put("username", username); put("name", name); put("description", description)
            put("goal", goal); put("isActive", 0); put("createdAt", sdf.format(java.util.Date()))
        }
        val id = db.insert(TABLE_SPLITS, null, values); db.close(); return id
    }

    fun updateSplit(split: WorkoutSplit): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", split.name); put("description", split.description)
            put("goal", split.goal); put("isActive", if (split.isActive) 1 else 0)
        }
        val result = db.update(TABLE_SPLITS, values, "id = ?", arrayOf(split.id.toString()))
        db.close(); return result
    }

    fun deleteSplit(splitId: Int) {
        val db = writableDatabase
        // Cascade delete days and exercises
        val dayCursor = db.rawQuery("SELECT id FROM $TABLE_WORKOUT_DAYS WHERE splitId = ?", arrayOf(splitId.toString()))
        if (dayCursor.moveToFirst()) do {
            val dayId = dayCursor.getInt(0)
            db.delete(TABLE_DAY_EXERCISES, "dayId = ?", arrayOf(dayId.toString()))
        } while (dayCursor.moveToNext())
        dayCursor.close()
        db.delete(TABLE_WORKOUT_DAYS, "splitId = ?", arrayOf(splitId.toString()))
        db.delete(TABLE_WEEKLY_SCHEDULE, "splitId = ?", arrayOf(splitId.toString()))
        db.delete(TABLE_SPLITS, "id = ?", arrayOf(splitId.toString()))
        db.close()
    }

    fun getSplitsForUser(username: String): List<WorkoutSplit> {
        val list = mutableListOf<WorkoutSplit>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SPLITS WHERE username = ? ORDER BY isActive DESC, createdAt DESC", arrayOf(username))
        if (cursor.moveToFirst()) do { list.add(cursorToSplit(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getActiveSplit(username: String): WorkoutSplit? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SPLITS WHERE username = ? AND isActive = 1 LIMIT 1", arrayOf(username))
        var split: WorkoutSplit? = null
        if (cursor.moveToFirst()) split = cursorToSplit(cursor)
        cursor.close(); db.close(); return split
    }

    fun getSplitById(splitId: Int): WorkoutSplit? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SPLITS WHERE id = ?", arrayOf(splitId.toString()))
        var split: WorkoutSplit? = null
        if (cursor.moveToFirst()) split = cursorToSplit(cursor)
        cursor.close(); db.close(); return split
    }

    fun setActiveSplit(username: String, splitId: Int) {
        val db = writableDatabase
        val clearValues = ContentValues().apply { put("isActive", 0) }
        db.update(TABLE_SPLITS, clearValues, "username = ?", arrayOf(username))
        val setValues = ContentValues().apply { put("isActive", 1) }
        db.update(TABLE_SPLITS, setValues, "id = ?", arrayOf(splitId.toString()))
        db.close()
    }

    fun duplicateSplit(splitId: Int, username: String): Long {
        val original = getSplitById(splitId) ?: return -1
        val newId = insertSplit(username, "${original.name} (Copy)", original.description, original.goal)
        val days = getWorkoutDaysForSplit(splitId)
        for (day in days) {
            val newDayId = insertWorkoutDay(newId.toInt(), day.dayName, day.muscleGroups, day.estimatedDuration, day.notes, day.sortOrder)
            val exercises = getDayExercisesForDay(day.id)
            for (ex in exercises) {
                insertDayExercise(newDayId.toInt(), ex.exerciseName, ex.muscleGroup, ex.equipment, ex.defaultSets, ex.defaultReps, ex.sortOrder, ex.notes)
            }
        }
        return newId
    }

    // ════════════════════════════════════════════════════════════════════════
    // WORKOUT DAYS
    // ════════════════════════════════════════════════════════════════════════

    fun insertWorkoutDay(splitId: Int, dayName: String, muscleGroups: String, estimatedDuration: Int = 60, notes: String = "", sortOrder: Int = 0): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("splitId", splitId); put("dayName", dayName); put("muscleGroups", muscleGroups)
            put("estimatedDuration", estimatedDuration); put("notes", notes); put("sortOrder", sortOrder)
        }
        val id = db.insert(TABLE_WORKOUT_DAYS, null, values); db.close(); return id
    }

    fun updateWorkoutDay(day: WorkoutDay): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("dayName", day.dayName); put("muscleGroups", day.muscleGroups)
            put("estimatedDuration", day.estimatedDuration); put("notes", day.notes); put("sortOrder", day.sortOrder)
        }
        val result = db.update(TABLE_WORKOUT_DAYS, values, "id = ?", arrayOf(day.id.toString()))
        db.close(); return result
    }

    fun deleteWorkoutDay(dayId: Int) {
        val db = writableDatabase
        db.delete(TABLE_DAY_EXERCISES, "dayId = ?", arrayOf(dayId.toString()))
        db.delete(TABLE_WORKOUT_DAYS, "id = ?", arrayOf(dayId.toString()))
        db.close()
    }

    fun getWorkoutDaysForSplit(splitId: Int): List<WorkoutDay> {
        val list = mutableListOf<WorkoutDay>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_WORKOUT_DAYS WHERE splitId = ? ORDER BY sortOrder ASC", arrayOf(splitId.toString()))
        if (cursor.moveToFirst()) do { list.add(cursorToWorkoutDay(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getWorkoutDayById(dayId: Int): WorkoutDay? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_WORKOUT_DAYS WHERE id = ?", arrayOf(dayId.toString()))
        var day: WorkoutDay? = null
        if (cursor.moveToFirst()) day = cursorToWorkoutDay(cursor)
        cursor.close(); db.close(); return day
    }

    // ════════════════════════════════════════════════════════════════════════
    // DAY EXERCISES
    // ════════════════════════════════════════════════════════════════════════

    fun insertDayExercise(dayId: Int, exerciseName: String, muscleGroup: String, equipment: String = "", sets: Int = 3, reps: Int = 10, sortOrder: Int = 0, notes: String = ""): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("dayId", dayId); put("exerciseName", exerciseName); put("muscleGroup", muscleGroup)
            put("equipment", equipment); put("defaultSets", sets); put("defaultReps", reps)
            put("sortOrder", sortOrder); put("notes", notes); put("isFavorite", 0)
        }
        val id = db.insert(TABLE_DAY_EXERCISES, null, values); db.close(); return id
    }

    fun updateDayExercise(exercise: DayExercise): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("exerciseName", exercise.exerciseName); put("muscleGroup", exercise.muscleGroup)
            put("equipment", exercise.equipment); put("defaultSets", exercise.defaultSets)
            put("defaultReps", exercise.defaultReps); put("sortOrder", exercise.sortOrder)
            put("notes", exercise.notes); put("isFavorite", if (exercise.isFavorite) 1 else 0)
        }
        val result = db.update(TABLE_DAY_EXERCISES, values, "id = ?", arrayOf(exercise.id.toString()))
        db.close(); return result
    }

    fun deleteDayExercise(exerciseId: Int) {
        val db = writableDatabase
        db.delete(TABLE_DAY_EXERCISES, "id = ?", arrayOf(exerciseId.toString()))
        db.close()
    }

    fun getDayExercisesForDay(dayId: Int): List<DayExercise> {
        val list = mutableListOf<DayExercise>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_DAY_EXERCISES WHERE dayId = ? ORDER BY sortOrder ASC", arrayOf(dayId.toString()))
        if (cursor.moveToFirst()) do { list.add(cursorToDayExercise(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun reorderDayExercises(exercises: List<DayExercise>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            exercises.forEachIndexed { index, exercise ->
                val values = ContentValues().apply { put("sortOrder", index) }
                db.update(TABLE_DAY_EXERCISES, values, "id = ?", arrayOf(exercise.id.toString()))
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        db.close()
    }

    // ════════════════════════════════════════════════════════════════════════
    // WEEKLY SCHEDULE
    // ════════════════════════════════════════════════════════════════════════

    /** weekday: 1=Mon, 2=Tue, ... 7=Sun. workoutDayId=0 and isRestDay=true means rest */
    fun saveWeeklyScheduleEntry(username: String, splitId: Int, weekday: Int, workoutDayId: Int, isRestDay: Boolean) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id FROM $TABLE_WEEKLY_SCHEDULE WHERE username = ? AND splitId = ? AND weekday = ?", arrayOf(username, splitId.toString(), weekday.toString()))
        val exists = cursor.moveToFirst(); val existingId = if (exists) cursor.getInt(0) else -1
        cursor.close()
        val values = ContentValues().apply {
            put("username", username); put("splitId", splitId); put("weekday", weekday)
            put("workoutDayId", workoutDayId); put("isRestDay", if (isRestDay) 1 else 0)
        }
        if (exists) db.update(TABLE_WEEKLY_SCHEDULE, values, "id = ?", arrayOf(existingId.toString()))
        else db.insert(TABLE_WEEKLY_SCHEDULE, null, values)
        db.close()
    }

    fun getWeeklyScheduleForSplit(username: String, splitId: Int): Map<Int, WorkoutDay?> {
        val result = mutableMapOf<Int, WorkoutDay?>()
        for (i in 1..7) result[i] = null
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT weekday, workoutDayId, isRestDay FROM $TABLE_WEEKLY_SCHEDULE WHERE username = ? AND splitId = ?", arrayOf(username, splitId.toString()))
        if (cursor.moveToFirst()) do {
            val weekday = cursor.getInt(0); val dayId = cursor.getInt(1); val isRest = cursor.getInt(2) == 1
            if (!isRest && dayId > 0) result[weekday] = getWorkoutDayById(dayId)
            else result[weekday] = null
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return result
    }

    /** Returns today's WorkoutDay from the active split's weekly schedule, or null if rest day */
    fun getTodayScheduledWorkoutDay(username: String): WorkoutDay? {
        val activeSplit = getActiveSplit(username) ?: return null
        val cal = java.util.Calendar.getInstance()
        // Calendar.DAY_OF_WEEK: 1=Sun,2=Mon,...7=Sat → convert to 1=Mon,...7=Sun
        val javaDow = cal.get(java.util.Calendar.DAY_OF_WEEK)
        val weekday = if (javaDow == java.util.Calendar.SUNDAY) 7 else javaDow - 1
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT workoutDayId, isRestDay FROM $TABLE_WEEKLY_SCHEDULE WHERE username = ? AND splitId = ? AND weekday = ?", arrayOf(username, activeSplit.id.toString(), weekday.toString()))
        var result: WorkoutDay? = null
        if (cursor.moveToFirst()) {
            val dayId = cursor.getInt(0); val isRest = cursor.getInt(1) == 1
            if (!isRest && dayId > 0) result = getWorkoutDayById(dayId)
        }
        cursor.close(); db.close(); return result
    }

    fun deleteWeeklyScheduleForSplit(username: String, splitId: Int) {
        val db = writableDatabase
        db.delete(TABLE_WEEKLY_SCHEDULE, "username = ? AND splitId = ?", arrayOf(username, splitId.toString()))
        db.close()
    }

    // ════════════════════════════════════════════════════════════════════════
    // WORKOUT SESSIONS
    // ════════════════════════════════════════════════════════════════════════

    fun insertSession(session: WorkoutSession): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", session.username); put("workoutDayId", session.workoutDayId)
            put("workoutDayName", session.workoutDayName); put("date", session.date)
            put("startTime", session.startTime); put("endTime", session.endTime)
            put("durationMinutes", session.durationMinutes); put("totalVolume", session.totalVolume)
            put("notes", session.notes); put("mood", session.mood); put("energyLevel", session.energyLevel)
            put("sleepQuality", session.sleepQuality); put("status", session.status); put("caloriesBurned", session.caloriesBurned)
        }
        val id = db.insert(TABLE_SESSIONS, null, values); db.close(); return id
    }

    fun updateSession(session: WorkoutSession): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("workoutDayName", session.workoutDayName); put("durationMinutes", session.durationMinutes)
            put("totalVolume", session.totalVolume); put("notes", session.notes); put("mood", session.mood)
            put("energyLevel", session.energyLevel); put("sleepQuality", session.sleepQuality)
            put("endTime", session.endTime); put("status", session.status); put("caloriesBurned", session.caloriesBurned)
        }
        val result = db.update(TABLE_SESSIONS, values, "id = ?", arrayOf(session.id.toString()))
        db.close(); return result
    }

    fun getSessionById(sessionId: Int): WorkoutSession? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SESSIONS WHERE id = ?", arrayOf(sessionId.toString()))
        var session: WorkoutSession? = null
        if (cursor.moveToFirst()) session = cursorToSession(cursor)
        cursor.close(); db.close(); return session
    }

    fun getSessionsForUser(username: String): List<WorkoutSession> {
        val list = mutableListOf<WorkoutSession>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SESSIONS WHERE username = ? ORDER BY date DESC", arrayOf(username))
        if (cursor.moveToFirst()) do { list.add(cursorToSession(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getLastSessionForWorkoutDay(username: String, workoutDayId: Int): WorkoutSession? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SESSIONS WHERE username = ? AND workoutDayId = ? AND status = 'completed' ORDER BY date DESC LIMIT 1", arrayOf(username, workoutDayId.toString()))
        var session: WorkoutSession? = null
        if (cursor.moveToFirst()) session = cursorToSession(cursor)
        cursor.close(); db.close(); return session
    }

    fun getSessionsOnDate(username: String, date: String): List<WorkoutSession> {
        val list = mutableListOf<WorkoutSession>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SESSIONS WHERE username = ? AND date = ?", arrayOf(username, date))
        if (cursor.moveToFirst()) do { list.add(cursorToSession(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun deleteSession(sessionId: Int) {
        val db = writableDatabase
        db.delete(TABLE_SESSION_LOGS, "sessionId = ?", arrayOf(sessionId.toString()))
        db.delete(TABLE_SESSIONS, "id = ?", arrayOf(sessionId.toString()))
        db.close()
    }

    fun getTotalSessionCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_SESSIONS WHERE username = ? AND status = 'completed'", arrayOf(username))
        var count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close(); db.close(); return count
    }

    fun getSessionVolumeTrend(username: String): List<Pair<String, Double>> {
        val list = mutableListOf<Pair<String, Double>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT date, totalVolume FROM $TABLE_SESSIONS WHERE username = ? AND status = 'completed' ORDER BY date DESC LIMIT 10", arrayOf(username))
        if (cursor.moveToFirst()) do { list.add(Pair(cursor.getString(0) ?: "", cursor.getDouble(1))) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list.reversed()
    }

    // ════════════════════════════════════════════════════════════════════════
    // SESSION EXERCISE LOGS
    // ════════════════════════════════════════════════════════════════════════

    fun insertSessionExerciseLog(log: SessionExerciseLog): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("sessionId", log.sessionId); put("exerciseName", log.exerciseName)
            put("muscleGroup", log.muscleGroup); put("weight", log.weight); put("sets", log.sets)
            put("reps", log.reps); put("notes", log.notes); put("isNewPR", if (log.isNewPR) 1 else 0)
            put("sortOrder", log.sortOrder)
        }
        val id = db.insert(TABLE_SESSION_LOGS, null, values); db.close(); return id
    }

    fun getLogsForSession(sessionId: Int): List<SessionExerciseLog> {
        val list = mutableListOf<SessionExerciseLog>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SESSION_LOGS WHERE sessionId = ? ORDER BY sortOrder ASC", arrayOf(sessionId.toString()))
        if (cursor.moveToFirst()) do { list.add(cursorToSessionLog(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getExerciseSessionHistory(username: String, exerciseName: String): List<SessionExerciseLog> {
        val list = mutableListOf<SessionExerciseLog>()
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT l.*, s.date FROM $TABLE_SESSION_LOGS l
            INNER JOIN $TABLE_SESSIONS s ON l.sessionId = s.id
            WHERE s.username = ? AND l.exerciseName = ?
            ORDER BY s.date DESC
        """.trimIndent(), arrayOf(username, exerciseName))
        if (cursor.moveToFirst()) do {
            val log = cursorToSessionLog(cursor)
            val dateIdx = cursor.getColumnIndex("date")
            val dateVal = if (dateIdx >= 0) cursor.getString(dateIdx) ?: "" else ""
            list.add(log.copy(date = dateVal))
        } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    // ════════════════════════════════════════════════════════════════════════
    // EXERCISE LIBRARY
    // ════════════════════════════════════════════════════════════════════════

    fun insertExerciseToLibrary(exercise: ExerciseInfo): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", exercise.name); put("muscleGroup", exercise.muscleGroup); put("equipment", exercise.equipment)
            put("difficulty", exercise.difficulty); put("category", exercise.category); put("description", exercise.description)
            put("isFavorite", if (exercise.isFavorite) 1 else 0); put("isCustom", if (exercise.isCustom) 1 else 0); put("username", exercise.username)
        }
        val id = db.insert(TABLE_EXERCISE_LIB, null, values); db.close(); return id
    }

    fun getExerciseLibrary(username: String): List<ExerciseInfo> {
        val list = mutableListOf<ExerciseInfo>()
        val db = readableDatabase
        // Global exercises (username = '') + user-custom exercises
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EXERCISE_LIB WHERE username = '' OR username = ? ORDER BY isFavorite DESC, muscleGroup ASC, name ASC", arrayOf(username))
        if (cursor.moveToFirst()) do { list.add(cursorToExerciseInfo(cursor, username)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun searchExerciseLibrary(query: String, username: String): List<ExerciseInfo> {
        val list = mutableListOf<ExerciseInfo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EXERCISE_LIB WHERE (username = '' OR username = ?) AND name LIKE ? ORDER BY isFavorite DESC, name ASC", arrayOf(username, "%$query%"))
        if (cursor.moveToFirst()) do { list.add(cursorToExerciseInfo(cursor, username)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getExercisesByMuscleGroup(muscleGroup: String, username: String): List<ExerciseInfo> {
        val list = mutableListOf<ExerciseInfo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EXERCISE_LIB WHERE (username = '' OR username = ?) AND muscleGroup = ? ORDER BY isFavorite DESC, name ASC", arrayOf(username, muscleGroup))
        if (cursor.moveToFirst()) do { list.add(cursorToExerciseInfo(cursor, username)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun toggleFavoriteExercise(exerciseName: String, username: String) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id, isFavorite FROM $TABLE_EXERCISE_LIB WHERE name = ? AND (username = '' OR username = ?) LIMIT 1", arrayOf(exerciseName, username))
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0); val current = cursor.getInt(1)
            val values = ContentValues().apply { put("isFavorite", if (current == 1) 0 else 1) }
            db.update(TABLE_EXERCISE_LIB, values, "id = ?", arrayOf(id.toString()))
        }
        cursor.close(); db.close()
    }

    fun isExerciseFavorite(exerciseName: String, username: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT isFavorite FROM $TABLE_EXERCISE_LIB WHERE name = ? AND (username = '' OR username = ?) LIMIT 1", arrayOf(exerciseName, username))
        var isFav = false
        if (cursor.moveToFirst()) isFav = cursor.getInt(0) == 1
        cursor.close(); db.close(); return isFav
    }

    fun deleteCustomExercise(exerciseId: Int) {
        val db = writableDatabase
        db.delete(TABLE_EXERCISE_LIB, "id = ? AND isCustom = 1", arrayOf(exerciseId.toString()))
        db.close()
    }

    fun isExerciseLibrarySeeded(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_EXERCISE_LIB WHERE username = ''", null)
        var count = 0; if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close(); db.close(); return count > 0
    }

    // ════════════════════════════════════════════════════════════════════════
    // BODY MEASUREMENTS
    // ════════════════════════════════════════════════════════════════════════

    fun insertMeasurement(measurement: BodyMeasurement): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", measurement.username); put("date", measurement.date); put("time", measurement.time)
            put("bodyWeight", measurement.bodyWeight); put("chest", measurement.chest); put("waist", measurement.waist)
            put("hips", measurement.hips); put("leftArm", measurement.leftArm); put("rightArm", measurement.rightArm)
            put("leftForearm", measurement.leftForearm); put("rightForearm", measurement.rightForearm)
            put("leftThigh", measurement.leftThigh); put("rightThigh", measurement.rightThigh)
            put("leftCalf", measurement.leftCalf); put("rightCalf", measurement.rightCalf)
            put("neck", measurement.neck); put("shoulderWidth", measurement.shoulderWidth)
            put("bodyFat", measurement.bodyFat); put("notes", measurement.notes)
        }
        val id = db.insert(TABLE_MEASUREMENTS, null, values); db.close(); return id
    }

    fun updateMeasurement(measurement: BodyMeasurement): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("date", measurement.date); put("time", measurement.time)
            put("bodyWeight", measurement.bodyWeight); put("chest", measurement.chest); put("waist", measurement.waist)
            put("hips", measurement.hips); put("leftArm", measurement.leftArm); put("rightArm", measurement.rightArm)
            put("leftForearm", measurement.leftForearm); put("rightForearm", measurement.rightForearm)
            put("leftThigh", measurement.leftThigh); put("rightThigh", measurement.rightThigh)
            put("leftCalf", measurement.leftCalf); put("rightCalf", measurement.rightCalf)
            put("neck", measurement.neck); put("shoulderWidth", measurement.shoulderWidth)
            put("bodyFat", measurement.bodyFat); put("notes", measurement.notes)
        }
        val result = db.update(TABLE_MEASUREMENTS, values, "id = ?", arrayOf(measurement.id.toString()))
        db.close(); return result
    }

    fun deleteMeasurement(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_MEASUREMENTS, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getMeasurementsForUser(username: String): List<BodyMeasurement> {
        val list = mutableListOf<BodyMeasurement>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MEASUREMENTS WHERE username = ? ORDER BY date DESC, time DESC", arrayOf(username))
        if (cursor.moveToFirst()) do { list.add(cursorToMeasurement(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getLatestMeasurement(username: String): BodyMeasurement? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MEASUREMENTS WHERE username = ? ORDER BY date DESC, time DESC LIMIT 1", arrayOf(username))
        var m: BodyMeasurement? = null
        if (cursor.moveToFirst()) m = cursorToMeasurement(cursor)
        cursor.close(); db.close(); return m
    }

    fun getPreviousMeasurement(username: String): BodyMeasurement? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MEASUREMENTS WHERE username = ? ORDER BY date DESC, time DESC LIMIT 2", arrayOf(username))
        var m: BodyMeasurement? = null
        if (cursor.moveToFirst() && cursor.moveToNext()) m = cursorToMeasurement(cursor)
        cursor.close(); db.close(); return m
    }

    fun getMeasurementTrend(username: String, field: String): List<Pair<String, Double>> {
        val safeField = field.replace(Regex("[^a-zA-Z]"), "")
        val list = mutableListOf<Pair<String, Double>>()
        val db = readableDatabase
        try {
            val cursor = db.rawQuery("SELECT date, $safeField FROM $TABLE_MEASUREMENTS WHERE username = ? AND $safeField > 0 ORDER BY date ASC", arrayOf(username))
            if (cursor.moveToFirst()) do { list.add(Pair(cursor.getString(0) ?: "", cursor.getDouble(1))) } while (cursor.moveToNext())
            cursor.close()
        } catch (e: Exception) { e.printStackTrace() }
        db.close(); return list
    }

    // ════════════════════════════════════════════════════════════════════════
    // PROGRESS PHOTOS (New Table)
    // ════════════════════════════════════════════════════════════════════════

    fun insertProgressPhotoNew(photo: ProgressPhoto): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", photo.username); put("date", photo.date); put("month", photo.month)
            put("category", photo.category); put("filePath", photo.filePath)
            put("bodyWeight", photo.bodyWeight); put("bodyFat", photo.bodyFat)
            put("measurementId", photo.measurementId); put("notes", photo.notes)
        }
        val id = db.insert(TABLE_PHOTOS, null, values); db.close(); return id
    }

    fun deleteProgressPhotoNew(photoId: Int) {
        val db = writableDatabase
        db.delete(TABLE_PHOTOS, "id = ?", arrayOf(photoId.toString()))
        db.close()
    }

    fun getProgressPhotosNew(username: String): List<ProgressPhoto> {
        val list = mutableListOf<ProgressPhoto>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PHOTOS WHERE username = ? ORDER BY date DESC", arrayOf(username))
        if (cursor.moveToFirst()) do { list.add(cursorToProgressPhoto(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getProgressPhotosByMonth(username: String, month: String): List<ProgressPhoto> {
        val list = mutableListOf<ProgressPhoto>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PHOTOS WHERE username = ? AND month = ? ORDER BY date DESC", arrayOf(username, month))
        if (cursor.moveToFirst()) do { list.add(cursorToProgressPhoto(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getAvailablePhotoMonths(username: String): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT month FROM $TABLE_PHOTOS WHERE username = ? AND month != '' ORDER BY date DESC", arrayOf(username))
        if (cursor.moveToFirst()) do { val m = cursor.getString(0); if (m != null) list.add(m) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun getPhotoByMonthAndCategory(username: String, month: String, category: String): ProgressPhoto? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PHOTOS WHERE username = ? AND month = ? AND category = ? ORDER BY date DESC LIMIT 1", arrayOf(username, month, category))
        var photo: ProgressPhoto? = null
        if (cursor.moveToFirst()) photo = cursorToProgressPhoto(cursor)
        cursor.close(); db.close(); return photo
    }

    // ════════════════════════════════════════════════════════════════════════
    // PLANNED WORKOUTS
    // ════════════════════════════════════════════════════════════════════════

    fun insertPlannedWorkout(plan: PlannedWorkout): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", plan.username); put("date", plan.date)
            put("workoutDayId", plan.workoutDayId); put("workoutDayName", plan.workoutDayName)
            put("status", plan.status); put("sessionId", plan.sessionId); put("notes", plan.notes)
        }
        val id = db.insert(TABLE_PLANNED, null, values); db.close(); return id
    }

    fun updatePlannedWorkoutStatus(username: String, date: String, status: String, sessionId: Int = 0) {
        val db = writableDatabase
        val values = ContentValues().apply { put("status", status); if (sessionId > 0) put("sessionId", sessionId) }
        db.update(TABLE_PLANNED, values, "username = ? AND date = ?", arrayOf(username, date))
        db.close()
    }

    fun getPlannedWorkoutForDate(username: String, date: String): PlannedWorkout? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PLANNED WHERE username = ? AND date = ? LIMIT 1", arrayOf(username, date))
        var plan: PlannedWorkout? = null
        if (cursor.moveToFirst()) plan = cursorToPlannedWorkout(cursor)
        cursor.close(); db.close(); return plan
    }

    fun getPlannedWorkoutsForMonth(username: String, monthYear: String): List<PlannedWorkout> {
        val list = mutableListOf<PlannedWorkout>()
        val db = readableDatabase
        // monthYear format: "MM/yyyy"
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PLANNED WHERE username = ? AND date LIKE ?", arrayOf(username, "%/$monthYear"))
        if (cursor.moveToFirst()) do { list.add(cursorToPlannedWorkout(cursor)) } while (cursor.moveToNext())
        cursor.close(); db.close(); return list
    }

    fun deletePlannedWorkout(username: String, date: String) {
        val db = writableDatabase
        db.delete(TABLE_PLANNED, "username = ? AND date = ?", arrayOf(username, date))
        db.close()
    }

    /** Checks both sessions and planned table to determine calendar day status */
    fun getCalendarDayStatus(username: String, date: String): String {
        // Check sessions table first
        val db = readableDatabase
        val sessionCursor = db.rawQuery("SELECT status FROM $TABLE_SESSIONS WHERE username = ? AND date = ? LIMIT 1", arrayOf(username, date))
        if (sessionCursor.moveToFirst()) {
            val status = sessionCursor.getString(0) ?: "completed"
            sessionCursor.close()
            // Also check if any PR was set
            val prCursor = db.rawQuery("SELECT id FROM $TABLE_SESSION_LOGS WHERE sessionId IN (SELECT id FROM $TABLE_SESSIONS WHERE username = ? AND date = ?) AND isNewPR = 1 LIMIT 1", arrayOf(username, date))
            val hasPR = prCursor.moveToFirst(); prCursor.close()
            db.close()
            return if (hasPR) "pr" else status
        }
        sessionCursor.close()
        // Check legacy workout table
        val legacyCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_WORKOUT WHERE username = ? AND date = ?", arrayOf(username, date))
        var legacyCount = 0; if (legacyCursor.moveToFirst()) legacyCount = legacyCursor.getInt(0)
        legacyCursor.close()
        if (legacyCount > 0) { db.close(); return "completed" }
        // Check planned table
        val planCursor = db.rawQuery("SELECT status FROM $TABLE_PLANNED WHERE username = ? AND date = ? LIMIT 1", arrayOf(username, date))
        var planStatus = ""
        if (planCursor.moveToFirst()) planStatus = planCursor.getString(0) ?: ""
        planCursor.close(); db.close()
        return planStatus.ifEmpty { "none" }
    }

    fun getMonthCompletionStats(username: String, monthYear: String): Map<String, Int> {
        val db = readableDatabase
        val map = mutableMapOf("planned" to 0, "completed" to 0, "missed" to 0, "rest" to 0, "skipped" to 0)
        val cursor = db.rawQuery("SELECT status, COUNT(*) as cnt FROM $TABLE_PLANNED WHERE username = ? AND date LIKE ? GROUP BY status", arrayOf(username, "%/$monthYear"))
        if (cursor.moveToFirst()) do {
            val status = cursor.getString(0) ?: continue
            map[status] = cursor.getInt(1)
        } while (cursor.moveToNext())
        cursor.close()
        // Count completed from sessions
        val sessionCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_SESSIONS WHERE username = ? AND date LIKE ? AND status = 'completed'", arrayOf(username, "%/$monthYear"))
        if (sessionCursor.moveToFirst()) map["completed"] = sessionCursor.getInt(0)
        sessionCursor.close()
        db.close(); return map
    }

    // ════════════════════════════════════════════════════════════════════════
    // EXERCISE MILESTONES
    // ════════════════════════════════════════════════════════════════════════

    fun unlockExerciseMilestone(username: String, exerciseName: String, milestoneKey: String) {
        if (isExerciseMilestoneUnlocked(username, exerciseName, milestoneKey)) return
        val db = writableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val values = ContentValues().apply {
            put("username", username); put("exerciseName", exerciseName)
            put("milestoneKey", milestoneKey); put("unlockedAt", sdf.format(java.util.Date()))
        }
        db.insert(TABLE_MILESTONES, null, values); db.close()
    }

    fun isExerciseMilestoneUnlocked(username: String, exerciseName: String, key: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM $TABLE_MILESTONES WHERE username = ? AND exerciseName = ? AND milestoneKey = ?", arrayOf(username, exerciseName, key))
        val exists = cursor.moveToFirst(); cursor.close(); db.close(); return exists
    }

    fun getMilestonesForExercise(username: String, exerciseName: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT milestoneKey, unlockedAt FROM $TABLE_MILESTONES WHERE username = ? AND exerciseName = ?", arrayOf(username, exerciseName))
        if (cursor.moveToFirst()) do { map[cursor.getString(0) ?: continue] = cursor.getString(1) ?: "" } while (cursor.moveToNext())
        cursor.close(); db.close(); return map
    }

    // ════════════════════════════════════════════════════════════════════════
    // EXERCISE PERFORMANCE ANALYTICS
    // ════════════════════════════════════════════════════════════════════════

    data class ExerciseStats(
        val exerciseName: String,
        val totalSessions: Int,
        val lastPerformedDate: String,
        val daysSinceLastSession: Int,
        val currentWeight: Double,
        val bestWeight: Double,
        val avgWeight: Double,
        val maxReps: Int,
        val avgReps: Double,
        val avgSets: Double,
        val bestVolumeSingle: Double,   // best volume in a single session
        val avgVolume: Double,
        val totalVolume: Double,
        val totalReps: Int,
        val totalSets: Int,
        val estimated1RM: Double,
        val weightHistory: List<Pair<String, Double>>,
        val volumeHistory: List<Pair<String, Double>>
    )

    fun getExerciseStats(username: String, exerciseName: String): ExerciseStats {
        val history = getExerciseSessionHistory(username, exerciseName)
        val legacyHistory = getWeightProgressForExercise(username, exerciseName)

        // Combine session logs and legacy workout data
        val allWeights = history.map { it.weight } + legacyHistory.map { it.second }
        val bestWeight = if (allWeights.isNotEmpty()) allWeights.max() else 0.0
        val avgWeight = if (allWeights.isNotEmpty()) allWeights.average() else 0.0
        val currentWeight = history.firstOrNull()?.weight ?: legacyHistory.lastOrNull()?.second ?: 0.0

        val totalSessions = history.map { it.sessionId }.distinct().size +
                if (legacyHistory.isNotEmpty() && history.isEmpty()) legacyHistory.size else 0
        val maxReps = history.maxOfOrNull { it.reps } ?: 0
        val avgReps = if (history.isNotEmpty()) history.map { it.reps }.average() else 0.0
        val avgSets = if (history.isNotEmpty()) history.map { it.sets }.average() else 0.0

        val volumes = history.map { it.weight * it.sets * it.reps }
        val totalVolume = volumes.sum() + legacyHistory.sumOf { it.second }
        val bestVolumeSingle = volumes.maxOrNull() ?: 0.0
        val avgVolume = volumes.average().takeIf { volumes.isNotEmpty() } ?: 0.0
        val totalReps = history.sumOf { it.reps }
        val totalSets = history.sumOf { it.sets }

        val est1RM = if (bestWeight > 0 && maxReps > 0) bestWeight * (1 + maxReps / 30.0) else 0.0

        val lastDate = history.firstOrNull()?.date ?: legacyHistory.lastOrNull()?.first ?: ""
        val daysSince = if (lastDate.isNotEmpty()) {
            try {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val d = sdf.parse(lastDate)
                if (d != null) ((System.currentTimeMillis() - d.time) / (1000 * 60 * 60 * 24)).toInt() else 0
            } catch (e: Exception) { 0 }
        } else 0

        val weightHistory = (history.map { Pair(it.date, it.weight) } + legacyHistory).sortedBy { it.first }
        val volumeHistory = history.groupBy { it.date }.map { (date, logs) -> Pair(date, logs.sumOf { it.weight * it.sets * it.reps }) }.sortedBy { it.first }

        return ExerciseStats(exerciseName, totalSessions, lastDate, daysSince, currentWeight, bestWeight, avgWeight, maxReps, avgReps, avgSets, bestVolumeSingle, avgVolume, totalVolume, totalReps, totalSets, est1RM, weightHistory, volumeHistory)
    }

    fun getSuggestedWeight(username: String, exerciseName: String): Double {
        val history = getExerciseSessionHistory(username, exerciseName)
        if (history.isEmpty()) {
            val legacy = getWeightProgressForExercise(username, exerciseName)
            return if (legacy.isNotEmpty()) legacy.last().second + 2.5 else 0.0
        }
        val lastWeight = history.first().weight
        val lastReps = history.first().reps
        val lastSets = history.first().sets
        // Progressive overload: if last session hit target reps/sets, suggest increase
        return if (lastReps >= 10 && lastSets >= 3) lastWeight + 2.5 else lastWeight
    }

    fun getPRHistoryForExercise(username: String, exerciseName: String): List<Pair<String, Double>> {
        val all = (getExerciseSessionHistory(username, exerciseName).map { Pair(it.date, it.weight) } +
                getWeightProgressForExercise(username, exerciseName)).sortedBy { it.first }
        val prHistory = mutableListOf<Pair<String, Double>>()
        var currentPR = 0.0
        for (entry in all) {
            if (entry.second > currentPR) { currentPR = entry.second; prHistory.add(entry) }
        }
        return prHistory
    }

    fun getExerciseComparisonStats(username: String, exerciseName: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val now = java.util.Calendar.getInstance()

        // This week vs last week
        val thisWeekStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0)
        }.timeInMillis
        val lastWeekStart = thisWeekStart - 7 * 24 * 60 * 60 * 1000L

        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val history = getExerciseSessionHistory(username, exerciseName)

        val thisWeek = history.filter { try { (sdf.parse(it.date)?.time ?: 0L) >= thisWeekStart } catch (e: Exception) { false } }
        val lastWeek = history.filter { try { val t = sdf.parse(it.date)?.time ?: 0L; t >= lastWeekStart && t < thisWeekStart } catch (e: Exception) { false } }

        result["thisWeekVolume"] = thisWeek.sumOf { it.weight * it.sets * it.reps }
        result["lastWeekVolume"] = lastWeek.sumOf { it.weight * it.sets * it.reps }
        result["thisWeekBestWeight"] = thisWeek.maxOfOrNull { it.weight } ?: 0.0
        result["lastWeekBestWeight"] = lastWeek.maxOfOrNull { it.weight } ?: 0.0

        return result
    }

    // ════════════════════════════════════════════════════════════════════════
    // INSIGHTS HELPERS (for AdaptiveInsightsEngine)
    // ════════════════════════════════════════════════════════════════════════

    fun getDaysSinceLastTrainedMuscle(username: String, muscleGroup: String): Int {
        val db = readableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val cursor = db.rawQuery("SELECT MAX(date) FROM $TABLE_WORKOUT WHERE username = ? AND muscleGroup = ?", arrayOf(username, muscleGroup))
        var days = -1
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            val dateStr = cursor.getString(0)
            try {
                val d = sdf.parse(dateStr)
                if (d != null) days = ((System.currentTimeMillis() - d.time) / (1000 * 60 * 60 * 24)).toInt()
            } catch (e: Exception) { }
        }
        cursor.close(); db.close(); return days
    }

    fun getExerciseWeightImprovementLastMonth(username: String, exerciseName: String): Double {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val monthAgo = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MONTH, -1) }.time
        val history = getWeightProgressForExercise(username, exerciseName)
        val old = history.filter { try { (sdf.parse(it.first)?.time ?: Long.MAX_VALUE) <= monthAgo.time } catch (e: Exception) { false } }.maxOfOrNull { it.second } ?: 0.0
        val recent = history.maxOfOrNull { it.second } ?: 0.0
        return if (old > 0) recent - old else 0.0
    }

    fun getBodyWeightChangeThisMonth(username: String): Double {
        val measurements = getMeasurementsForUser(username)
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val monthStart = java.util.Calendar.getInstance().apply { set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0) }.time
        val thisMonthMeasurements = measurements.filter { try { (sdf.parse(it.date)?.time ?: 0L) >= monthStart.time } catch (e: Exception) { false } }
        if (thisMonthMeasurements.size < 2) return 0.0
        val earliest = thisMonthMeasurements.minByOrNull { it.date }?.bodyWeight ?: 0.0
        val latest = thisMonthMeasurements.maxByOrNull { it.date }?.bodyWeight ?: 0.0
        return if (earliest > 0 && latest > 0) latest - earliest else 0.0
    }

    fun getMuscleGroupWorkoutCountsThisMonth(username: String): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        val db = readableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val monthStart = java.util.Calendar.getInstance().apply { set(java.util.Calendar.DAY_OF_MONTH, 1) }.time
        val cursor = db.rawQuery("SELECT $COL_MUSCLE_GROUP, COUNT(*) as cnt FROM $TABLE_WORKOUT WHERE $COL_USERNAME = ? GROUP BY $COL_MUSCLE_GROUP", arrayOf(username))
        if (cursor.moveToFirst()) do { map[cursor.getString(0) ?: continue] = cursor.getInt(1) } while (cursor.moveToNext())
        cursor.close(); db.close(); return map
    }

    // ════════════════════════════════════════════════════════════════════════
    // PRIVATE CURSOR HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private fun cursorToWorkout(cursor: Cursor): Workout {
        val imagePathIndex = cursor.getColumnIndex(COL_IMAGE_PATH)
        val usernameIndex = cursor.getColumnIndex(COL_USERNAME)
        return Workout(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
            exerciseName = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXERCISE_NAME)),
            muscleGroup = cursor.getString(cursor.getColumnIndexOrThrow(COL_MUSCLE_GROUP)),
            weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WEIGHT)),
            sets = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SETS)),
            reps = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REPS)),
            duration = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
            notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)) ?: "",
            imagePath = if (imagePathIndex >= 0) cursor.getString(imagePathIndex) ?: "" else "",
            username = if (usernameIndex >= 0) cursor.getString(usernameIndex) ?: "" else ""
        )
    }

    private fun cursorToSplit(cursor: Cursor): WorkoutSplit = WorkoutSplit(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        username = cursor.getString(cursor.getColumnIndexOrThrow("username")) ?: "",
        name = cursor.getString(cursor.getColumnIndexOrThrow("name")) ?: "",
        description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
        goal = cursor.getString(cursor.getColumnIndexOrThrow("goal")) ?: "",
        isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1,
        createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt")) ?: ""
    )

    private fun cursorToWorkoutDay(cursor: Cursor): WorkoutDay = WorkoutDay(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        splitId = cursor.getInt(cursor.getColumnIndexOrThrow("splitId")),
        dayName = cursor.getString(cursor.getColumnIndexOrThrow("dayName")) ?: "",
        muscleGroups = cursor.getString(cursor.getColumnIndexOrThrow("muscleGroups")) ?: "",
        estimatedDuration = cursor.getInt(cursor.getColumnIndexOrThrow("estimatedDuration")),
        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: "",
        sortOrder = cursor.getInt(cursor.getColumnIndexOrThrow("sortOrder"))
    )

    private fun cursorToDayExercise(cursor: Cursor): DayExercise = DayExercise(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        dayId = cursor.getInt(cursor.getColumnIndexOrThrow("dayId")),
        exerciseName = cursor.getString(cursor.getColumnIndexOrThrow("exerciseName")) ?: "",
        muscleGroup = cursor.getString(cursor.getColumnIndexOrThrow("muscleGroup")) ?: "",
        equipment = cursor.getString(cursor.getColumnIndexOrThrow("equipment")) ?: "",
        defaultSets = cursor.getInt(cursor.getColumnIndexOrThrow("defaultSets")),
        defaultReps = cursor.getInt(cursor.getColumnIndexOrThrow("defaultReps")),
        sortOrder = cursor.getInt(cursor.getColumnIndexOrThrow("sortOrder")),
        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: "",
        isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow("isFavorite")) == 1
    )

    private fun cursorToSession(cursor: Cursor): WorkoutSession = WorkoutSession(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        username = cursor.getString(cursor.getColumnIndexOrThrow("username")) ?: "",
        workoutDayId = cursor.getInt(cursor.getColumnIndexOrThrow("workoutDayId")),
        workoutDayName = cursor.getString(cursor.getColumnIndexOrThrow("workoutDayName")) ?: "",
        date = cursor.getString(cursor.getColumnIndexOrThrow("date")) ?: "",
        startTime = cursor.getString(cursor.getColumnIndexOrThrow("startTime")) ?: "",
        endTime = cursor.getString(cursor.getColumnIndexOrThrow("endTime")) ?: "",
        durationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow("durationMinutes")),
        totalVolume = cursor.getDouble(cursor.getColumnIndexOrThrow("totalVolume")),
        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: "",
        mood = cursor.getInt(cursor.getColumnIndexOrThrow("mood")),
        energyLevel = cursor.getInt(cursor.getColumnIndexOrThrow("energyLevel")),
        sleepQuality = cursor.getInt(cursor.getColumnIndexOrThrow("sleepQuality")),
        status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "completed",
        caloriesBurned = cursor.getInt(cursor.getColumnIndexOrThrow("caloriesBurned"))
    )

    private fun cursorToSessionLog(cursor: Cursor): SessionExerciseLog = SessionExerciseLog(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        sessionId = cursor.getInt(cursor.getColumnIndexOrThrow("sessionId")),
        exerciseName = cursor.getString(cursor.getColumnIndexOrThrow("exerciseName")) ?: "",
        muscleGroup = cursor.getString(cursor.getColumnIndexOrThrow("muscleGroup")) ?: "",
        weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight")),
        sets = cursor.getInt(cursor.getColumnIndexOrThrow("sets")),
        reps = cursor.getInt(cursor.getColumnIndexOrThrow("reps")),
        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: "",
        isNewPR = cursor.getInt(cursor.getColumnIndexOrThrow("isNewPR")) == 1,
        sortOrder = cursor.getInt(cursor.getColumnIndexOrThrow("sortOrder"))
    )

    private fun cursorToExerciseInfo(cursor: Cursor, username: String): ExerciseInfo = ExerciseInfo(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        name = cursor.getString(cursor.getColumnIndexOrThrow("name")) ?: "",
        muscleGroup = cursor.getString(cursor.getColumnIndexOrThrow("muscleGroup")) ?: "",
        equipment = cursor.getString(cursor.getColumnIndexOrThrow("equipment")) ?: "",
        difficulty = cursor.getString(cursor.getColumnIndexOrThrow("difficulty")) ?: "Intermediate",
        category = cursor.getString(cursor.getColumnIndexOrThrow("category")) ?: "",
        description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
        isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow("isFavorite")) == 1,
        isCustom = cursor.getInt(cursor.getColumnIndexOrThrow("isCustom")) == 1,
        username = cursor.getString(cursor.getColumnIndexOrThrow("username")) ?: ""
    )

    private fun cursorToMeasurement(cursor: Cursor): BodyMeasurement = BodyMeasurement(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        username = cursor.getString(cursor.getColumnIndexOrThrow("username")) ?: "",
        date = cursor.getString(cursor.getColumnIndexOrThrow("date")) ?: "",
        time = cursor.getString(cursor.getColumnIndexOrThrow("time")) ?: "",
        bodyWeight = cursor.getDouble(cursor.getColumnIndexOrThrow("bodyWeight")),
        chest = cursor.getDouble(cursor.getColumnIndexOrThrow("chest")),
        waist = cursor.getDouble(cursor.getColumnIndexOrThrow("waist")),
        hips = cursor.getDouble(cursor.getColumnIndexOrThrow("hips")),
        leftArm = cursor.getDouble(cursor.getColumnIndexOrThrow("leftArm")),
        rightArm = cursor.getDouble(cursor.getColumnIndexOrThrow("rightArm")),
        leftForearm = cursor.getDouble(cursor.getColumnIndexOrThrow("leftForearm")),
        rightForearm = cursor.getDouble(cursor.getColumnIndexOrThrow("rightForearm")),
        leftThigh = cursor.getDouble(cursor.getColumnIndexOrThrow("leftThigh")),
        rightThigh = cursor.getDouble(cursor.getColumnIndexOrThrow("rightThigh")),
        leftCalf = cursor.getDouble(cursor.getColumnIndexOrThrow("leftCalf")),
        rightCalf = cursor.getDouble(cursor.getColumnIndexOrThrow("rightCalf")),
        neck = cursor.getDouble(cursor.getColumnIndexOrThrow("neck")),
        shoulderWidth = cursor.getDouble(cursor.getColumnIndexOrThrow("shoulderWidth")),
        bodyFat = cursor.getDouble(cursor.getColumnIndexOrThrow("bodyFat")),
        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: ""
    )

    private fun cursorToProgressPhoto(cursor: Cursor): ProgressPhoto = ProgressPhoto(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        username = cursor.getString(cursor.getColumnIndexOrThrow("username")) ?: "",
        date = cursor.getString(cursor.getColumnIndexOrThrow("date")) ?: "",
        month = cursor.getString(cursor.getColumnIndexOrThrow("month")) ?: "",
        category = cursor.getString(cursor.getColumnIndexOrThrow("category")) ?: "front",
        filePath = cursor.getString(cursor.getColumnIndexOrThrow("filePath")) ?: "",
        bodyWeight = cursor.getDouble(cursor.getColumnIndexOrThrow("bodyWeight")),
        bodyFat = cursor.getDouble(cursor.getColumnIndexOrThrow("bodyFat")),
        measurementId = cursor.getInt(cursor.getColumnIndexOrThrow("measurementId")),
        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: ""
    )

    private fun cursorToPlannedWorkout(cursor: Cursor): PlannedWorkout = PlannedWorkout(
        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        username = cursor.getString(cursor.getColumnIndexOrThrow("username")) ?: "",
        date = cursor.getString(cursor.getColumnIndexOrThrow("date")) ?: "",
        workoutDayId = cursor.getInt(cursor.getColumnIndexOrThrow("workoutDayId")),
        workoutDayName = cursor.getString(cursor.getColumnIndexOrThrow("workoutDayName")) ?: "",
        status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "planned",
        sessionId = cursor.getInt(cursor.getColumnIndexOrThrow("sessionId")),
        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: ""
    )

    fun getAllSplitsForUser(username: String): List<WorkoutSplit> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SPLITS WHERE username=? ORDER BY createdAt DESC", arrayOf(username))
        val list = mutableListOf<WorkoutSplit>()
        while (cursor.moveToNext()) list.add(cursorToSplit(cursor))
        cursor.close()
        return list
    }
}
