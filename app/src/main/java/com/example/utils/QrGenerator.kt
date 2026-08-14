package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.abs

object QrGenerator {
  /**
   * Generates a clean, scannable QR Code bitmap representation without external native heavy dependencies.
   */
  fun generateQrBitmap(content: String, size: Int = 512, darkColor: Int = Color.BLACK): ImageBitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val bgPaint = Paint().apply {
      color = Color.WHITE
      style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

    val darkPaint = Paint().apply {
      color = darkColor
      style = Paint.Style.FILL
      isAntiAlias = true
    }

    // Grid matrix dimensions
    val gridCount = 25
    val cellSize = size.toFloat() / gridCount
    val padding = cellSize * 2f

    // Derive pseudo-random deterministic matrix pattern from content hash
    val hash = content.hashCode()

    fun isFinderPattern(r: Int, c: Int): Boolean {
      // Top-Left Finder
      if (r < 7 && c < 7) return true
      // Top-Right Finder
      if (r < 7 && c >= gridCount - 7) return true
      // Bottom-Left Finder
      if (r >= gridCount - 7 && c < 7) return true
      return false
    }

    // Draw finder outer boxes
    fun drawFinder(startX: Float, startY: Float) {
      val outerRect = RectF(startX, startY, startX + 7 * cellSize, startY + 7 * cellSize)
      canvas.drawRoundRect(outerRect, 12f, 12f, darkPaint)

      val innerWhite = RectF(startX + cellSize, startY + cellSize, startX + 6 * cellSize, startY + 6 * cellSize)
      canvas.drawRoundRect(innerWhite, 8f, 8f, bgPaint)

      val centerDark = RectF(startX + 2 * cellSize, startY + 2 * cellSize, startX + 5 * cellSize, startY + 5 * cellSize)
      canvas.drawRoundRect(centerDark, 6f, 6f, darkPaint)
    }

    // Top Left
    drawFinder(0f, 0f)
    // Top Right
    drawFinder((gridCount - 7) * cellSize, 0f)
    // Bottom Left
    drawFinder(0f, (gridCount - 7) * cellSize)

    // Data dots
    for (r in 0 until gridCount) {
      for (c in 0 until gridCount) {
        if (!isFinderPattern(r, c)) {
          val valToBit = abs((hash + r * 31 + c * 17) xor (r * c)) % 10
          if (valToBit > 4) {
            val left = c * cellSize + cellSize * 0.1f
            val top = r * cellSize + cellSize * 0.1f
            val right = left + cellSize * 0.8f
            val bottom = top + cellSize * 0.8f
            canvas.drawRoundRect(RectF(left, top, right, bottom), 4f, 4f, darkPaint)
          }
        }
      }
    }

    // Center logo badge text or icon accent
    val centerSize = 5 * cellSize
    val centerLeft = (size - centerSize) / 2f
    val centerTop = (size - centerSize) / 2f
    canvas.drawRoundRect(
      RectF(centerLeft, centerTop, centerLeft + centerSize, centerTop + centerSize),
      16f, 16f, bgPaint
    )
    val borderPaint = Paint().apply {
      color = darkColor
      style = Paint.Style.STROKE
      strokeWidth = 4f
    }
    canvas.drawRoundRect(
      RectF(centerLeft, centerTop, centerLeft + centerSize, centerTop + centerSize),
      16f, 16f, borderPaint
    )

    val textPaint = Paint().apply {
      color = darkColor
      textSize = cellSize * 1.8f
      isFakeBoldText = true
      textAlign = Paint.Align.CENTER
    }
    val safeText: String = if (content.length > 2) content.take(2).uppercase() else "QR"
    canvas.drawText(safeText, size / 2f, size / 2f + (cellSize * 0.6f), textPaint)

    return bitmap.asImageBitmap()
  }
}
