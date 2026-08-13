cat << 'INNER_EOF' > /tmp/pager_patch.diff
--- app/src/main/java/com/example/ui/MainApp.kt
+++ app/src/main/java/com/example/ui/MainApp.kt
@@ -241,27 +241,18 @@
 
     // Pager state for smooth horizontal tab swiping (excluding Theme page at index 4)
     val pagerState = rememberPagerState(initialPage = if (viewModel.activeTab in 0..3) viewModel.activeTab else 0) { 4 }
-    var isProgrammaticScroll by remember { mutableStateOf(false) }
 
     // Sync pagerState -> viewModel.activeTab when user swipes
-    LaunchedEffect(pagerState) {
-        snapshotFlow { pagerState.settledPage to pagerState.isScrollInProgress }.collect { (page, isScrolling) ->
-            if (!isProgrammaticScroll && !isScrolling && viewModel.activeTab != page && viewModel.activeTab in 0..3 && page in 0..3) {
-                viewModel.activeTab = page
-            }
+    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
+        if (!pagerState.isScrollInProgress && viewModel.activeTab != pagerState.currentPage && pagerState.currentPage in 0..3) {
+            viewModel.activeTab = pagerState.currentPage
         }
     }
 
     // Sync viewModel.activeTab -> pagerState when tab is changed via bottom nav or buttons
     LaunchedEffect(viewModel.activeTab) {
         if (viewModel.activeTab in 0..3 && pagerState.currentPage != viewModel.activeTab) {
-            isProgrammaticScroll = true
-            try {
-                pagerState.animateScrollToPage(
-                    page = viewModel.activeTab,
-                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
-                )
-            } finally {
-                delay(50) // small settle delay to clear pending measurements
-                isProgrammaticScroll = false
-            }
+            pagerState.animateScrollToPage(
+                page = viewModel.activeTab,
+                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
+            )
         }
     }
INNER_EOF
patch -p0 < /tmp/pager_patch.diff
