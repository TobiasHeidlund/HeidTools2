package me.tubs.heidtools4.workApp.models

import android.database.Observable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.tubs.heidtools4.workApp.support.CheckinDao
import java.time.LocalTime

class CheckinViewModel private constructor(): ViewModel(){



    data class Builder(var id:Long,val checkinDao: CheckinDao,val host:LifecycleOwner){
        var checkin: LiveData<Checkin>? = if(id != 0L)  checkinDao.get(id.toInt()) else null;
        fun build():CheckinViewModel{
            return CheckinViewModel(checkin,checkinDao, host)
        }
    }

    private var checkinDao: CheckinDao? = null
    private var checkin = Checkin()
    private val km: MutableLiveData<Int> = MutableLiveData(0)
    private val serial: MutableLiveData<String> = MutableLiveData("")
    private val date: MutableLiveData<String> = MutableLiveData("checkin.value!!.date")
    private val time: MutableLiveData<Long> = MutableLiveData(0L)
    private val extraText: MutableLiveData<String> = MutableLiveData("checkin.value!!.extraText")
    private val photoPath: MutableLiveData<String> = MutableLiveData("")
    private val fuelLevel: MutableLiveData<Int> = MutableLiveData(0)
    private val parkingSpot: MutableLiveData<Int> = MutableLiveData(0)


    val _km: LiveData<Int> = km
    val _serial: LiveData<String> = serial
    val _date: LiveData<String> = date
    val _time: LiveData<Long> = time
    val _extraText: LiveData<String> = extraText
    val _photoPath: LiveData<String> = photoPath
    val _fuelLevel: LiveData<Int> = fuelLevel
    val _parkingSpot: LiveData<Int> = parkingSpot


    constructor(liveCheckin: LiveData<Checkin>?,
                checkinDao: CheckinDao?,
                host:LifecycleOwner) : this() {
        this.checkinDao = checkinDao
        if(liveCheckin != null){
        liveCheckin.observe(host) {
            if (it != null) {
                checkin = it
                km.value = it.km
                serial.value = it.serial
                date.value = it.date
                time.value = it.time
                extraText.value = it.extraText
                fuelLevel.value = it.fuelLevel
                parkingSpot.value = it.parkingspot
                photoPath.value = it.photoPath
            }
            liveCheckin.removeObservers(host)
        }
        }else{
            this.checkin = Checkin()
            km.value = checkin.km
            serial.value = checkin.serial
            date.value = checkin.date
            time.value = checkin.time
            extraText.value = checkin.extraText
            fuelLevel.value = checkin.fuelLevel
            parkingSpot.value = checkin.parkingspot
            photoPath.value = checkin.photoPath
        }
    }

    constructor(checkin: Checkin):this(){
        this.checkin = Checkin()
        km.value = checkin.km
        serial.value = checkin.serial
        date.value = checkin.date
        time.value = checkin.time
        extraText.value = checkin.extraText
        fuelLevel.value = checkin.fuelLevel
        parkingSpot.value = checkin.parkingspot
        photoPath.value = checkin.photoPath
    }



    //SKA INTE SAVA FÖR ATT UNVIKA ATT AUTOMATISKT SKAPA ETT NY DB ENRTY
    fun setPhotoPath(value: String){
        photoPath.value = value
        checkin.photoPath = value
        save()
    }

    fun setKm(value: Int) {
        km.value = value
        checkin.km = value
        save()
    }

    fun setSerial(value: String) {
        serial.value = value
        checkin.serial = value
        save()

    }

    fun setDate(value: String) {
        date.value = value
        checkin.date = value
        save()
    }

    fun setHour(value: Int) {
        val newTime = LocalTime.of(value,LocalTime.ofSecondOfDay(checkin.time).minute).toSecondOfDay().toLong()
        time.value = newTime
        checkin.time = newTime
        save()
    }

    fun setMinute(value: Int) {
        val newTime = LocalTime.of(LocalTime.ofSecondOfDay(checkin.time).hour,value).toSecondOfDay().toLong()
        time.value = newTime
        checkin.time = newTime
        save()
    }
    fun setTime(value: Long) {
        time.value = value
        checkin.time = value
        save()
    }
    fun setExtraText(value: String){
        extraText.value = value
        checkin.extraText = value
        save()
    }
    fun setFuelLevel(value: Int) {
        fuelLevel.value = value
        checkin.fuelLevel = value
        save()
    }

    fun setParkingSpot(value: Int) {
        parkingSpot.value = value
        checkin.parkingspot = value
        save()
    }
    private fun save(){
        runBlocking {
            launch(Dispatchers.IO) {
                    if(Checkin().hashCode() != checkin.hashCode()){
                        checkin.uid = checkinDao?.update(checkin)?.toInt() ?: 0
                    }
                }
            }
        }


}




