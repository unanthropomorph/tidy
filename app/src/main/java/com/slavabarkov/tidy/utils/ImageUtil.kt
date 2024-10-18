/**
 * Copyright 2023 Viacheslav Barkov
 * Copyright 2021 Microsoft Corporation.
 *
 * Parts of the following code are a derivative work of the code from the ONNX Runtime project,
 * which is licensed MIT.
 */

package com.slavabarkov.tidy

import android.graphics.*
import java.nio.FloatBuffer

const val DIM_BATCH_SIZE = 1
const val DIM_PIXEL_SIZE = 3
const val IMAGE_SIZE_X = 256
const val IMAGE_SIZE_Y = 256

fun preProcess(bitmap: Bitmap): FloatBuffer {
    val imgData = FloatBuffer.allocate(
        DIM_BATCH_SIZE * DIM_PIXEL_SIZE * IMAGE_SIZE_X * IMAGE_SIZE_Y
    )
    imgData.rewind()
    val stride = IMAGE_SIZE_X * IMAGE_SIZE_Y
    val bmpData = IntArray(stride)
    bitmap.getPixels(bmpData, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    for (i in 0 until IMAGE_SIZE_X) {
        for (j in 0 until IMAGE_SIZE_Y) {
            val idx = IMAGE_SIZE_Y * i + j
            val pixelValue = bmpData[idx]
            imgData.put(idx, (((pixelValue shr 16 and 0xFF) / 255f - 0.0f) / 1.0f))
            imgData.put(
                idx + stride, (((pixelValue shr 8 and 0xFF) / 255f - 0.0f) / 1.0f)
            )
            imgData.put(
                idx + stride * 2, (((pixelValue and 0xFF) / 255f - 0.0f) / 1.0f)
            )
        }
    }

    imgData.rewind()
    return imgData
}

fun centerCrop(bitmap: Bitmap, imageSize: Int): Bitmap {
    val cropX: Int
    val cropY: Int
    val cropSize: Int
    if (bitmap.width >= bitmap.height) {
        cropX = bitmap.width / 2 - bitmap.height / 2
        cropY = 0
        cropSize = bitmap.height
    } else {
        cropX = 0
        cropY = bitmap.height / 2 - bitmap.width / 2
        cropSize = bitmap.width
    }
    var bitmapCropped = Bitmap.createBitmap(
        bitmap, cropX, cropY, cropSize, cropSize
    )
    bitmapCropped = Bitmap.createScaledBitmap(
        bitmapCropped, imageSize, imageSize, false
    )
    return bitmapCropped
}

fun resizeAndPad(bitmap: Bitmap, targetSize: Int = 256): Bitmap {
    // Create a new bitmap with the target size and a black background
    val paddedBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(paddedBitmap)
    canvas.drawColor(Color.BLACK) // Fill the background with black

    // Calculate padding for centering
    val scale: Float
    val xOffset: Int
    val yOffset: Int

    if (bitmap.width > bitmap.height) {
        scale = targetSize.toFloat() / bitmap.width
        val newHeight = (bitmap.height * scale).toInt()
        yOffset = (targetSize - newHeight) / 2
        xOffset = 0
    } else {
        scale = targetSize.toFloat() / bitmap.height
        val newWidth = (bitmap.width * scale).toInt()
        xOffset = (targetSize - newWidth) / 2
        yOffset = 0
    }

    // Create a scaled version of the original bitmap
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 
        (bitmap.width * scale).toInt(), 
        (bitmap.height * scale).toInt(), 
        false)

    // Draw the scaled bitmap onto the padded canvas
    canvas.drawBitmap(scaledBitmap, xOffset.toFloat(), yOffset.toFloat(), null)

    return paddedBitmap
}
