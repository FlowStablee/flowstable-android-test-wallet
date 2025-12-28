package com.antigravity.cryptowallet.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.antigravity.cryptowallet.data.db.TokenDao
import com.antigravity.cryptowallet.data.db.TokenEntity

@Database(entities = [TokenEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao
}
