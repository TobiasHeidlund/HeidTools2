package me.tubs.heidtools4.workApp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Entity
data class Checkin(
    @PrimaryKey(true) var uid: Int = 0,
    @ColumnInfo(name = "km") var km:Int = 0,
    @ColumnInfo(name = "serial") var serial: String = "",
    @ColumnInfo(name = "date") var date: String = millisToDateFormater(System.currentTimeMillis()),
    @ColumnInfo(name = "time") var time: Long = 0,
    @ColumnInfo(name = "fuelLevel") var fuelLevel: Int = 0,
    @ColumnInfo(name = "extraText") var extraText: String = "",
    @ColumnInfo(name = "parkingspot") var parkingspot: Int = 0,
    @ColumnInfo(name = "completed") var completed:Boolean = false,
    @ColumnInfo(name= "photopath") var photoPath:String = ""
){
   companion object constants{
        @Ignore  val fuelLevels:List<String> = listOf("F","7/8","3/4","5/8","H","3/8","1/4","E")
        @Ignore  val parkingspots: List<String> = (1..45).map { if(it>41){
            "G"+(it-41)
        }else it.toString()
        }
   }


    fun toStringShort(): String {
        val s = StringBuilder()
        if(serial == "") {
            s.append("ID: $uid").append("\t")
        }else s.append(serial).append("\t")
        if(km != 0) s.append(km.toString()).append(" km\t")
        s.append(fuelLevels[fuelLevel].toString())
        return s.toString()
    }
    fun toStringLong(): String {
        val s = StringBuilder()
        if(serial == "") {
            s.append("ID: $uid").append("\n")
        }else s.append(serial).append("\n")
        if(km != 0) s.append(km.toString()).append(" km\n")
        s.append(fuelLevels[fuelLevel].toString()).append("\n")
        if(time!=0L) s.append("Kom in kl ${LocalTime.ofSecondOfDay(time).format(DateTimeFormatter.ofPattern("HH:mm"))}\n")
        if(parkingspot > 0) s.append("Parkerad på: ${parkingspots[parkingspot]}\n")
        if (extraText != "") s.append(extraText)
        return s.toString()
    }


}

fun millisToDateFormater(time: Long): String {
    val inst = Instant.ofEpochMilli(time)
    val date = LocalDate.from(inst.atZone(ZoneId.systemDefault()))
    return date.format(DateTimeFormatter.ofPattern("uuuu-MM-dd"))
}
fun dateToMillisFormater(time: String): Long {
    val format = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    val date = LocalDate.parse(time,format)
    return date.atStartOfDay(ZoneId.systemDefault()).plusHours(12).toInstant().toEpochMilli()
}
