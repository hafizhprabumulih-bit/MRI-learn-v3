package com.example.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.example.viewmodel.MriViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Menyimpan dan memulihkan posisi gulir (scroll offset) per [screenKey] di [MriViewModel]
 * sehingga menekan tombol Kembali (Back) selalu mengembalikan layar ke posisi terakhir.
 */
@Composable
fun rememberRestorableScrollState(
    screenKey: String,
    viewModel: MriViewModel
): ScrollState {
    val savedInitial = viewModel.getSavedScrollOffset(screenKey)
    val scrollState = remember(screenKey) {
        ScrollState(initial = savedInitial)
    }

    LaunchedEffect(screenKey) {
        val target = viewModel.getSavedScrollOffset(screenKey)
        if (target > 0) {
            // Tunggu satu frame agar konten selesai diukur sebelum scrollTo
            delay(16)
            if (scrollState.value != target && scrollState.maxValue > 0) {
                scrollState.scrollTo(target.coerceAtMost(scrollState.maxValue))
            }
        }
    }

    LaunchedEffect(scrollState, screenKey) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .collect { offset ->
                viewModel.updateScrollOffset(screenKey, offset)
            }
    }

    return scrollState
}
