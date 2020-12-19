package com.example.YOLOv2

class BoundingBox {
    private var x = 0.0f
    private var y = 0.0f
    private var w = 0.0f
    private var h = 0.0f
    private var confidence = 0.0f
    private var classes: FloatArray = FloatArray(CLASS_NUM)

    fun setX(x: Float) {
        this.x = x
    }
    fun setY(y: Float) {
        this.y = y
    }
    fun setW(w: Float) {
        this.w = w
    }
    fun setH(h: Float) {
        this.h = h
    }
    fun setConfidence(confidence: Float) {
        this.confidence = confidence
    }
    fun setClasses(classes: FloatArray) {
        this.classes = classes
    }

    fun getX(): Float {
        return x
    }
    fun getY(): Float {
        return y
    }
    fun getW(): Float {
        return w
    }
    fun getH(): Float {
        return h
    }
    fun getConfidence(): Float {
        return confidence
    }
    fun getClasses(): FloatArray {
        return classes
    }
}