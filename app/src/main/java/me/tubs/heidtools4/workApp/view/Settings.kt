package me.tubs.heidtools4.workApp.view

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

class Settings {

    @Composable
    fun view(context: Context){
        val text = remember {
            mutableStateOf(context.getSharedPreferences("Work",0).getString("SubjectEmail","")!!)
        }
        context.getSharedPreferences("Work",0).edit().putString("SubjectEmail",text.value).apply()

        Box(Modifier.fillMaxWidth(.9f)){
            TextField(value = text.value,
                onValueChange = {it->
                  text.value = it
                }, modifier = Modifier
                .height(90.dp)
                .align(Alignment.TopCenter)
                .padding(top = 30.dp),
                singleLine = true,
                placeholder ={
                    Text(text = "Insert email here")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        }



    }
}