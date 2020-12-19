package com.example.YOLOv2

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import android.widget.Toast
import org.apache.commons.math3.analysis.function.Sigmoid
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import kotlin.math.exp

class YOLO {
    private var bitmap: Bitmap? = null
    private val MODEL_PATH = "file:///android_asset/yolov2_dog.pb"

    fun runYOLOv2(assets: AssetManager, bm: Bitmap): Bitmap? {
        val preprocessedImage = imagePreprocessing(bm)
        val floatArray = Bitmap2FloatArray(preprocessedImage)
        val inferenceInterface = TensorFlowInferenceInterface(assets, MODEL_PATH)
        val outputSize = 13 * 13 * (5 + CLASS_NUM) * 5
        val predict = FloatArray(outputSize)

        try {
            inferenceInterface.feed("input", floatArray, INTPUT_BATCH.toLong(), INPUTE_SIZE.toLong(), INPUTE_SIZE.toLong(), INPUT_CHANNEL.toLong())
            inferenceInterface.run(arrayOf("output"))
            inferenceInterface.fetch("output", predict)
        } catch (e: Exception) {
            Log.e("model load error", e.toString())
        }

        var offset = 0
        var bestBoundingBoxPosition = BoundingBoxPosition()
        for (cellRow in 0 until CELL_NUM) {
            for (cellCol in 0 until CELL_NUM) {
                for (box in 0 until ANCHOR_NUM) {
                    val boundingBox = getBoundingBox(predict, cellCol, cellRow, box, offset)
                    val boundingBoxPosition = BoundingBoxPosition().getPosition(boundingBox)
                    if (boundingBoxPosition.getConfidence() > bestBoundingBoxPosition.getConfidence()) {
                        bestBoundingBoxPosition = boundingBoxPosition
                    }
                    offset += (5 + CLASS_NUM)
                }
            }
        }
        if (bestBoundingBoxPosition.getConfidence() != 0f) {
            bitmap = getBoundingBoxImage(preprocessedImage, bestBoundingBoxPosition)
        }

        return bitmap
    }

    private fun scaleBitmap(bm: Bitmap, maxSize: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = maxSize.toFloat() / width
        val scaleHeight = maxSize.toFloat() / height
        val scaleRate = listOf(scaleWidth, scaleHeight).min()!!

        val matrix = Matrix()
        matrix.postScale(scaleRate, scaleRate)

        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)
    }

    private fun imagePreprocessing(bm: Bitmap): Bitmap {
        val scaleImage = scaleBitmap(bm, INPUTE_SIZE)
        val intValues = IntArray(INPUTE_SIZE * INPUTE_SIZE)
        for (i in intValues.indices) {
            intValues[i] = Color.GRAY
        }

        when {
            scaleImage.width > scaleImage.height -> scaleImage.getPixels(intValues, INPUTE_SIZE*((INPUTE_SIZE-scaleImage.height)/2), INPUTE_SIZE, 0, 0, scaleImage.width, scaleImage.height)
            scaleImage.width < scaleImage.height -> scaleImage.getPixels(intValues, (INPUTE_SIZE-scaleImage.width)/2, INPUTE_SIZE, 0, 0, scaleImage.width, scaleImage.height)
            else -> scaleImage.getPixels(intValues, 0, INPUTE_SIZE, 0, 0, scaleImage.width, scaleImage.height)
        }
        val image = Bitmap.createBitmap(INPUTE_SIZE, INPUTE_SIZE, Bitmap.Config.ARGB_8888)
        image!!.setPixels(intValues, 0, INPUTE_SIZE, 0, 0, INPUTE_SIZE, INPUTE_SIZE)

        return image
    }

    private fun Bitmap2FloatArray (bm: Bitmap): FloatArray {
        val intValues = IntArray(INPUTE_SIZE * INPUTE_SIZE)
        val floatValues = FloatArray(INPUTE_SIZE * INPUTE_SIZE* INPUT_CHANNEL)

        bm.getPixels(intValues, 0, INPUTE_SIZE, 0, 0, bm.width, bm.height)
        for (i in intValues.indices) {
            val value = intValues[i]
            floatValues[i * 3 + 0] = ((value shr 16 and 0xFF)-128) / 128.0f
            floatValues[i * 3 + 1] = ((value shr 8 and 0xFF)-128) /  128.0f
            floatValues[i * 3 + 2] = ((value and 0xFF)-128) / 128.0f
        }
        return floatValues
    }

    private fun getBoundingBox(tfOutput: FloatArray, col: Int, row: Int, b: Int, offset: Int): BoundingBox {
        val boundingBox = BoundingBox()

        boundingBox.setX(((col + Sigmoid().value(tfOutput[offset].toDouble())) * 32).toFloat())
        boundingBox.setY(((row + Sigmoid().value(tfOutput[offset+1].toDouble())) * 32).toFloat())
        boundingBox.setW((exp(tfOutput[offset+2].toDouble()) * ANCHORS[2*b] * 32).toFloat())
        boundingBox.setH((exp(tfOutput[offset+3].toDouble()) * ANCHORS[2*b+1] * 32).toFloat())
        boundingBox.setConfidence(Sigmoid().value(tfOutput[offset+4].toDouble()).toFloat())

        val classes = FloatArray(CLASS_NUM)
        for (i in 0 until CLASS_NUM) {
            classes[i] = tfOutput[i + offset + 5]
        }
        boundingBox.setClasses(classes)
        return boundingBox
    }

    private fun getBoundingBoxImage(bm: Bitmap, boundingBoxPosition: BoundingBoxPosition): Bitmap {
        val width = (boundingBoxPosition.getRight() - boundingBoxPosition.getLeft()).toInt()
        val height = (boundingBoxPosition.getBottom() - boundingBoxPosition.getTop()).toInt()
        val intValues = IntArray(width * height)
        val x = boundingBoxPosition.getLeft().toInt()
        val y = boundingBoxPosition.getTop().toInt()
        val detectedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        bm.getPixels(intValues, 0, width, x, y, width, height)
        detectedBitmap.setPixels(intValues, 0, width, 0, 0, width, height)

        return detectedBitmap
    }
}