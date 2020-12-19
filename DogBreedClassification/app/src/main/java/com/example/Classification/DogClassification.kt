package com.example.Classification

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Matrix
import org.tensorflow.contrib.android.TensorFlowInferenceInterface

class DogClassification {
    private val MODEL_PATH = "file:///android_asset/Xception.pb"
    private val INPUT_WIDTH: Int = 200
    private val INPUT_HEIGHT: Int = 200
    private val INPUT_CHANNEL: Int = 3
    private val INTPUT_BATCH: Int = 1

    fun runClassification(assets: AssetManager, bm: Bitmap): FloatArray {
        val resizeBitmap = scaleBitmap(bm, INPUT_WIDTH, INPUT_HEIGHT)
        val array = Bitmap2FloatArray(resizeBitmap)
        val inferenceInterface = TensorFlowInferenceInterface(assets, MODEL_PATH)
        inferenceInterface.feed("xception_input", array, INTPUT_BATCH.toLong(), INPUT_WIDTH.toLong(), INPUT_HEIGHT.toLong(), INPUT_CHANNEL.toLong())
        val predict = FloatArray(120)
        inferenceInterface.run(arrayOf("output_1"))
        inferenceInterface.fetch("output_1", predict)

        return predict
    }

    private fun scaleBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)
    }

    private fun Bitmap2FloatArray(bm: Bitmap): FloatArray {
        val intValues = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
        val floatValues = FloatArray(INPUT_WIDTH * INPUT_HEIGHT * INPUT_CHANNEL)
        bm.getPixels(intValues, 0, bm.width, 0, 0, bm.width, bm.height)
        for (i in intValues.indices) {
            val value = intValues[i]
            floatValues[i * 3 + 0] = ((value shr 16 and 0xFF) - 128) / 128.0f
            floatValues[i * 3 + 1] = ((value shr 8 and 0xFF) - 128) / 128.0f
            floatValues[i * 3 + 2] = ((value and 0xFF) - 128) / 128.0f
        }
        return floatValues
    }

}