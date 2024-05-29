@file:OptIn(ExperimentalMaterial3Api::class)

package me.tubs.heidtools4.workApp.view

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.tubs.heidtools4.workApp.models.Checkin
import me.tubs.heidtools4.workApp.models.dateToMillisFormater
import me.tubs.heidtools4.workApp.models.millisToDateFormater
import me.tubs.heidtools4.workApp.navStates
import me.tubs.heidtools4.workApp.support.CheckinDao
import me.tubs.heidtools4.workApp.ui.theme.HeidTools4Theme
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.TemporalField
import java.util.ArrayList
import kotlin.random.Random

@ExperimentalMaterial3Api
class Browse {

    class mViewModel : ViewModel() {
        var d: MutableLiveData<List<Checkin>> = MutableLiveData()
        var dbLiveCon: LiveData<List<Checkin>>? = null


        fun updateDay(checkinDao: CheckinDao,lifecycleOwner: LifecycleOwner,day:String = millisToDateFormater(System.currentTimeMillis())){
            dbLiveCon?.removeObservers(lifecycleOwner)
            dbLiveCon = checkinDao.getDay(day)
            dbLiveCon!!.observe(lifecycleOwner){
                d.value = it
            }
        }
    }

    @Composable
    fun View(checkinDao: CheckinDao, navStates: navStates,initialDate: String =  millisToDateFormater(System.currentTimeMillis())) {
        val model : mViewModel = viewModel()
        val lifecycleOwner = LocalLifecycleOwner.current
        val loadEntrys by model.d.observeAsState(listOf())

        LaunchedEffect(true){
            model.updateDay(checkinDao, lifecycleOwner,initialDate)
        }


        browse(loadEntrys, navStates,checkinDao ,dateUpdate = model,initialDate)
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun browse(
        loadEntrys: List<Checkin>,
        navStates: navStates,
        checkinDao: CheckinDao? = null,
        dateUpdate: mViewModel? = null,
        initialDate: String =  millisToDateFormater(System.currentTimeMillis())
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val openDialog = remember {
            mutableStateOf(false)
        }
        var dates = checkinDao?.getPosibleDays()?.observeAsState()


        val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
              val date =  millisToDateFormater(utcTimeMillis)
              if (dates?.value?.contains(date) == true){
                  return true
              }else if (0<System.currentTimeMillis()-utcTimeMillis&&System.currentTimeMillis()-utcTimeMillis<=(1000*60*60*24L) ){
                  return true
              }else return false
            }
        }, initialSelectedDateMillis = dateToMillisFormater(initialDate))

        var date = datePickerState.selectedDateMillis?.let {
            millisToDateFormater(it)
        }.toString()



        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                DateBar(date, openDialog)
                HorizontalDivider()
                List(loadEntrys, navStates)
            }
            val interactionSource = remember { MutableInteractionSource() }

            val viewConfiguration = LocalViewConfiguration.current


            LaunchedEffect(interactionSource) {
                var isLongClick = false

                interactionSource.interactions.collectLatest { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> {
                            isLongClick = false
                            delay(viewConfiguration.longPressTimeoutMillis)
                            isLongClick = true
                            navStates.navSettings()
                        }
                        is PressInteraction.Release -> {
                            if (isLongClick.not()){
                                navStates.navAddCheckin(0)
                            }

                        }
                        is PressInteraction.Cancel -> {
                            isLongClick = false
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = {},
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(-25.dp, -25.dp),
                interactionSource= interactionSource) {
                Icon(Icons.Filled.Add, "New Car Entry")
            }
            when {
                openDialog.value -> {
                    BackHandler {
                        openDialog.value = false
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = .8f))
                    ) {
                        DatePickerView(datePickerState = datePickerState,
                            dismiss = {
                                openDialog.value = false
                            },
                            confirm = {
                               // onDateUpdated(it)
                                if (dateUpdate != null) {
                                    if (checkinDao != null) {
                                        navStates.navController.popBackStack()
                                        navStates.navController.navigate("${navStates.BROWSE}/$it")
                                        Log.d("Browse", "browse: test")
                                    }
                                }
                            });
                    }


                }


            }

        }


    }

    @Composable
    private fun DateBar(
        date: String,
        openDialog: MutableState<Boolean>
    ) {
        Text(text = date, Modifier.clickable {
            Log.w("", "brow: i was here")
            openDialog.value = true
        }, fontSize = TextUnit(64f, TextUnitType.Sp))
    }

    @Composable
    private fun List(loadEntrys: List<Checkin>, navStates: navStates) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            loadEntrys.forEach {
                item {
                    checkInListItem(it, navStates = navStates)
                    HorizontalDivider(Modifier.padding(2.dp))
                }
            }
            //LoadEntrys(date,checkinDao)
        }
    }

    @Composable
    private fun checkInListItem(it: Checkin, navStates: navStates) {
        val fontSize = TextUnit(21f, TextUnitType.Sp)
        Row(
            Modifier
                .padding(Dp(0f), Dp(1f))
                .fillMaxWidth(.85f)
                .height(50.dp)
                .clickable {
                    //navStates.navAddCheckin(it.uid.toLong())
                    navStates.navView(it.uid.toLong(), it.date)
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if(it.serial.isEmpty()) it.uid.toString() else it.serial, fontSize = fontSize, modifier = Modifier.width(95.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "${it.km} km", fontSize = fontSize, modifier = Modifier.width(100.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = Checkin.fuelLevels[it.fuelLevel],
                fontSize = fontSize, modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text =  if (it.parkingspot != -1){Checkin.parkingspots[it.parkingspot]}else{""},
                fontSize = fontSize, modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
        }
    }


    @Composable
    fun DatePickerView(
        datePickerState: DatePickerState,
        dismiss: () -> Unit,
        confirm: (String?) -> Unit
    ) {
        val selectedDate = datePickerState.selectedDateMillis?.let {
            millisToDateFormater(it)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DatePickerDialog(
                onDismissRequest = { dismiss() },
                confirmButton = {
                    Button(onClick = {
                        confirm(selectedDate)
                        dismiss()
                    }

                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        dismiss()
                    }) {
                        Text(text = "Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
            Spacer(
                modifier = Modifier.height(
                    32.dp
                )
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    HeidTools4Theme {
        val list = ArrayList<Checkin>()
        for (i in (0..Random.nextInt(5, 30))) {
            list.add(
                Checkin(
                    serial = "SE0" + Random.nextInt(100, 2000),
                    km = Random.nextInt(500, 50000),
                    fuelLevel = Random.nextInt(0, 7),
                    parkingspot = Random.nextInt(0, 44)
                )
            )
        }
        Browse().browse(list.toList(), navStates(rememberNavController()))
    }
}