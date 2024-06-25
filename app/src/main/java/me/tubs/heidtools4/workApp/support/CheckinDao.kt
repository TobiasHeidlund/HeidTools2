package me.tubs.heidtools4.workApp.support

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import me.tubs.heidtools4.workApp.models.Checkin
import me.tubs.heidtools4.workApp.models.millisToDateFormater

@Dao
interface CheckinDao {
    @Query("SELECT * FROM Checkin")
    fun getAll(): LiveData<List<Checkin>>

    @Insert
    fun insertAll(vararg checkIn: Checkin):Array<Long>

    @Delete
    fun delete(checkIn: Checkin)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(checkIn: Checkin):Long


    @Query("SELECT * FROM Checkin WHERE uid = :id LIMIT 1")
    fun get(id: Int): LiveData<Checkin>


    @Query("SELECT * FROM Checkin WHERE date=:date")
    fun getDay(date: String = millisToDateFormater(System.currentTimeMillis())): LiveData<List<Checkin>>

    @Query("SELECT DISTINCT date FROM Checkin")
    fun getPosibleDays(): LiveData<List<String>>

}