sed -i 's/var value by remember { mutableStateOf(initialHsv\[2\]) }/var alpha by remember { mutableStateOf(initialColor.alpha) }/' app/src/main/java/com/example/ui/components/ColorWheelPickerDialog.kt
sed -i 's/android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))/android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, 1f))/' app/src/main/java/com/example/ui/components/ColorWheelPickerDialog.kt
sed -i 's/val currentColor = remember(hue, saturation, value)/val currentColor = remember(hue, saturation, alpha)/' app/src/main/java/com/example/ui/components/ColorWheelPickerDialog.kt
sed -i 's/String.format(Locale.US, "#%02X%02X%02X", r, g, b)/String.format(Locale.US, "#%02X%02X%02X%02X", android.graphics.Color.alpha(argb), r, g, b)/' app/src/main/java/com/example/ui/components/ColorWheelPickerDialog.kt
sed -i 's/"উজ্জ্বলতা (Brightness)" else "Brightness"/"স্বচ্ছতা (Transparency)" else "Transparency"/' app/src/main/java/com/example/ui/components/ColorWheelPickerDialog.kt
sed -i 's/value = value/value = alpha/' app/src/main/java/com/example/ui/components/ColorWheelPickerDialog.kt
sed -i 's/onValueChange = { value = it }/onValueChange = { alpha = it }/' app/src/main/java/com/example/ui/components/ColorWheelPickerDialog.kt
sed -i 's/value = hsv\[2\]/alpha = 1f/' app/src/main/java/com/example/ui/components/ColorWheelPickerDialog.kt
