package me.tubs.heidtools4

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import me.tubs.heidtools4.ui.theme.HeidTools4Theme
import me.tubs.heidtools4.workApp.WorkActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeidTools4Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                        listItem(
                            name = "Work",
                            icon = ContextCompat.getDrawable(baseContext,R.drawable.ic_launcher_foreground)!!,
                            modifier = Modifier.clickable {
                                    startActivity(Intent(applicationContext,WorkActivity::class.java))
                            }
                        )
                        listItem(
                            name = "TEst2",
                            icon = ContextCompat.getDrawable(baseContext,R.drawable.ic_launcher_foreground)!!,
                            modifier = Modifier.clickable {
                                startActivity(Intent(applicationContext,test4::class.java))

                            }
                        )
                        listItem(
                            name = "TEst3",
                            icon = ContextCompat.getDrawable(baseContext,R.drawable.ic_launcher_foreground)!!,
                            modifier = Modifier.clickable {

                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun listItem(name:String, icon: Drawable,modifier: Modifier) {
    Row(horizontalArrangement = Arrangement.Absolute.Left, verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth())
    {
        Icon(bitmap = icon.toBitmap(256, 256).asImageBitmap(), contentDescription = name)
        Text(text = name, textAlign = TextAlign.Center,
            fontSize = TextUnit(22f,TextUnitType.Sp),
            modifier = Modifier.apply {
                fillMaxWidth()
                fillMaxHeight()
            })
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HeidTools4Theme {
        Column {

        }
    }
}