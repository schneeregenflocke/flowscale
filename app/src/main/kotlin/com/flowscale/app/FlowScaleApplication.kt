package com.flowscale.app

import android.app.Application
import androidx.room.Room
import com.flowscale.app.data.AppDatabase

class FlowScaleApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "flowscale.db",
        ).build()
    }
}
