package pl.sokolowskibartlomiej.languagesar

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class LanguagesARApplication: Application() {

    init {
        instance = this@LanguagesARApplication
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: Context
    }
}