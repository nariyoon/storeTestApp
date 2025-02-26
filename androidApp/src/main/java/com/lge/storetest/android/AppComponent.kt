package com.lge.storetest.android

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.lge.storetest.data.db.DriverFactory
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class AppComponent(@get:Provides val context: Context) {

    @Provides
    fun provideDriver(): SqlDriver = DriverFactory(context).createDriver()



}