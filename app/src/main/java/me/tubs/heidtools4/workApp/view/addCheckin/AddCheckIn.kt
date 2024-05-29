@file:OptIn(ExperimentalFoundationApi::class)

package me.tubs.heidtools4.workApp.view.addCheckin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.camera.view.PreviewView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.viewinterop.AndroidView
import me.heid.heidtools.work.support.CamX
import me.tubs.heidtools4.R
import me.tubs.heidtools4.workApp.models.Checkin
import me.tubs.heidtools4.workApp.models.CheckinViewModel
import me.tubs.heidtools4.workApp.navStates
import me.tubs.heidtools4.workApp.support.WorkFileManager
import me.tubs.heidtools4.workApp.view.Ocr

class AddCheckIn(
    private val navStates: navStates,
    private val host: ComponentActivity?, ){
   //private var camX: CamX = CamX()

    @Composable
    fun View(checkIn: CheckinViewModel) {
        val camX = remember { CamX() }
        val path :String by checkIn._photoPath.observeAsState("")
        val serial :String by checkIn._serial.observeAsState("")
        val odometer :Int by checkIn._km.observeAsState(0)
        val extraText :String by checkIn._extraText.observeAsState("")
        val extraPhotos = remember { mutableIntStateOf(0) }
        val baseImage =
            if(host == null){
                ImageBitmap(50, 50)
            }else drawableToBitmap(host.resources.getDrawable(R.drawable.baseline_camera_24,host.theme)).asImageBitmap()

        Column(
            modifier = Modifier.padding(Dp(0f), Dp(0f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CamPreview(camX)
            TopBar(checkIn)
            HorizontalDivider()
            KeyPhoto(serial, checkIn, baseImage, host, path, camX)
            HorizontalDivider()
            DashPhoto(odometer, checkIn, baseImage, host, path, camX)
            HorizontalDivider()
            ExtraPhotos(extraPhotos, checkIn, baseImage, host, path, camX)
            HorizontalDivider()
            BottomBar(extraText, checkIn)
        }



    }

    @Composable
    private fun CamPreview(camX: CamX) {
        val lifecycle = LocalLifecycleOwner.current
        lifecycle.lifecycle.addObserver(camX)


        AndroidView(
            factory = { context: Context ->
                val view = PreviewView(context)
                if (host != null) {
                    Log.d("CamX", "CamPreview: ")
                    camX.onViewCreated(view, host, lifecycle)
                }
                view
            }, Modifier
                .height(Dp(200f))
                .aspectRatio(1f)
                .padding(bottom = Dp(30f))
        )
    }

    @Composable
    private fun BottomBar(
        extraText: String,
        checkIn: CheckinViewModel
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(.9f)
                .padding(Dp(5f)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(value = extraText, onValueChange = {
                checkIn.setExtraText(it)
            }, label = { Text("Extra Info") })

            FloatingActionButton(onClick = {
                navStates.backTo("browse")
            }) {
                Icon(Icons.Filled.Add, "Floating Add")
            }

        }
    }

    @Composable
    private fun TopBar(checkIn: CheckinViewModel
    ) {
        val parkingState by checkIn._parkingSpot.observeAsState()
        val fuelLevel = rememberPagerState(initialPage = checkIn._fuelLevel.value!!) { 8 }
        SavePagerState(fuelLevel){ value:Int-> checkIn.setFuelLevel(value) }
        LaunchedEffect(checkIn._fuelLevel.value!!) {
            if (fuelLevel.currentPage != checkIn._fuelLevel.value!!) {
                fuelLevel.scrollToPage(checkIn._fuelLevel.value!!)
            }

        }
        val hourState = rememberPagerState { 24 }
        SavePagerState(hourState){ value:Int-> checkIn.setHour(value) }
        val minuteState = rememberPagerState { 60 }
        SavePagerState(minuteState){ value:Int-> checkIn.setMinute(value) }
        val parkingSpot = rememberPagerState(initialPage = checkIn._parkingSpot.value!!) { 44 }
        SavePagerState(parkingSpot){ value:Int-> checkIn.setParkingSpot(value) }
        LaunchedEffect(parkingState) {
            if (parkingSpot.currentPage != parkingState!!) {
                parkingSpot.scrollToPage(parkingState!!)
            }
        }


        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .padding(Dp(25f), Dp(20f))
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val expanded = remember { mutableStateOf(false) }
            /* Column(horizontalAlignment = Alignment.CenterHorizontally) {

                 Row {
                     VerticalPager(modifier = Modifier.height(Dp(40f)), state = hourState)
                     {
                         Text(text = it.toString().let { it1 ->
                             if (it1.length == 1) {
                                 "0$it1"
                             } else {
                                 it1
                             }
                         }, fontSize = TextUnit(40f, TextUnitType.Sp))

                     }
                     Text(":", fontSize = TextUnit(40f, TextUnitType.Sp))
                     VerticalPager(modifier = Modifier.height(Dp(40f)), state = minuteState)
                     { it1 ->
                         Text(
                             it1.toString().let {
                                 if (it.length == 1) {
                                     "0$it"
                                 } else {
                                     it
                                 }
                             },
                             fontSize = TextUnit(40f, TextUnitType.Sp)
                         )

                     }
                 }


             }
             VerticalPager(modifier = Modifier.height(Dp(40f)), state = parkingSpot, beyondBoundsPageCount = 10)
             {
                 Text(
                     Checkin.parkingspots[it],
                     fontSize = TextUnit(40f, TextUnitType.Sp)
                 )

             }*/
            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded.value = !expanded.value
                    }, contentAlignment = Alignment.Center
            ) {

                VerticalPager(
                    state = fuelLevel,
                    modifier = Modifier.height(Dp(40f)),
                    beyondBoundsPageCount = 10
                ) {
                    Text(
                        text =
                        Checkin.fuelLevels[it], fontSize = TextUnit(40f, TextUnitType.Sp)
                    )
                }
            }


        }
    }

    @Composable
    private fun ExtraPhotos(
        extraPhotos: MutableIntState,
        checkIn: CheckinViewModel,
        baseImage: ImageBitmap,
        host: ComponentActivity?,
        path: String,
        camX: CamX
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(.9f)
                .padding(Dp(5f)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Extra Photos", fontSize = TextUnit(30f, TextUnitType.Sp))
                Text(
                    text = extraPhotos.intValue.toString(),
                    fontSize = TextUnit(20f, TextUnitType.Sp)
                )
            }
            val model = remember { mutableStateOf(baseImage) }
            Image(bitmap = model.value,
                contentDescription = "Icon",
                alignment = Alignment.CenterEnd,
                modifier = Modifier
                    .width(Dp(100f))
                    .height(Dp(100f))
                    .rotate(90f)
                    .clickable {
                        Log.e("view: ", model.toString())
                        if (host != null) {
                            camX.takePhoto(
                                host.baseContext,
                                WorkFileManager(
                                    host.baseContext,
                                    requirePath(checkIn, path, host)
                                ).nextExtra
                            ) { _, path ->
                                val b = BitmapFactory.decodeFile(path)
                                model.value = b.asImageBitmap()
                                extraPhotos.intValue = extraPhotos.intValue
                                    .inc()

                            }
                        }
                    }
            )
        }
    }

    @Composable
    private fun DashPhoto(
        odometer: Int,
        checkIn: CheckinViewModel,
        baseImage: ImageBitmap,
        host: ComponentActivity?,
        path: String,
        camX: CamX
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(.9f)
                .padding(Dp(5f)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Dash Photo", fontSize = TextUnit(30f, TextUnitType.Sp))
                TextField(
                    value = if (odometer.toString() == "0") "" else odometer.toString(),
                    onValueChange = { s ->
                        try {
                            checkIn.setKm(s.toInt())
                        } catch (error: NumberFormatException) {
                            checkIn.setKm(0)
                        }


                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(Dp(150f)),
                    singleLine = true
                )

            }
            val model = remember { mutableStateOf(baseImage) }
            if (path.isNotEmpty()) {
                val file = WorkFileManager(host, path)
                if (file.dashFile.exists())
                    LaunchedEffect(file) {
                        model.value = BitmapFactory.decodeFile(file.dashPath).asImageBitmap()
                    }
            }
            Image(bitmap = model.value,
                contentDescription = "Icon",
                alignment = Alignment.CenterEnd,
                modifier = Modifier
                    .width(Dp(100f))
                    .height(Dp(100f))
                    .rotate(90f)
                    .clickable {
                        Log.e("view: ", model.toString())
                        if (host != null) {
                            camX.takePhoto(
                                host.baseContext,
                                WorkFileManager(
                                    host.baseContext,
                                    requirePath(checkIn, path, host)
                                ).dashPath
                            ) { _, path ->
                                val b = BitmapFactory.decodeFile(path)
                                val ocr = Ocr()
                                ocr.CheckImage(b,
                                    {
                                        val text2 = ocr.checkDashText(it)
                                        try {
                                            checkIn.setKm(text2.toInt())
                                        } catch (e: NumberFormatException) {
                                            e.printStackTrace()
                                        }

                                    })

                                model.value = b.asImageBitmap()
                            }
                        }
                    })
        }
    }

    @Composable
    private fun KeyPhoto(
        serial: String,
        checkin: CheckinViewModel,
        baseImage: ImageBitmap,
        host: ComponentActivity?,
        path: String,
        camX: CamX
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(.9f)
                .padding(Dp(5f)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Key Photo", fontSize = TextUnit(30f, TextUnitType.Sp))
                TextField(
                    value = serial, onValueChange = { s ->
                        checkin.setSerial(s)
                    }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(Dp(150f)),
                    singleLine = true
                )
            }


            val model = remember { mutableStateOf(baseImage) }
            if (path.isNotEmpty()) {
                val file = WorkFileManager(host, path)
                if (file.keyFile.exists())
                    LaunchedEffect(file) {
                        model.value = BitmapFactory.decodeFile(file.keyPath).asImageBitmap()
                    }
            }


            Image(bitmap = model.value,
                contentDescription = "Icon",
                alignment = Alignment.CenterEnd,
                modifier = Modifier
                    .width(Dp(100f))
                    .height(Dp(100f))
                    .rotate(90f)
                    .clickable {
                        Log.e("view: ", model.toString())
                        if (host != null) {
                            camX.takePhoto(
                                host.baseContext,
                                WorkFileManager(
                                    host.baseContext,
                                    requirePath(checkin, path, host)
                                ).keyPath
                            ) { _, path ->
                                val b = BitmapFactory.decodeFile(path)
                                val ocr = Ocr()
                                ocr.CheckImage(b,
                                    {
                                        val text2 = ocr.checkKeyText(it)
                                        checkin.setSerial(text2)
                                    })
                                model.value = b.asImageBitmap()
                            }
                        }
                    })

        }
    }

    private fun requirePath(checkIn: CheckinViewModel, path: String?, context: Context): String {
        return if (path.isNullOrEmpty()){
            val npath = WorkFileManager(context).basePath.path
            checkIn.setPhotoPath(npath)
            npath
        } else path

    }

    @Composable
    private fun SavePagerState(fuelLevel: PagerState, function: (Int) -> Unit) {
        LaunchedEffect(fuelLevel) {
            snapshotFlow { fuelLevel.currentPage }.collect { page ->
                function(page)
            }
        }

    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        var bitmap =
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )

        if (drawable is BitmapDrawable) {
            bitmap = drawable.bitmap
        }else {
            val canvas = Canvas(bitmap)

            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }

        return bitmap.let {
            val matrix = Matrix().apply { postRotate(-90f) }
            Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
        }
    }

}