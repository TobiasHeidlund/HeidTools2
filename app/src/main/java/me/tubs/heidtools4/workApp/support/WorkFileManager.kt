package me.tubs.heidtools4.workApp.support

import android.content.Context
import java.io.File
import java.time.LocalDate

class WorkFileManager(private val context: Context?, private val path:String? = null){
    val extraFile = File(basePath,"Extra")
    val dashFile = File(basePath,"Dash.jpeg")
    val keyFile = File(basePath,"Key.jpeg")
    val dataFile =  File(basePath, "data.json")


    val extraPath: String get() = extraFile.path
    val dashPath: String get() = dashFile.path
    val keyPath: String get() = keyFile.path
    val nextExtra :String get() = File(extraFile,"${extraFile.listFiles()?.size}.jpeg").path

    var bp: File? = null

    val basePath: File get() = bp ?: genBasePath()

    /* val keyUri get() = FileProvider.getUriForFile(
         context,
         BuildConfig.APPLICATION_ID + ".provider",
         keyFile
     )
     val dashUri get() = FileProvider.getUriForFile(
         context,
         BuildConfig.APPLICATION_ID + ".provider",
         dashFile
     )
     val extraUri get() = FileProvider.getUriForFile(
         context,
         BuildConfig.APPLICATION_ID + ".provider",
         basePath
     )*/
    private fun genBasePath(): File {
        if (path == null) {
            val cd = LocalDate.now()
            val date = "${cd.year}-${cd.monthValue}-${cd.dayOfMonth}"

            val filepath = (date).replace("-", "/")
            val file = File(context?.filesDir?.path, "data/$filepath")
            var highestnr = 0
            if (!file.exists()) {
                file.mkdirs()
            }
            for (f in file.listFiles()!!) {
                val i = f.name.substring(3).toInt()
                if (i > highestnr) {
                    highestnr = i;
                }
            }
            highestnr = highestnr.inc()
            bp = File(file, "car$highestnr")
            File(bp, "Extra").mkdirs()
            return bp as File;
        } else {
            bp = File(path)
            return File(path)
        }
    }
}