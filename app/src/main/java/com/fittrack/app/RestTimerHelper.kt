package com.fittrack.app

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper

/**
 * Manages the in-session rest timer.
 * Uses a simple CountDownTimer — no background service needed.
 */
class RestTimerHelper(private val context: Context) {

    private var countDownTimer: CountDownTimer? = null
    private var listener: RestTimerListener? = null
    private var isRunning = false
    private var remainingMs = 0L

    interface RestTimerListener {
        fun onTick(remainingSeconds: Int)
        fun onFinished()
    }

    fun setListener(l: RestTimerListener) { listener = l }

    fun start(seconds: Int) {
        cancel()
        remainingMs = seconds * 1000L
        isRunning = true
        countDownTimer = object : CountDownTimer(remainingMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMs = millisUntilFinished
                listener?.onTick((millisUntilFinished / 1000).toInt())
            }
            override fun onFinish() {
                isRunning = false
                remainingMs = 0
                listener?.onFinished()
            }
        }.start()
    }

    fun cancel() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
        remainingMs = 0
    }

    fun pause() {
        countDownTimer?.cancel()
        isRunning = false
    }

    fun resume() {
        if (remainingMs > 0) {
            isRunning = true
            countDownTimer = object : CountDownTimer(remainingMs, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    remainingMs = millisUntilFinished
                    listener?.onTick((millisUntilFinished / 1000).toInt())
                }
                override fun onFinish() {
                    isRunning = false; remainingMs = 0; listener?.onFinished()
                }
            }.start()
        }
    }

    fun isRunning() = isRunning
    fun getRemainingSeconds() = (remainingMs / 1000).toInt()

    companion object {
        const val REST_30S  = 30
        const val REST_60S  = 60
        const val REST_90S  = 90
        const val REST_120S = 120

        val PRESETS = listOf(
            Pair(REST_30S,  "30s"),
            Pair(REST_60S,  "1 min"),
            Pair(REST_90S,  "90s"),
            Pair(REST_120S, "2 min")
        )
    }
}
