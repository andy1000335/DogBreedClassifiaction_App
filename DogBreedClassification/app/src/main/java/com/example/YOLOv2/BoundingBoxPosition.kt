package com.example.YOLOv2

class BoundingBoxPosition {
    private var confidence = 0F
    private var left = 0F
    private var right = 0F
    private var top = 0F
    private var bottom = 0F

    fun getPosition(box: BoundingBox): BoundingBoxPosition {
        val result = Math.ArgMax().calculate(Math.SoftMax().calculate(box.getClasses()))
        val confidenceOfClass = result.getValue() * box.getConfidence()
        val boundingBoxPosition = BoundingBoxPosition()
        if (confidenceOfClass > THRESHOLD) {
            val tempLeft = box.getX() - box.getW()/2
            val tempTop = box.getY() - box.getH()/2
            val tempRight = tempLeft + box.getW()
            val tempBottom = tempTop + box.getH()

            var left = listOf(tempLeft, tempRight).min()!!
            var right = listOf(tempLeft, tempRight).max()!!
            var top = listOf(tempTop, tempBottom).min()!!
            var bottom = listOf(tempTop, tempBottom).max()!!

            if (left < 0) {
                left = 0f
            }
            if (right > INPUTE_SIZE) {
                right = INPUTE_SIZE.toFloat()
            }
            if (top < 0) {
                top = 0f
            }
            if (bottom > INPUTE_SIZE) {
                bottom = INPUTE_SIZE.toFloat()
            }

            boundingBoxPosition.setConfidence(confidenceOfClass)
            boundingBoxPosition.setLeft(left)
            boundingBoxPosition.setRight(right)
            boundingBoxPosition.setTop(top)
            boundingBoxPosition.setBottom(bottom)
        } else {
            boundingBoxPosition.setConfidence(0F)
            boundingBoxPosition.setLeft(0F)
            boundingBoxPosition.setRight(0F)
            boundingBoxPosition.setTop(0F)
            boundingBoxPosition.setBottom(0F)
        }
        return  boundingBoxPosition
    }

    private fun setConfidence(confidence: Float) {
        this.confidence = confidence
    }
    private fun setLeft(left: Float) {
        this.left = left
    }
    private fun setRight(right: Float) {
        this.right = right
    }
    private fun setTop(top: Float) {
        this.top = top
    }
    private fun setBottom(bottom: Float) {
        this.bottom = bottom
    }

    fun getConfidence(): Float {
        return this.confidence
    }
    fun getLeft(): Float {
        return this.left
    }
    fun getRight(): Float {
        return this.right
    }
    fun getTop(): Float {
        return this.top
    }
    fun getBottom(): Float {
        return this.bottom
    }
}