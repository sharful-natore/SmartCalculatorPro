    // Sync pagerState -> viewModel.activeTab when user swipes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }
            .collect { inProgress ->
                if (!inProgress && pagerState.currentPage in 0..3) {
                    if (viewModel.activeTab != pagerState.currentPage) {
                        viewModel.activeTab = pagerState.currentPage
                    }
                }
            }
    }
