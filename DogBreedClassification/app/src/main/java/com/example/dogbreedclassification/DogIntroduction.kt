package com.example.dogbreedclassification

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.View
import kotlinx.android.synthetic.main.activity_dog_introduction.*
import org.htmlcleaner.HtmlCleaner
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern

class DogIntroduction : AppCompatActivity() {


    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_introduction)

        val intent: Intent = intent
        index = intent.getIntExtra("index", 0)

        StrictMode
            .setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build()
            )
        StrictMode
            .setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
        showIntroduction(index)
        tv_hyperLink.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        tv_hyperLink.setTextColor(resources.getColor(R.color.lightBlue2))
    }

    fun getBitmapFromURL(source: String): Bitmap? {
        try {
            val url = URL(source)
            try {
                val conn = url.openConnection() as HttpURLConnection
                conn.connect()
                val input = conn.inputStream
                return BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null
        }
        return null
    }

    private fun showIntroduction(idx: Int) {
        val wikiURL = resources.getStringArray(R.array.wiki_url)[idx]

        val cleaner = HtmlCleaner()
        try {
            tv_dogBreed.text = resources.getStringArray(R.array.CH_breeds)[idx]

            tv_information.text = resources.getStringArray(R.array.dogIntroduction)[idx]

            val imageNode = cleaner.clean(URL(wikiURL))
            val image = imageNode.getElementsByAttValue(
                "class",
                "mw-parser-output",
                true,
                true
            )[0].getElementsByName("img", true)
            val imgUrl = "https://" + image[0].getAttributeByName("src")
            iv_imageExample.setImageBitmap(getBitmapFromURL(imgUrl))

        } catch (e: Exception) {
            iv_imageExample.setImageResource(android.R.drawable.ic_dialog_alert)
//            tv_information.text = "資料搜尋時發生錯誤 :("
//            tv_information.text = e.toString()
            e.printStackTrace()
        }
    }

    private fun initial() {
        tv_dogBreed.text = ""
        tv_information.text = ""
        iv_imageExample.setImageBitmap(null)
    }

    fun onNext(v: View) {
        initial()
        index += 1
        if (index > 119) index = 0
        showIntroduction(index)
    }

    fun onPrevious(v: View) {
        initial()
        index -= 1
        if (index < 0) index = 119
        showIntroduction(index)
    }

    fun onOpenWeb(v: View) {
        val webIntent = Intent(Intent.ACTION_VIEW)
        webIntent.data = Uri.parse(resources.getStringArray(R.array.wiki_url)[index])
        startActivity(webIntent)
    }
}
