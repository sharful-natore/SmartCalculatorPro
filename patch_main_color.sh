cat << 'INNER_EOF' > /tmp/main_color.diff
--- app/src/main/java/com/example/ui/MainApp.kt
+++ app/src/main/java/com/example/ui/MainApp.kt
@@ -3517,3 +3517,3 @@
             onColorSelected = { color ->
-                val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())
+                val hex = String.format("#%08X", color.toArgb())
                 when (target) {
@@ -3579,3 +3579,3 @@
             onColorSelected = { color ->
-                val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())
+                val hex = String.format("#%08X", color.toArgb())
                 if (isGrad) {
INNER_EOF
patch -p0 < /tmp/main_color.diff
