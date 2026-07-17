package com.fittrack.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FitTrack.db"
        private const val DATABASE_VERSION = 4          // bumped for username column & goals/achievements
        private const val TABLE_NAME = "Workout"
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
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_NAME (
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COL_IMAGE_PATH TEXT DEFAULT ''")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COL_USERNAME TEXT DEFAULT ''")
        }
        if (oldVersion < 4) {
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
        }
    }

    // ─── CRUD ───────────────────────────────────────────────────────────────

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
        val result = db.insert(TABLE_NAME, null, values)
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
        val result = db.update(TABLE_NAME, values, "$COL_ID = ?", arrayOf(workout.id.toString()))
        db.close()
        return result
    }

    fun deleteWorkout(id: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun getWorkoutById(id: Int): Workout? {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, "$COL_ID = ?", arrayOf(id.toString()), null, null, null)
        var workout: Workout? = null
        if (cursor.moveToFirst()) workout = cursorToWorkout(cursor)
        cursor.close()
        db.close()
        return workout
    }

    fun getAllWorkouts(username: String): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, "$COL_USERNAME = ?", arrayOf(username), null, null, "$COL_DATE DESC")
        if (cursor.moveToFirst()) do { workouts.add(cursorToWorkout(cursor)) } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return workouts
    }

    fun searchWorkouts(query: String, username: String): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, "$COL_USERNAME = ? AND $COL_EXERCISE_NAME LIKE ?", arrayOf(username, "%$query%"), null, null, "$COL_DATE DESC")
        if (cursor.moveToFirst()) do { workouts.add(cursorToWorkout(cursor)) } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return workouts
    }

    fun getPersonalRecords(username: String): List<Pair<String, Double>> {
        val records = mutableListOf<Pair<String, Double>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_EXERCISE_NAME, MAX($COL_WEIGHT) as maxWeight FROM $TABLE_NAME WHERE $COL_USERNAME = ? GROUP BY $COL_EXERCISE_NAME ORDER BY maxWeight DESC", 
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
            var maxWeight = 0.0
            var maxWeightDate = ""
            var maxVolume = 0.0
            var est1RM = 0.0
            
            val cursor = db.rawQuery(
                "SELECT $COL_WEIGHT, $COL_REPS, $COL_SETS, $COL_DATE FROM $TABLE_NAME WHERE $COL_USERNAME = ? AND $COL_EXERCISE_NAME = ?", 
                arrayOf(username, exercise)
            )
            
            if (cursor.moveToFirst()) do {
                val w = cursor.getDouble(0)
                val r = cursor.getInt(1)
                val s = cursor.getInt(2)
                val d = cursor.getString(3) ?: ""
                
                if (w > maxWeight) {
                    maxWeight = w
                    maxWeightDate = d
                }
                
                val vol = w * r * s
                if (vol > maxVolume) {
                    maxVolume = vol
                }
                
                val e1 = w * (1 + (r / 30.0))
                if (e1 > est1RM) {
                    est1RM = e1
                }
            } while (cursor.moveToNext())
            cursor.close()
            
            if (maxWeight > 0 || maxVolume > 0) {
                records.add(DetailedPR(exercise, maxWeight, maxWeightDate, est1RM, maxVolume))
            }
        }
        
        db.close()
        return records.sortedByDescending { it.maxWeight }
    }

    fun getTotalWorkoutCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getGlobalTotalWorkoutCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    // ─── STATS ──────────────────────────────────────────────────────────────

    fun getTotalWeightLifted(username: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_WEIGHT * $COL_SETS * $COL_REPS) FROM $TABLE_NAME WHERE $COL_USERNAME = ?", 
            arrayOf(username)
        )
        var total = 0.0
        if (cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getDouble(0)
        cursor.close()
        db.close()
        return total
    }

    fun getUniqueExerciseCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(DISTINCT $COL_EXERCISE_NAME) FROM $TABLE_NAME WHERE $COL_USERNAME = ?", 
            arrayOf(username)
        )
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getFavouriteMuscleGroup(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_MUSCLE_GROUP, COUNT(*) as cnt FROM $TABLE_NAME WHERE $COL_USERNAME = ? GROUP BY $COL_MUSCLE_GROUP ORDER BY cnt DESC LIMIT 1", 
            arrayOf(username)
        )
        var muscle = "N/A"
        if (cursor.moveToFirst()) muscle = cursor.getString(cursor.getColumnIndexOrThrow(COL_MUSCLE_GROUP)) ?: "N/A"
        cursor.close()
        db.close()
        return muscle
    }

    fun getFavouriteExercise(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_EXERCISE_NAME, COUNT(*) as cnt FROM $TABLE_NAME WHERE $COL_USERNAME = ? GROUP BY $COL_EXERCISE_NAME ORDER BY cnt DESC LIMIT 1", 
            arrayOf(username)
        )
        var exercise = "N/A"
        if (cursor.moveToFirst()) exercise = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXERCISE_NAME)) ?: "N/A"
        cursor.close()
        db.close()
        return exercise
    }

    fun getWorkoutStreak(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT DISTINCT $COL_DATE FROM $TABLE_NAME WHERE $COL_USERNAME = ? ORDER BY $COL_DATE DESC", 
            arrayOf(username)
        )
        if (!cursor.moveToFirst()) { cursor.close(); db.close(); return 0 }

        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        var streak = 0

        do {
            val dateStr = cursor.getString(0) ?: continue
            val workoutDate = try { sdf.parse(dateStr) } catch (e: Exception) { continue } ?: continue
            val cal2 = java.util.Calendar.getInstance().apply { time = workoutDate }

            val daysDiff = ((calendar.timeInMillis - cal2.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff == streak) {
                streak++
            } else {
                break
            }
        } while (cursor.moveToNext())

        cursor.close()
        db.close()
        return streak
    }

    // ─── EXTENDED STATS & ANALYTICS ─────────────────────────────────────────

    fun getTotalWorkoutDuration(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_DURATION) FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        var total = 0
        if (cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getInt(0)
        cursor.close()
        db.close()
        return total
    }

    fun getPersonalRecordsCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(DISTINCT $COL_EXERCISE_NAME) FROM $TABLE_NAME WHERE $COL_USERNAME = ?", 
            arrayOf(username)
        )
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getProgressPhotoCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL_USERNAME = ? AND $COL_IMAGE_PATH != ''", 
            arrayOf(username)
        )
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun getWeeklyWorkoutCount(username: String): Int {
        val db = readableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        
        // Get all dates
        val cursor = db.rawQuery("SELECT $COL_DATE FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        var count = 0
        
        val calendar = java.util.Calendar.getInstance()
        // Start of current week (Monday)
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        val startOfWeek = calendar.timeInMillis
        
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null && date.time >= startOfWeek) {
                count++
            }
        } while (cursor.moveToNext())
        
        cursor.close()
        db.close()
        return count
    }

    fun getMonthlyWorkoutCount(username: String): Int {
        val db = readableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val cursor = db.rawQuery("SELECT $COL_DATE FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        var count = 0
        
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null && date.time >= startOfMonth) {
                count++
            }
        } while (cursor.moveToNext())
        
        cursor.close()
        db.close()
        return count
    }

    fun getAverageWorkoutDuration(username: String): Int {
        val count = getTotalWorkoutCount(username)
        if (count == 0) return 0
        return getTotalWorkoutDuration(username) / count
    }

    fun getAverageSetsPerWorkout(username: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT AVG($COL_SETS) FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        var avg = 0.0
        if (cursor.moveToFirst() && !cursor.isNull(0)) avg = cursor.getDouble(0)
        cursor.close()
        db.close()
        return avg
    }

    fun getAverageReps(username: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT AVG($COL_REPS) FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        var avg = 0.0
        if (cursor.moveToFirst() && !cursor.isNull(0)) avg = cursor.getDouble(0)
        cursor.close()
        db.close()
        return avg
    }

    fun getLeastTrainedMuscleGroup(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_MUSCLE_GROUP, COUNT(*) as cnt FROM $TABLE_NAME WHERE $COL_USERNAME = ? GROUP BY $COL_MUSCLE_GROUP ORDER BY cnt ASC LIMIT 1", 
            arrayOf(username)
        )
        var muscle = "N/A"
        if (cursor.moveToFirst()) muscle = cursor.getString(cursor.getColumnIndexOrThrow(COL_MUSCLE_GROUP)) ?: "N/A"
        cursor.close()
        db.close()
        return muscle
    }

    fun getMostImprovedExercise(username: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_EXERCISE_NAME, MAX($COL_WEIGHT) as maxWeight, MIN($COL_WEIGHT) as minWeight FROM $TABLE_NAME WHERE $COL_USERNAME = ? AND $COL_WEIGHT > 0 GROUP BY $COL_EXERCISE_NAME", 
            arrayOf(username)
        )
        var maxImprovement = -1.0
        var improvedExercise = "N/A"
        
        if (cursor.moveToFirst()) do {
            val name = cursor.getString(0) ?: continue
            val maxW = cursor.getDouble(1)
            val minW = cursor.getDouble(2)
            if (minW > 0) {
                val improvement = (maxW - minW) / minW
                if (improvement > maxImprovement) {
                    maxImprovement = improvement
                    improvedExercise = name
                }
            }
        } while (cursor.moveToNext())
        
        cursor.close()
        db.close()
        return improvedExercise
    }
    
    fun getLongestStreak(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT DISTINCT $COL_DATE FROM $TABLE_NAME WHERE $COL_USERNAME = ? ORDER BY $COL_DATE DESC", 
            arrayOf(username)
        )
        if (!cursor.moveToFirst()) { cursor.close(); db.close(); return 0 }

        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        var maxStreak = 0
        var currentStreak = 1
        var prevDateMillis = 0L

        do {
            val dateStr = cursor.getString(0) ?: continue
            val workoutDate = try { sdf.parse(dateStr) } catch (e: Exception) { continue } ?: continue
            val currentMillis = workoutDate.time
            
            if (prevDateMillis == 0L) {
                prevDateMillis = currentMillis
                maxStreak = 1
                continue
            }
            
            val daysDiff = ((prevDateMillis - currentMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff == 1) {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else if (daysDiff > 1) {
                currentStreak = 1
            }
            prevDateMillis = currentMillis
            
        } while (cursor.moveToNext())

        cursor.close()
        db.close()
        return maxStreak
    }

    fun getWorkoutsByWeekday(username: String): Map<Int, Int> {
        val map = mutableMapOf<Int, Int>() 
        for (i in 1..7) map[i] = 0
        
        val db = readableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val cursor = db.rawQuery("SELECT $COL_DATE FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) {
                val cal = java.util.Calendar.getInstance().apply { time = date }
                val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
                map[dayOfWeek] = map.getOrDefault(dayOfWeek, 0) + 1
            }
        } while (cursor.moveToNext())
        
        cursor.close()
        db.close()
        return map
    }

    fun getMonthlyWorkoutCounts(username: String): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val cal = java.util.Calendar.getInstance()
        val monthFormat = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        
        for (i in 5 downTo 0) {
            val c = java.util.Calendar.getInstance()
            c.add(java.util.Calendar.MONTH, -i)
            list.add(Pair(monthFormat.format(c.time), 0))
        }
        
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_DATE FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) {
                val workoutCal = java.util.Calendar.getInstance().apply { time = date }
                
                for (i in 0 until 6) {
                    val c = java.util.Calendar.getInstance()
                    c.add(java.util.Calendar.MONTH, -(5 - i))
                    if (workoutCal.get(java.util.Calendar.YEAR) == c.get(java.util.Calendar.YEAR) &&
                        workoutCal.get(java.util.Calendar.MONTH) == c.get(java.util.Calendar.MONTH)) {
                        list[i] = list[i].copy(second = list[i].second + 1)
                    }
                }
            }
        } while (cursor.moveToNext())
        
        cursor.close()
        db.close()
        return list
    }

    fun getMuscleGroupDistribution(username: String): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_MUSCLE_GROUP, COUNT(*) as cnt FROM $TABLE_NAME WHERE $COL_USERNAME = ? GROUP BY $COL_MUSCLE_GROUP ORDER BY cnt DESC", 
            arrayOf(username)
        )
        if (cursor.moveToFirst()) do {
            val name = cursor.getString(0) ?: "Unknown"
            val count = cursor.getInt(1)
            list.add(Pair(name, count))
        } while (cursor.moveToNext())
        
        cursor.close()
        db.close()
        return list
    }

    fun getWeightProgressForExercise(username: String, exercise: String): List<Pair<String, Double>> {
        val list = mutableListOf<Pair<String, Double>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_DATE, $COL_WEIGHT FROM $TABLE_NAME WHERE $COL_USERNAME = ? AND $COL_EXERCISE_NAME = ?", 
            arrayOf(username, exercise)
        )
        
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val data = mutableListOf<Pair<java.util.Date, Double>>()
        
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val weight = cursor.getDouble(1)
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) {
                data.add(Pair(date, weight))
            }
        } while (cursor.moveToNext())
        
        cursor.close()
        db.close()
        
        data.sortBy { it.first }
        data.forEach {
            list.add(Pair(sdf.format(it.first), it.second))
        }
        
        return list
    }

    fun getDurationTrend(username: String): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_DATE, SUM($COL_DURATION) FROM $TABLE_NAME WHERE $COL_USERNAME = ? GROUP BY $COL_DATE", 
            arrayOf(username)
        )
        
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val data = mutableListOf<Pair<java.util.Date, Int>>()
        
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val duration = cursor.getInt(1)
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) {
                data.add(Pair(date, duration))
            }
        } while (cursor.moveToNext())
        
        cursor.close()
        db.close()
        
        data.sortBy { it.first }
        val last10 = data.takeLast(10)
        last10.forEach {
            list.add(Pair(sdf.format(it.first), it.second))
        }
        
        return list
    }

    fun getVolumeTrend(username: String): List<Pair<String, Double>> {
        val list = mutableListOf<Pair<String, Double>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_DATE, SUM($COL_WEIGHT * $COL_SETS * $COL_REPS) FROM $TABLE_NAME WHERE $COL_USERNAME = ? GROUP BY $COL_DATE", 
            arrayOf(username)
        )
        
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val data = mutableListOf<Pair<java.util.Date, Double>>()
        
        if (cursor.moveToFirst()) do {
            val dateStr = cursor.getString(0) ?: continue
            val volume = cursor.getDouble(1)
            val date = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (date != null) {
                data.add(Pair(date, volume))
            }
        } while (cursor.moveToNext())
        
        cursor.close()
        db.close()
        
        data.sortBy { it.first }
        val last10 = data.takeLast(10)
        last10.forEach {
            list.add(Pair(sdf.format(it.first), it.second))
        }
        
        return list
    }

    fun getWorkoutsOnDate(username: String, date: String): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, "$COL_USERNAME = ? AND $COL_DATE = ?", arrayOf(username, date), null, null, null)
        if (cursor.moveToFirst()) do { workouts.add(cursorToWorkout(cursor)) } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return workouts
    }

    fun getWorkoutDatesSet(username: String): Set<String> {
        val set = mutableSetOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COL_DATE FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        if (cursor.moveToFirst()) do {
            val d = cursor.getString(0)
            if (d != null) set.add(d)
        } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return set
    }

    fun getAllExerciseNames(username: String): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COL_EXERCISE_NAME FROM $TABLE_NAME WHERE $COL_USERNAME = ?", arrayOf(username))
        if (cursor.moveToFirst()) do {
            val name = cursor.getString(0)
            if (name != null) list.add(name)
        } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return list
    }

    fun getExerciseProgress(username: String): List<ExerciseProgress> {
        val names = getAllExerciseNames(username)
        val progressList = mutableListOf<ExerciseProgress>()
        
        for (name in names) {
            val weights = getWeightProgressForExercise(username, name)
            if (weights.isEmpty()) continue
            
            val bestWeight = weights.maxByOrNull { it.second }?.second ?: 0.0
            val currentWeight = weights.last().second
            val previousWeight = if (weights.size > 1) weights[weights.size - 2].second else currentWeight
            val improvement = if (previousWeight > 0) (currentWeight - previousWeight) / previousWeight else 0.0
            
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT MAX($COL_WEIGHT * (1 + CAST($COL_REPS AS REAL) / 30.0)) FROM $TABLE_NAME WHERE $COL_USERNAME = ? AND $COL_EXERCISE_NAME = ?", arrayOf(username, name))
            var estimated1RM = 0.0
            if (cursor.moveToFirst() && !cursor.isNull(0)) estimated1RM = cursor.getDouble(0)
            cursor.close()
            db.close()
            
            progressList.add(ExerciseProgress(
                exerciseName = name,
                currentWeight = currentWeight,
                previousWeight = previousWeight,
                bestWeight = bestWeight,
                improvementPercent = improvement * 100,
                frequency = weights.size,
                estimatedOneRepMax = estimated1RM,
                history = weights
            ))
        }
        return progressList
    }

    fun getAllExerciseProgress(username: String): List<ExerciseProgress> = getExerciseProgress(username)

    fun getConsistencyScore(username: String): Int {
        val dates = getWorkoutDatesSet(username)
        if (dates.isEmpty()) return 0
        
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val parsedDates = dates.mapNotNull { try { sdf.parse(it) } catch(e: Exception){null} }.sorted()
        if (parsedDates.isEmpty()) return 0
        
        val firstDate = parsedDates.first().time
        val lastDate = parsedDates.last().time
        
        if (firstDate == lastDate) return 100 
        
        val weeksDiff = ((lastDate - firstDate) / (1000L * 60 * 60 * 24 * 7)).toInt() + 1
        
        val weekSet = mutableSetOf<Int>()
        val cal = java.util.Calendar.getInstance()
        parsedDates.forEach {
            cal.time = it
            weekSet.add(cal.get(java.util.Calendar.WEEK_OF_YEAR) + cal.get(java.util.Calendar.YEAR) * 100)
        }
        
        val score = (weekSet.size.toDouble() / weeksDiff) * 100
        return score.toInt().coerceIn(0, 100)
    }

    fun getWeeklyCompletionPercentage(username: String, weeklyGoal: Int): Int {
        if (weeklyGoal <= 0) return 0
        val count = getWeeklyWorkoutCount(username)
        return ((count.toDouble() / weeklyGoal) * 100).toInt().coerceAtMost(100)
    }

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
        } else {
            Goal()
        }
        cursor.close()
        db.close()
        return goal
    }

    fun saveGoals(username: String, goal: Goal) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id FROM Goals WHERE username = ?", arrayOf(username))
        val exists = cursor.moveToFirst()
        cursor.close()
        
        val values = android.content.ContentValues().apply {
            put("username", username)
            put("weeklyWorkoutGoal", goal.weeklyWorkoutGoal)
            put("monthlyWorkoutGoal", goal.monthlyWorkoutGoal)
            put("targetWeight", goal.targetWeight)
            put("targetDuration", goal.targetDuration)
        }
        
        if (exists) {
            db.update("Goals", values, "username = ?", arrayOf(username))
        } else {
            db.insert("Goals", null, values)
        }
        db.close()
    }

    fun saveGoals(goal: Goal, username: String) = saveGoals(username, goal)

    // ─── ACHIEVEMENTS ───────────────────────────────────────────────────────

    fun getUnlockedAchievements(username: String): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT achievementKey FROM Achievements WHERE username = ?", arrayOf(username))
        if (cursor.moveToFirst()) do {
            val key = cursor.getString(0)
            if (key != null) list.add(key)
        } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return list
    }

    fun unlockAchievement(username: String, key: String) {
        if (isAchievementUnlocked(username, key)) return
        val db = writableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val values = android.content.ContentValues().apply {
            put("username", username)
            put("achievementKey", key)
            put("unlockedAt", sdf.format(java.util.Date()))
        }
        db.insert("Achievements", null, values)
        db.close()
    }

    fun isAchievementUnlocked(username: String, key: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM Achievements WHERE username = ? AND achievementKey = ?", arrayOf(username, key))
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun getAllAchievements(username: String): List<Achievement> {
        // Define all possible achievements
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
        if (cursor.moveToFirst()) do {
            val key = cursor.getString(0)
            val date = cursor.getString(1) ?: ""
            if (key != null) unlockedMap[key] = date
        } while (cursor.moveToNext())
        cursor.close()
        db.close()

        return allDefs.map { (key, title, desc) ->
            Achievement(
                key = key,
                name = title,
                description = desc,
                icon = "ic_achievement",
                isUnlocked = unlockedMap.containsKey(key),
                unlockedDate = unlockedMap[key] ?: ""
            )
        }.sortedByDescending { it.isUnlocked }
    }

    // ─── PROGRESS PHOTOS ────────────────────────────────────────────────────

    fun getProgressPhotos(username: String): List<Pair<String, String>> {
        val photos = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COL_DATE, COL_IMAGE_PATH), "$COL_USERNAME = ? AND $COL_IMAGE_PATH != ''", arrayOf(username), null, null, "$COL_DATE DESC")
        if (cursor.moveToFirst()) do {
            val date = cursor.getString(0) ?: ""
            val path = cursor.getString(1) ?: ""
            if (path.isNotEmpty()) photos.add(Pair(date, path))
        } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return photos
    }

    fun saveProgressPhoto(username: String, date: String, path: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EXERCISE_NAME, "Progress Photo")
            put(COL_MUSCLE_GROUP, "Other")
            put(COL_WEIGHT, 0.0)
            put(COL_SETS, 0)
            put(COL_REPS, 0)
            put(COL_DURATION, 0)
            put(COL_DATE, date)
            put(COL_NOTES, "Progress photo")
            put(COL_IMAGE_PATH, path)
            put(COL_USERNAME, username)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getProgressPhotoCount(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL_USERNAME = ? AND $COL_IMAGE_PATH != ''",
            arrayOf(username)
        )
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    // ─── PRIVATE ────────────────────────────────────────────────────────────

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
}

