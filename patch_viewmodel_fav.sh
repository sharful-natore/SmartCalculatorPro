cat << 'INNER_EOF' > /tmp/viewmodel_fav.diff
--- app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt
+++ app/src/main/java/com/example/ui/viewmodel/CalculatorViewModel.kt
@@ -1589,6 +1589,9 @@
     }
 
+    var pendingUnfavoriteTool by mutableStateOf<String?>(null)
+    var pendingUnfavoriteConverter by mutableStateOf<String?>(null)
+
     fun toggleFavoriteTool(toolName: String) {
         val currentList = orderedFavoriteTools.toMutableList()
         if (currentList.contains(toolName)) {
-            currentList.remove(toolName)
+            pendingUnfavoriteTool = toolName
         } else {
             currentList.add(toolName)
             saveOrderedFavorites(currentList)
         }
-        saveOrderedFavorites(currentList)
+    }
+    
+    fun confirmUnfavoriteTool() {
+        pendingUnfavoriteTool?.let { toolName ->
+            val currentList = orderedFavoriteTools.toMutableList()
+            currentList.remove(toolName)
+            saveOrderedFavorites(currentList)
+        }
+        pendingUnfavoriteTool = null
     }
 
     fun toggleFavoriteConverter(converterName: String) {
         favoriteConverters = if (favoriteConverters.contains(converterName)) {
-            favoriteConverters - converterName
+            pendingUnfavoriteConverter = converterName
+            favoriteConverters
         } else {
             favoriteConverters + converterName
         }
         sharedPrefs.edit().putStringSet("favorite_converters", favoriteConverters).apply()
     }
+    
+    fun confirmUnfavoriteConverter() {
+        pendingUnfavoriteConverter?.let { converterName ->
+            favoriteConverters = favoriteConverters - converterName
+            sharedPrefs.edit().putStringSet("favorite_converters", favoriteConverters).apply()
+        }
+        pendingUnfavoriteConverter = null
+    }
INNER_EOF
patch -p0 < /tmp/viewmodel_fav.diff
