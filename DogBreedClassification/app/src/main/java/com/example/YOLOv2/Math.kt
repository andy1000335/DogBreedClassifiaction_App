package com.example.YOLOv2

import kotlin.math.exp

class Math {
    class ArgMax {
        class Result(private var index: Int, private var value: Float) {
            fun getIndex(): Int {
                return index
            }
            fun getValue(): Float {
                return value
            }
        }
        fun calculate(floatArray: FloatArray): Result {
            var maxIndex = 0
            for (i in 0 until floatArray.size) {
                if (floatArray[maxIndex] < floatArray[i]) {
                    maxIndex = i
                }
            }
            return Result(maxIndex, floatArray[maxIndex])
        }
    }

    class SoftMax {
        fun calculate(floatArray: FloatArray): FloatArray {
            var sum = 0F
            for (i in 0 until floatArray.size) {
                floatArray[i] = exp(floatArray[i])
                sum += floatArray[i]
            }
            if (sum < 0) {
                for (i in 0 until floatArray.size) {
                    floatArray[i] = (1.0 / floatArray.size.toFloat()).toFloat()
                }
            } else {
                for (i in 0 until floatArray.size) {
                    floatArray[i] = floatArray[i] / sum
                }
            }
            return floatArray
        }
    }
}