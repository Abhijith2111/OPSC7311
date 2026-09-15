package com.example.schwiftysavings

import android.app.Application
import android.util.Log
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.data.SessionStore
import com.example.schwiftysavings.data.local.SchwiftyDatabase

class SchwiftyApplication : Application() {
    lateinit var repository: SchwiftyRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = SchwiftyDatabase.getInstance(this)
        val session = SessionStore(this)
        repository = SchwiftyRepository(db, session, this)
        Log.d("SchwiftyApp", "Application started — repository ready")
    }
}