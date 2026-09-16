package com.mnmyounus.yala.util

object Constants {
    const val PIN_LENGTH = 4
    const val PASSWORD_LENGTH = 8
    const val PATTERN_GRID_SIZE = 3          // 3x3 = 9 nodes
    const val PATTERN_MIN_NODES = 4
    const val IMAGE_POOL_SIZE = 5            // user uploads exactly 5 images
    val IMAGE_SEQUENCE_LENGTH_OPTIONS = listOf(3, 4)
    const val RECOVERY_KEY_LENGTH = 12
    const val MAX_FAILED_ATTEMPTS_BEFORE_RECOVERY_HINT = 3
}
