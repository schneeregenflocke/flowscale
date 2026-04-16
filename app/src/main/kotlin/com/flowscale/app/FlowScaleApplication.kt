package com.flowscale.app

import android.app.Application
import androidx.room.Room
import com.flowscale.app.data.AppDatabase

class FlowscaleApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            AppDatabase.NAME,
        ).build()
    }
}
