package com.mnmyounus.yala

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * YALA never initializes any analytics, crash-reporting, or networking SDK here
 * (or anywhere else). The manifest carries no INTERNET permission, so even a
 * bug could not exfiltrate data off-device.
 */
@HiltAndroidApp
class YalaApplication : Application()
