package me.tubs.heidtools4.workApp.support

import androidx.room.Database
import androidx.room.RoomDatabase
import me.tubs.heidtools4.workApp.models.Checkin

@Database(entities = [Checkin::class], version = 10)
abstract class WorkDatabase : RoomDatabase() {
    abstract fun checkinDao(): CheckinDao
}