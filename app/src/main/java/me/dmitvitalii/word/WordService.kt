/*
 * Copyright (c) 2017 Vitalii Dmitriev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.dmitvitalii.word

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.os.IBinder
import android.os.SystemClock
import android.widget.RemoteViews

import com.yotadevices.sdk.EpdConstants.EpdLauncherConstants.OPTION_WIDGET_THEME
import com.yotadevices.sdk.EpdConstants.EpdLauncherConstants.WIDGET_THEME_BLACK
import com.yotadevices.sdk.EpdConstants.EpdLauncherConstants.WIDGET_THEME_WHITE

/**
 * Creates an IntentService.  Invoked by your subclass's constructor.
 * [TAG] is used to name the worker thread, important only for debugging.
 *
 * @author Vitalii Dmitriev
 * @since 04.04.2017
 */
class WordService : IntentService(WordService.TAG) {
    private var mWords: Array<String> = resources.getStringArray(R.array.words)
    private var mWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(this)

    private var mWidgetComponentName: ComponentName? = null
    private var mExplanations: Array<String>? = null

    /**
     * Returns a color for active elements. Example: if the first button was pressed, it either
     * must be black or white, depending on which theme was chosen in YotaHub.
     * Uses statically imported constants from SDK.

     * @return required color.
     */
    private val activeElementColor: Int
        get() {
            val id = mWidgetManager.getAppWidgetIds(mWidgetComponentName)[0]
            val isWhiteTheme = mWidgetManager.getAppWidgetOptions(id)
                    .getInt(OPTION_WIDGET_THEME, WIDGET_THEME_BLACK) == WIDGET_THEME_WHITE
            return if (isWhiteTheme) Color.BLACK else Color.WHITE
        }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mExplanations = resources.getStringArray(R.array.meanings)
        mWidgetComponentName = ComponentName(packageName, EpdWidget::class.simpleName)
        val intent = Intent(this, WordService::class.java)
        intent.action = NEXT_WORD
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        )
    }

    public override fun onHandleIntent(intent: Intent?) {
        val action = when (intent) {
            null -> ""
            else -> intent.action
        }
        if (NEXT_WORD == action) {
            val widget = RemoteViews(packageName, R.layout.epd_layout_fullscreen)
            var stringId = id
            if (mWords.size >= stringId) {
                stringId = FIRST.toInt()
            }
            saveNextId(stringId)
            widget.setTextViewText(R.id.text_word, mWords[stringId])
            widget.setTextColor(R.id.text_word, activeElementColor)
            widget.setTextViewText(R.id.text_explanation, mExplanations!![stringId])
            widget.setTextColor(R.id.text_explanation, activeElementColor)
            updateWidget(widget)
        } else if (id == FIRST.toInt()) {
            startService(Intent(intent).setAction(NEXT_WORD))
        }
    }

    private fun updateWidget(views: RemoteViews) {
        mWidgetManager.updateAppWidget(mWidgetComponentName, views)
    }

    val id: Int
        get() = getSharedPreferences(packageName + TAG, Context.MODE_PRIVATE).getInt(NEXT_WORD, FIRST.toInt())

    private fun saveNextId(id: Int) {
        getSharedPreferences(packageName + TAG, Context.MODE_PRIVATE)
                .edit()
                .putInt(NEXT_WORD, id + 1)
                .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        val NEXT_WORD = "change"
        private val TAG = WordService::class.simpleName
        private val FIRST: Byte = 0
    }
}
