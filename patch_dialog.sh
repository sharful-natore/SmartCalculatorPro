cat << 'INNER_EOF' > /tmp/patch_dialog.diff
--- app/src/main/java/com/example/ui/screens/DashboardScreen.kt
+++ app/src/main/java/com/example/ui/screens/DashboardScreen.kt
@@ -838,8 +838,15 @@
         if (showAllFeaturedDialog) {
-            AlertDialog(
+            androidx.compose.ui.window.Dialog(
                 onDismissRequest = { showAllFeaturedDialog = false },
-                title = {
+                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
+            ) {
+                androidx.compose.material3.Surface(
+                    modifier = Modifier.fillMaxWidth(0.92f).padding(vertical = 24.dp),
+                    shape = RoundedCornerShape(24.dp),
+                    color = themeColors.cardBg
+                ) {
+                    Column(modifier = Modifier.padding(20.dp)) {
                     Text(
                         text = if (isBn) "ফিচার্ড ও ফেভারিট টুলস" else "Featured & Favorite Tools",
                         fontWeight = FontWeight.ExtraBold,
                         fontSize = 18.sp,
-                        color = themeColors.displayText
+                        color = themeColors.displayText,
+                        modifier = Modifier.padding(bottom = 12.dp)
                     )
-                },
-                text = {
-                    Column(
-                        modifier = Modifier.fillMaxWidth()
-                    ) {
                         Text(
                             text = if (isBn) 
@@ -866,3 +873,3 @@
                                 .fillMaxWidth()
-                                .heightIn(max = 380.dp)
+                                .weight(1f, fill = false)
                                 .verticalScroll(rememberScrollState())
@@ -951,12 +958,11 @@
-                },
-                confirmButton = {
+                        Spacer(modifier = Modifier.height(16.dp))
+                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                     TextButton(onClick = { showAllFeaturedDialog = false }) {
                         Text(
                             text = if (isBn) "বন্ধ করুন" else "Close",
                             fontWeight = FontWeight.Bold,
                             color = themeColors.buttonEqualBg
                         )
                     }
-                },
-                containerColor = themeColors.cardBg
-            )
+                        }
+                    }
+                }
+            }
         }
INNER_EOF
patch -p0 < /tmp/patch_dialog.diff
