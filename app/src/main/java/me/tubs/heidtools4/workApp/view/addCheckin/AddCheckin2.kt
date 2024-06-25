@file:OptIn(ExperimentalFoundationApi::class)

package me.tubs.heidtools4.workApp.view.addCheckin

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.view.PreviewView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.heid.heidtools.work.support.CamX
import me.tubs.heidtools4.workApp.support.WorkDatabase
import me.tubs.heidtools4.workApp.ui.theme.HeidTools4Theme
import me.tubs.heidtools4.workApp.view.addCheckin.Components.MyBottomAppBar

@ExperimentalMaterial3Api
class AddCheckin2: ComponentActivity() {

    private var uid : Int = -1
    private val camX = CamX()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            WorkDatabase::class.java, "Cars"
        ).fallbackToDestructiveMigration().build()

        CameraPermission()

        if (savedInstanceState?.containsKey("id") == true){
            uid = savedInstanceState.getInt("id")
        }
        if(this.intent.extras?.containsKey("id") == true && uid == -1){
            uid = intent.extras!!.getInt("id")
        }

        val viewmodel:AddChekinViewModel by viewModels()
        val dataGrabber = db.checkinDao().get(id = uid).distinctUntilChanged()

        dataGrabber.observe(this){dbCheckin->
            if (dbCheckin != null) {
                viewmodel.initData(dbCheckin)
                dataGrabber.removeObservers(this)
            }
        }

        lifecycleScope.launch (Dispatchers.IO){
                viewmodel.uiState.collect {
                    if (it.uid != 0) {
                        db.checkinDao().update(it)
                    }
                }
        }


        setContent{
            HeidTools4Theme {
                view(viewmodel)
            }
        }






    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("id",uid)
    }



    private fun CameraPermission() {
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
    }


    @Preview
    @Composable
    fun view(viewModel: AddChekinViewModel = AddChekinViewModel()){
        var dialog = remember { mutableIntStateOf(0) }
        Scaffold(bottomBar = {BottomBar(viewModel,dialog)},
            floatingActionButton = {FloatingButton()},) {
            Box(Modifier.fillMaxSize().padding(bottom = it.calculateBottomPadding()+80.dp, top = it.calculateTopPadding()), contentAlignment = Alignment.BottomCenter){
                CamPreview(camX = camX,it)
            }


        }

        val model by viewModel.uiState.collectAsState()

        when{
            dialog.intValue == 1 ->
                MyDialog("Serial Number",model.serial,dialog, onCompleted={
                    viewModel.setSerial(it)
                })
            dialog.intValue == 2 ->
                MyDialog("Serial Number",model.serial,dialog, onCompleted={
                    viewModel.setOdometer(it.toInt())
                }, keyboardType = KeyboardType.Number)
        }


    }

    private @Composable
    fun MyDialog(
        titleValue: String,
        currentValue: String,
        dialog: MutableIntState,
        onCompleted: (String) -> Unit,
        keyboardType: KeyboardType = KeyboardType.Text) {
        val focusRequester = remember { FocusRequester() }


        BackHandler {
            dialog.intValue = 0
        }

        var textFieldValueState by remember {
            mutableStateOf(
                TextFieldValue(currentValue
                    , TextRange( currentValue.length)

                )
            )
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0, 0, 0, 150))){
            AlertDialog(
                onDismissRequest = { dialog.intValue = 0 },
                confirmButton = {
                    Button(onClick = {
                        onCompleted(textFieldValueState.text)
                        dialog.intValue = 0
                    }) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { dialog.intValue = 0 }) {
                        Text(text = "Cancel")
                    }
                },
                title= {
                Text("Set $titleValue")
                                                                                              
                },text = {
                    OutlinedTextField(value = textFieldValueState, onValueChange ={textFieldValueState = it},
                        modifier = Modifier
                            .align(Alignment.Center)
                            .focusRequester(focusRequester),
                        singleLine = true, maxLines = 1, keyboardActions = KeyboardActions(onDone = {
                            onCompleted(textFieldValueState.text)
                            dialog.intValue = 0
                        }), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = keyboardType)
                    )
                }

            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }


    @Composable
    private fun CamPreview(camX: CamX, paddingValues: PaddingValues) {
        val lifecycle = LocalLifecycleOwner.current
        lifecycle.lifecycle.addObserver(camX)
        val prev = LocalInspectionMode.current
        AndroidView(
            //338*resources.displayMetrics.density
            factory = {
                context: Context ->
                val view = PreviewView(context)
                view.setOnTouchListener{
                    v,event ->
                    when(event.action){
                        MotionEvent.ACTION_DOWN -> {
                            // Handle touch down (finger touches the screen)
                            WhenClicked(v, event,camX)

                            v.performClick()
                            true
                        }
                        else -> false
                    }
                }
                Log.d("CamX", "CamPreview: ")
                if(!prev) {
                    camX.onViewCreated(view, this, lifecycle)
                }
                view
            }

        )
    }

    private fun WhenClicked(v: View?, event: MotionEvent,camX: CamX) {
        val previewView = v as PreviewView
        Log.d("AddCheckin2", "WhenClicked: " +
                "${event.x*event.xPrecision}")
        Log.d("AddCheckin2", "WhenClicked: ${event.y}")
        Log.d("AddCheckin2", "WhenClicked: ${v.height}")

        //camX.getClosestText(//meteringPoint)

    }

    private @Composable
    fun FloatingButton() {
        FloatingActionButton(onClick = {
        }) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Save description"
            )
        }
    }

    private @Composable
    fun BottomBar(viewModel: AddChekinViewModel, dialog: MutableIntState) {
        MyBottomAppBar(modifier = Modifier.height(150.dp), actions = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                FuelGage(viewModel)
                HorizontalDivider()
                Row {
                    BottomBarObject("Serial Number",.33f,dialog, 1) {}
                    VerticalDivider()
                    BottomBarObject("Odometer", .5f, dialog, 2) {}
                    VerticalDivider()
                    BottomBarObject("Extra Photos", 1f, dialog, 0) {}
                }
            }
        })
    }

    @Composable
    private fun FuelGage(viewModel: AddChekinViewModel) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.height(50.dp)) {
            Text(text = "Fuel Level", fontSize = TextUnit(18f, TextUnitType.Sp))
            val slider by remember { viewModel.fuelLevel }
            Slider(
                value = slider, onValueChange = {
                    Log.d("AddCheckin2", "FuelGage: $it")
                    viewModel.setFuelLevel(it.toInt())
                }, Modifier.fillMaxWidth(.8f),
                steps = 7, valueRange = 0f..8f
            )
        }
    }

    @Composable
    private fun BottomBarObject(
        s: String,
        fraction: Float,
        dialog: MutableIntState,
        setDialogTo: Int,
        function: () -> Unit
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(fraction)
        ) {
            Text(text = s, fontSize = TextUnit(18f, TextUnitType.Sp))
            Icon(
                Icons.Filled.Clear,
                contentDescription = "$s Localized description",
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(onLongClick = {
                        dialog.intValue = setDialogTo
                    }) {}
            )
        }
    }


}