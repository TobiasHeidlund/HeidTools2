package me.tubs.heidtools4.workApp.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.tubs.heidtools4.workApp.WorkActivity
import me.tubs.heidtools4.workApp.models.Checkin
import me.tubs.heidtools4.workApp.navStates
import me.tubs.heidtools4.workApp.support.CheckinDao
import me.tubs.heidtools4.workApp.support.WorkFileManager
import java.io.File

class View {


    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun views(
        initalObj: Int = 0,
        checkinDao: CheckinDao,
        day: String,
        navStates: navStates,
        activity: WorkActivity
    ) {
        val focusRequester = remember { FocusRequester() }
        val checkin: List<Checkin> by checkinDao.getDay(day).observeAsState(emptyList())
        val scope = rememberCoroutineScope()
        val fullParkingDialogScreen = remember { mutableStateOf(-1) }
        val viewConfiguration = LocalViewConfiguration.current
        val state = rememberPagerState(initialPage = 0) {
            checkin.size
        }
        LaunchedEffect(checkin) {
            val v = checkin.indexOf(checkin.find {
                it.uid == initalObj
            })
            if (v != -1) {
                state.scrollToPage(v)
            }


        }



        val interactionSource = remember { MutableInteractionSource() }
        LaunchedEffect(interactionSource) {
            var isLongClick = false

            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        isLongClick = false
                        delay(viewConfiguration.longPressTimeoutMillis)
                        isLongClick = true
                        shareAll(checkin[state.currentPage], activity)
                    }

                    is PressInteraction.Release -> {
                        if (isLongClick.not()) {
                            share(checkin[state.currentPage], activity)
                        }

                    }

                    is PressInteraction.Cancel -> {
                        isLongClick = false
                    }
                }
            }
        }

        Scaffold(
            bottomBar = {
                BottomAppBar(contentPadding = PaddingValues(horizontal = 20.dp),
                    actions = {
                        IconButton(onClick = {
                            val job =scope.launch (Dispatchers.IO){
                            checkinDao.delete(checkin[state.currentPage])
                        }
                            job.invokeOnCompletion {
                                navStates.navController.popBackStack()
                            } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Localized description")
                        }
                        IconButton(onClick = {
                            navStates.navAddCheckin(checkin[state.currentPage].uid.toLong()) }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Localized description",
                            )
                        }
                        IconButton(onClick = {
                            fullParkingDialogScreen.value = 0

                        }) {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = "Localized description",
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { /* do something */ },
                            interactionSource = interactionSource,
                            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                        ) {
                            Icon(Icons.Filled.Share, "Localized description")
                        }
                    }
                )
            }
        ) { innerPadding ->
        Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding())) {
            HorizontalPager(state = state) {
                view(checkin = checkin[it], checkinDao = checkinDao, navStates, activity,fullParkingDialogScreen,focusRequester)
            }
        }
    }
}



    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun view(
        checkin: Checkin,
        checkinDao: CheckinDao,
        navStates: navStates,
        activity: WorkActivity,
        fullParkingDialogScreen: MutableState<Int>,
        focusRequester: FocusRequester
    ): Unit {

        val fullImageScreen = remember { mutableStateOf(-1) }


        val scope = rememberCoroutineScope()
        val path = checkin.photoPath


        val keyBitmap: MutableState<ImageBitmap?> = remember { mutableStateOf(null) }
        val dashBitmap: MutableState<ImageBitmap?> = remember { mutableStateOf(null) }
        val extrasBitmaps: SnapshotStateList<ImageBitmap?> = remember { mutableStateListOf() }

        if (path.isNotEmpty()) {
            val fm = WorkFileManager(null, path)
            LaunchedEffect(path) {
                scope.launch(Dispatchers.IO) {
                if (fm.keyFile.exists())
                    keyBitmap.value = BitmapFactory.decodeFile(fm.keyPath).asImageBitmap()
                }
            }
            LaunchedEffect(path) {
                scope.launch(Dispatchers.IO) {
                    if (fm.dashFile.exists())
                        dashBitmap.value = BitmapFactory.decodeFile(fm.dashPath).asImageBitmap()
                }
            }
            LaunchedEffect(path) {
                scope.launch(Dispatchers.IO) {
                    fm.extraFile.listFiles()?.forEach {

                        val map = BitmapFactory.decodeFile(it.path).asImageBitmap()
                        extrasBitmaps.add(map)
                    }
                }
            }
        }


        Box(
            Modifier
                .width(400.dp)
                .height(800.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {


                val pagerState = rememberPagerState {
                    extrasBitmaps.size
                }
                VerticalPager(
                    state = pagerState,
                    Modifier
                        .height(300.dp)
                        .padding(top = 50.dp)
                        .width(300.dp)
                ) {

                    extrasBitmaps[it]?.let { it1 ->
                        Image(
                            bitmap = it1,
                            contentDescription = "Extra",
                            Modifier
                                .rotate(90f)
                                .padding(5.dp)
                                .height(240.dp)
                                .width(240.dp)
                                .combinedClickable(
                                    onClick = {
                                        fullImageScreen.value = it

                                    },
                                    onLongClick = {
                                        openImageinAnotherApp(activity, it1, checkin.photoPath)
                                    })
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(5.dp, 30.dp, 5.dp, 80.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    keyBitmap.value?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Key",
                            Modifier
                                .rotate(90f)
                                .fillMaxWidth(.5f)
                        )
                    }
                    dashBitmap.value?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Dash",
                            Modifier.rotate(90f)
                        )
                    }
                }

                val tSize = 30.sp
                val texts = checkin.toStringLong().split("\n")
                texts.forEach {
                    Text(text = it, fontSize = tSize)
                }


            }



            when {



                fullParkingDialogScreen.value!=-1 -> {
                    val text = if(checkin.parkingspot != -1){
                        Checkin.parkingspots[checkin.parkingspot]
                    }else{
                        ""
                    }
                    var textFieldValueState by remember {
                        mutableStateOf(
                            TextFieldValue(text
                                , TextRange( text.length)

                            ))
                    }
                    BackHandler {
                        fullParkingDialogScreen.value = -1
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = .8f))
                    ) {
                        OutlinedTextField(value = textFieldValueState, onValueChange ={
                            textFieldValueState = it

                            checkin.parkingspot = Checkin.parkingspots.indexOfFirst { it2: String ->
                                it2==it.text
                            }
                            scope.launch(Dispatchers.IO) {
                                checkinDao.update(checkin)
                            }
                        }, modifier = Modifier.align(Alignment.Center).focusRequester(focusRequester).background(Color.Black),
                            singleLine = true, maxLines = 1, keyboardActions = KeyboardActions(onDone = {
                            fullParkingDialogScreen.value = -1
                        }), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters) )
                    }
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }


            }


            when {
                fullImageScreen.value!=-1 -> {
                    BackHandler {
                        fullImageScreen.value = -1
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = .8f))
                    ) {
                        Image(bitmap = extrasBitmaps[fullImageScreen.value]!!, contentDescription = "TO BIG IMAGE",
                            Modifier
                                .clickable { fullImageScreen.value = -1 }
                                .rotate(90f)
                                .offset(x = 200.dp))
                    }


                }


            }
        }
    }

    private fun openImageinAnotherApp(activity: WorkActivity, it1: ImageBitmap,path:String) {
        val nr = File(path).name
        val file = File(activity.baseContext.externalCacheDir,"exp")
        val extproviderUri = FileProvider.getUriForFile(activity.baseContext, "me.heid.heidtools4.provider",file)

        val imageUris = ArrayList<Uri>().apply {
            val extrasPath = File(path,"Extra")
            if(extrasPath.isDirectory()){
                for (extras in extrasPath.listFiles()){
                    if(!File(file,"$nr/Extra/${extras.name}").exists()) {
                        extras.copyTo(File(file, "$nr/Extra/${extras.name}"))
                    }
                    add(extproviderUri.buildUpon().appendPath("$nr/Extra/${extras.name}").build())
                }
            }
        }


        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags =  Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(extproviderUri.buildUpon().appendPath("$nr/Extra/${file.name}").build(),"image/jpeg")
        }
        activity.startActivity(intent)
    }

    private fun share(checkin: Checkin, activity: WorkActivity) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(activity.baseContext.getSharedPreferences("Work",0).getString("SubjectEmail","").toString()))
            putExtra(Intent.EXTRA_SUBJECT, checkin.toStringShort())
            putExtra(Intent.EXTRA_TEXT,checkin.toStringShort())
            flags = (Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        shareIntent.flags = (Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        activity.startActivity(shareIntent)

    }

    private fun shareAll(checkin: Checkin, activity: WorkActivity) {
        val path = checkin.photoPath
        val nr = File(path).name
        val file = File(activity.baseContext.externalCacheDir,"exp")
        val extproviderUri = FileProvider.getUriForFile(activity.baseContext, "me.heid.heidtools4.provider",file)

        val imageUris = ArrayList<Uri>().apply {
            if(File(path+"/Key.jpeg").exists()){

                if(!File(file,"$nr/Key.jpeg").exists()) {
                    File(path!!, "Key.jpeg").copyTo(File(file, "$nr/Key.jpeg"))
                }
                add(extproviderUri.buildUpon().appendPath("$nr/Key.jpeg").build())
            }
            if(File(path+"/Dash.jpeg").exists()){
                if(!File(file,"$nr/Dash.jpeg").exists()) {
                    File(path!!, "Dash.jpeg").copyTo(File(file, "$nr/Dash.jpeg"))
                }
                add(extproviderUri.buildUpon().appendPath("$nr/Dash.jpeg").build())
            }

            val extrasPath = File(path,"Extra")
            if(extrasPath.isDirectory()){
                for (extras in extrasPath.listFiles()){
                    if(!File(file,"$nr/Extra/${extras.name}").exists()) {
                        extras.copyTo(File(file, "$nr/Extra/${extras.name}"))
                    }
                    add(extproviderUri.buildUpon().appendPath("$nr/Extra/${extras.name}").build())
                }
            }



        }


        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(activity.baseContext.getSharedPreferences("Work",0).getString("SubjectEmail","").toString()))
            putExtra(Intent.EXTRA_SUBJECT, checkin.toStringShort())
            putExtra(Intent.EXTRA_TEXT,checkin.toStringLong())
            flags = (Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM,imageUris)
            type = "image/*"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        shareIntent.flags = (Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        activity.startActivity(shareIntent)


    }

}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
