package com.luchodevs.tx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.luchodevs.tx.worker.AutoStopWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Encolar el WorkManager solo una vez
        val workRequest = PeriodicWorkRequest.Builder(
            AutoStopWorker::class.java,
            1, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "autoStopWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
