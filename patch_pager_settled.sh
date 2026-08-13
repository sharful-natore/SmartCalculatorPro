cat << 'INNER_EOF' > /tmp/pager_settled.diff
--- app/src/main/java/com/example/ui/MainApp.kt
+++ app/src/main/java/com/example/ui/MainApp.kt
@@ -244,5 +244,5 @@
     // Sync pagerState -> viewModel.activeTab when user swipes
-    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
-        if (!pagerState.isScrollInProgress && viewModel.activeTab != pagerState.currentPage && pagerState.currentPage in 0..3) {
-            viewModel.activeTab = pagerState.currentPage
+    LaunchedEffect(pagerState.settledPage) {
+        if (viewModel.activeTab != pagerState.settledPage && pagerState.settledPage in 0..3) {
+            viewModel.activeTab = pagerState.settledPage
         }
INNER_EOF
patch -p0 < /tmp/pager_settled.diff
