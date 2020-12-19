package com.example.dogbreedclassification

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.Classification.DogClassification
import com.example.YOLOv2.YOLO
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException
import java.lang.Exception
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private var bitmap: Bitmap? = null
    private var yoloOutput: Bitmap? = null

    private var firstIndex: Int = 0
    private var secondIndex: Int = 0
    private var thirdIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPieChart()
        iv_showImage.setImageResource(R.drawable.sample_dog)

        sw_switchImage.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (yoloOutput != null) {
                    iv_showImage.setImageBitmap(yoloOutput)
                } else {
                    if (bitmap != null) {
                        iv_showImage.setImageBitmap(bitmap)
                    } else {
                        iv_showImage.setImageResource(R.drawable.sample_dog)
                    }
                }
            } else {
                if (bitmap != null) {
                    iv_showImage.setImageBitmap(bitmap)
                } else {
                    iv_showImage.setImageResource(R.drawable.sample_dog)
                }
            }
        }
    }

    private fun initPieChart () {
        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#00000000"))

        val pieContent = ArrayList<PieEntry>()
        pieContent.add(PieEntry(0f, ""))

        val dataSet = PieDataSet(pieContent, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 0f

        val pieData = PieData(dataSet)
        pieChart.description.isEnabled = false    // 不顯示描述文字
        pieChart.setEntryLabelTextSize(0f)
        pieChart.legend.isEnabled = false    // 不顯示標籤
        pieChart.data = pieData
        pieChart.invalidate()    //刷新

        tv_firstColor.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        tv_secondColor.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        tv_thirdColor.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        tv_othersColor.setBackgroundColor(Color.parseColor("#00FFFFFF"))

        tv_firstText.text = ""
        tv_secondText.text = ""
        tv_thirdText.text = ""
        tv_othersText.text = ""
    }

     fun onChooseImage (v: View) {
        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, 100)
     }

    fun onCamera(v: View) {
        intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode==Activity.RESULT_OK && requestCode==100) {
            val imageUri = data?.data
            val cr = this.contentResolver

            try {
                bitmap = BitmapFactory.decodeStream(cr.openInputStream(imageUri!!))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            iv_showImage.setImageBitmap(bitmap)
        }
        else if (resultCode==Activity.RESULT_OK && requestCode==200) {
            val bundle = data?.extras
            bitmap = bundle?.get("data") as Bitmap?
            iv_showImage.setImageBitmap(bitmap)
        }
    }

    private fun Percentage(f: Float): Float {
        return (f * 10000).roundToInt().toFloat() / 100
    }

    private fun sortAccuracy(accuracy: FloatArray): ArrayList<Pair<Float, String>> {
        val list = ArrayList<Pair<Float, String>>()
        val breeds = resources.getStringArray(R.array.CH_breeds)

        var firstAcc = 0f
        var secondAcc = 0f
        var thirdAcc = 0f
        val others: Float

        for (i in accuracy.indices) {
            val acc = accuracy[i]
            when {
                acc > firstAcc -> {
                    firstAcc = acc
                    firstIndex = i
                }
                acc > secondAcc -> {
                    secondAcc = acc
                    secondIndex = i
                }
                acc > thirdAcc -> {
                    thirdAcc = acc
                    thirdIndex = i
                }
            }
        }
        firstAcc = Percentage(firstAcc)
        secondAcc = Percentage(secondAcc)
        thirdAcc = Percentage(thirdAcc)
        others = ((100f - firstAcc - secondAcc - thirdAcc) * 100).roundToInt().toFloat() / 100
        list.add(Pair(firstAcc, breeds[firstIndex]))
        list.add(Pair(secondAcc, breeds[secondIndex]))
        list.add(Pair(thirdAcc, breeds[thirdIndex]))
        list.add(Pair(others, "其他"))
        return list
    }

    fun enableButton(enable: Boolean) {
        bt_chooseImage.isEnabled = enable
        bt_camera.isEnabled = enable
        bt_predict.isEnabled = enable
    }

    @SuppressLint("SetTextI18n")
    fun onPredictDogBreeds(v: View) {
        enableButton(false)
        if (bitmap != null) {
            initPieChart()
            Toast.makeText(this, "圖片偵測中", Toast.LENGTH_LONG).show()
            Handler().postDelayed({    // delay 0.5s
                yoloOutput = YOLO().runYOLOv2(assets, bitmap!!)
                if (yoloOutput != null) {
                    Toast.makeText(this, "圖片偵測成功，開始預測種類", Toast.LENGTH_SHORT).show()
                    Handler().postDelayed({    // delay 0.5s
                        val predict = DogClassification().runClassification(assets, yoloOutput!!)
                        val sortAcc = sortAccuracy(predict)
                        val pieContent = ArrayList<PieEntry>()
                        for (i in 0..3) {
                            val text = sortAcc[i].second + "(" + sortAcc[i].first + "%)"
                            pieContent.add(PieEntry(sortAcc[i].first, text))
                        }

                        if (predict.isNotEmpty()) {
                            enableButton(true)
                        }

                        tv_firstText.text = sortAcc[0].second + "\n(" + sortAcc[0].first + "%)"
                        tv_secondText.text = sortAcc[1].second + "\n(" + sortAcc[1].first + "%)"
                        tv_thirdText.text = sortAcc[2].second + "\n(" + sortAcc[2].first + "%)"
                        tv_othersText.text = sortAcc[3].second + "\n(" + sortAcc[3].first + "%)"

                        val colors = ArrayList<Int>()
                        colors.add(resources.getColor(R.color.lightRed))
                        colors.add(resources.getColor(R.color.lightBlue))
                        colors.add(resources.getColor(R.color.lightOrange))
                        colors.add(resources.getColor(R.color.lightGreen))

                        tv_firstColor.setBackgroundColor(resources.getColor(R.color.lightRed))
                        tv_secondColor.setBackgroundColor(resources.getColor(R.color.lightBlue))
                        tv_thirdColor.setBackgroundColor(resources.getColor(R.color.lightOrange))
                        tv_othersColor.setBackgroundColor(resources.getColor(R.color.lightGreen))

                        val dataSet = PieDataSet(pieContent, "")
                        dataSet.colors = colors
                        dataSet.valueTextSize = 0f

                        val pieData = PieData(dataSet)
                        pieChart.setEntryLabelColor(Color.BLACK)
                        pieChart.description.isEnabled = false    // 不顯示描述文字
                        pieChart.setEntryLabelTextSize(0f)
                        pieChart.data = pieData
                        pieChart.legend.isEnabled = false    // 不顯示標籤
                        pieChart.invalidate()    // 刷新

                        Toast.makeText(this, "預測成功", Toast.LENGTH_SHORT).show()
                    }, 500)
                } else {
                    enableButton(true)
                    Toast.makeText(this, "圖片未偵測到狗", Toast.LENGTH_SHORT).show()
                }
            }, 500)
        } else {
            enableButton(true)
            Toast.makeText(this, "請選擇圖片", Toast.LENGTH_SHORT).show()
        }
    }

    fun onShowIntroduction(v: View) {
        v as TextView
        if (v.text.isNotEmpty()) {
            val intent = Intent()
            when (v) {
                tv_firstText -> intent.putExtra("index", firstIndex)
                tv_secondText -> intent.putExtra("index", secondIndex)
                tv_thirdText -> intent.putExtra("index", thirdIndex)
            }
            intent.setClass(this, DogIntroduction::class.java)
            startActivity(intent)
        }
    }

}
