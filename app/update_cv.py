import re

def update_ats():
    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "r", encoding="utf-8") as f:
        content = f.read()

    # 1. drawPhotoAt
    idx = content.find("fun drawPhotoAt(")
    if idx != -1:
        end_idx = content.find("    // ================= BRANCH 1: EXECUTIVE TWO-COLUMN SIDEBAR", idx)
        if end_idx != -1:
            new_draw_photo = """fun drawPhotoAt(px: Float, py: Float, size: Float, shape: String = data.photoShape, borderColor: Int = primaryColor) {
        if (photoBitmap == null) return
        val bw = photoBitmap.width.toFloat()
        val bh = photoBitmap.height.toFloat()
        if (bw <= 0f || bh <= 0f) return

        val widthRatio = (data.photoWidth.coerceIn(50, 130)) / 80f
        val heightRatio = (data.photoHeight.coerceIn(50, 140)) / 80f
        val pw = size * widthRatio
        val ph = if (shape == "Circle" || shape == "Square") pw else (size * heightRatio)
        val cornerRad = (data.photoCornerRadius.toFloat() * (pw / 80f)).coerceAtLeast(2f)

        canvas.save()
        val path = android.graphics.Path()
        val photoRect = android.graphics.RectF(px, py, px + pw, py + ph)
        when (shape) {
            "Circle" -> {
                val rad = minOf(pw, ph) / 2f
                path.addCircle(px + pw / 2f, py + ph / 2f, rad, android.graphics.Path.Direction.CW)
            }
            "Oval" -> {
                path.addOval(photoRect, android.graphics.Path.Direction.CW)
            }
            "Rounded" -> {
                path.addRoundRect(photoRect, cornerRad, cornerRad, android.graphics.Path.Direction.CW)
            }
            "Rectangle" -> {
                path.addRoundRect(photoRect, 3f, 3f, android.graphics.Path.Direction.CW)
            }
            else -> { // Square
                path.addRect(photoRect, android.graphics.Path.Direction.CW)
            }
        }
        canvas.clipPath(path)

        canvas.save()
        val cx = px + pw / 2f
        val cy = py + ph / 2f
        canvas.translate(cx, cy)
        canvas.scale(data.photoScale, data.photoScale)
        canvas.translate(data.photoOffsetX / 3.2f, data.photoOffsetY / 3.2f)
        canvas.translate(-cx, -cy)

        // Center Crop computation:
        val scale = maxOf(pw / bw, ph / bh)
        val sw = bw * scale
        val sh = bh * scale
        val sx = px + (pw - sw) / 2f
        val sy = py + (ph - sh) / 2f
        val dstRect = android.graphics.RectF(sx, sy, sx + sw, sy + sh)

        canvas.drawBitmap(photoBitmap, null, dstRect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
        canvas.restore()
        canvas.restore()

        val effectiveBorderWidth = data.photoBorderWidth.coerceIn(0f, 5f)
        if (effectiveBorderWidth > 0f) {
            val borderPaint = Paint().apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = effectiveBorderWidth
                isAntiAlias = true
            }
            when (shape) {
                "Circle" -> canvas.drawCircle(px + pw / 2f, py + ph / 2f, minOf(pw, ph) / 2f, borderPaint)
                "Oval" -> canvas.drawOval(photoRect, borderPaint)
                "Rounded" -> canvas.drawRoundRect(photoRect, cornerRad, cornerRad, borderPaint)
                "Rectangle" -> canvas.drawRoundRect(photoRect, 3f, 3f, borderPaint)
                else -> canvas.drawRect(photoRect, borderPaint)
            }
        }
    }

"""
            content = content[:idx] + new_draw_photo + content[end_idx:]
            print("Successfully updated drawPhotoAt!")

    # Template replacements
    content = content.replace('drawPhotoAt(px, 24f, pSize, "Circle", AndroidColor.WHITE)', 'drawPhotoAt(px, 24f, pSize, data.photoShape, AndroidColor.WHITE)')
    content = content.replace('drawPhotoAt(px, currentY, pSize, "Rounded", primaryColor)', 'drawPhotoAt(px, currentY, pSize, data.photoShape, primaryColor)')
    content = content.replace('drawPhotoAt((pageWidth - pSize) / 2f, headY, pSize, "Circle", primaryColor)', 'drawPhotoAt((pageWidth - pSize) / 2f, headY, pSize, data.photoShape, primaryColor)')

    with open("app/src/main/java/com/example/ui/screens/tools/AtsCvBuilderTool.kt", "w", encoding="utf-8") as f:
        f.write(content)

if __name__ == "__main__":
    update_ats()
