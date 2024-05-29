package me.tubs.heidtools4.workApp

import android.util.Log
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

class navStates(val navController: NavHostController){
    val VIEW: String = "view"
    val BROWSE = "browse"
     val ADDCHECKIN = "addCheckin"
     val DATESELECT = "dateSelect"
     val SETTINGS = "settings"


    fun navView(checkin: Long,day:String){
        navController.navigate("$VIEW/$checkin/$day")
    }
    fun navBrowse(){
        navController.navigate(BROWSE)
    }
    fun navAddCheckin(checkin: Long) {
        navController.navigate("$ADDCHECKIN/$checkin" )
    }
    fun backTo(to: String) {
        if(!navController.popBackStack(to,true)) navController.navigate(to)

    }

    fun navSettings() {
        navController.navigate(SETTINGS)
    }


}