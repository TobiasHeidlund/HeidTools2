package me.tubs.heidtools4.workApp.view

import android.graphics.Bitmap
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.ArrayList

class Ocr{

    internal fun CheckImage(it: Bitmap, onSuccessListener: OnSuccessListener<Text>, onFailureListener: OnFailureListener = OnFailureListener {
        it.printStackTrace()
    }) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val impImage = InputImage.fromBitmap(it, 0)

        recognizer.process(impImage)
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }


    internal fun checkKeyText(visionText: Text): String {
        var text: String
        for (blocks in visionText.textBlocks) {
            for (line in blocks.lines)
                for (elements in line.elements) {
                    text = elements.text.lowercase()
                    if (text.contains("se")) {
                        text = text.replace('o', '0')
                        if(text.contains(Regex("se\\d{5}")) && text.length >= 7) {
                            val start = text.indexOf("se")
                            text = text.substring(start, start+7 )
                            return text
                        }
                    }

                }
        }
        return ""
    }

    internal fun checkDashText(visionText: Text): Int {
        var text: String
        val texts = ArrayList<Int>()
        for (blocks in visionText.textBlocks) {
            for (line in blocks.lines)
                for (elements in line.elements) {
                    text = elements.text.lowercase()

                    text = text.replace('o', '0')
                    var number = ""
                    text.toCharArray().forEach {
                        if (it.isDigit()) number += it
                    }
                    try{
                        if (number.isNotEmpty()) texts.add(number.toInt())
                    }catch (e:java.lang.NumberFormatException){
                        e.printStackTrace()
                    }



                }
        }
        var greatest = 0
        texts.forEach {
            if (it > greatest) greatest = it
        }

        return greatest
    }
}