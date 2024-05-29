package me.tubs.heidtools4.workApp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import me.tubs.heidtools4.workApp.models.CheckinViewModel
import me.tubs.heidtools4.workApp.models.millisToDateFormater
import me.tubs.heidtools4.workApp.support.WorkDatabase
import me.tubs.heidtools4.workApp.ui.theme.HeidTools4Theme
import me.tubs.heidtools4.workApp.view.Browse
import me.tubs.heidtools4.workApp.view.addCheckin.AddCheckIn
import me.tubs.heidtools4.workApp.view.Settings
import me.tubs.heidtools4.workApp.view.View

@OptIn(ExperimentalMaterial3Api::class)
class WorkActivity : ComponentActivity() {
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(
            applicationContext,
            WorkDatabase::class.java, "Cars"
        ).fallbackToDestructiveMigration().build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    Toast(this.baseContext).apply {
                        setText("Camera Access is required for the app to function, Please Enter Settings to fix")
                        show()
                    }
                }
            }.launch(Manifest.permission.CAMERA)
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContent {
            HeidTools4Theme {
                // A surface container using the 'background' color from the theme
                val controller = rememberNavController()
                val navStates = navStates(controller)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = controller, startDestination = navStates.BROWSE) {
                        composable(navStates.BROWSE) {
                            Browse().View(db.checkinDao(), navStates)
                        }
                        composable("${navStates.BROWSE}/{date}") {
                            Log.d("WorkActivity", "onCreate: DATE VARIANT RUN")
                            
                            var date = it.arguments?.getString("date")
                            if (date == null) {
                                date = millisToDateFormater(System.currentTimeMillis())
                            }
                            Browse().View(db.checkinDao(), navStates,date)
                        }

                        composable("${navStates.ADDCHECKIN}/{id}") {
                            val id = it.arguments?.getString("id")!!.toLong()
                            AddCheckIn(navStates, this@WorkActivity).View(CheckinViewModel.Builder(id,
                                checkinDao = db.checkinDao(),this@WorkActivity).build()
                            )
                        }
                        composable("${navStates.VIEW}/{id}/{day}"){
                            val id:Int = it.arguments?.getString("id")!!.toInt()
                            val day = it.arguments?.getString("day")!!

                            View().views(checkinDao = db.checkinDao(), initalObj = id, day = day, navStates = navStates, activity = this@WorkActivity)
                        }
                        composable(navStates.SETTINGS){
                            Settings().view(context = baseContext)
                        }
                    }

                }
            }
        }
    }

}