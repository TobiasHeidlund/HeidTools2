package me.tubs.heidtools4

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class test4: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Cyan)
                .padding(30.dp)){
                val state = rememberScrollState()
                Log.i("TAG", "onCreate: ${state.value}")


                Column(
                    Modifier
                        .background(Color.Red)
                        .height(50.dp)
                        .fillMaxWidth()
                        .verticalScroll(
                            state

                        )
                    ){
                    for(i in 0..100){
                        Box(modifier = Modifier.height(50.dp)){
                            Text(i.toString(), fontSize = 34.sp)
                        }
                    }
                }

            }




        }

    }
}