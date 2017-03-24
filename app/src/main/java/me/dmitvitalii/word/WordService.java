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
package me.dmitvitalii.word;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import static com.yotadevices.sdk.EpdConstants.EpdLauncherConstants.OPTION_WIDGET_THEME;
import static com.yotadevices.sdk.EpdConstants.EpdLauncherConstants.WIDGET_THEME_BLACK;
import static com.yotadevices.sdk.EpdConstants.EpdLauncherConstants.WIDGET_THEME_WHITE;

/**
 * @author Vitalii Dmitriev
 * @since 24.03.2017
 */
public class WordService extends IntentService {

    public static final String NEXT_WORD = "change";
    @SuppressWarnings("unused")
    private static final String TAG = WordService.class.getSimpleName();
    private static final byte FIRST = 0;
    private String[] mWords;
    private ComponentName mWidgetComponentName;
    private String[] mExplanations;
    private AppWidgetManager mWidgetManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * {@link #TAG} is used to name the worker thread, important only for debugging.
     */
    public WordService() {
        super(TAG);
    }

    /**
     * Returns a color for active elements. Example: if the first button was pressed, it either
     * must be black or white, depending on which theme was chosen in YotaHub.
     * Uses statically imported constants from SDK.
     *
     * @return required color.
     */
    private int getActiveElementColor() {
        int id = mWidgetManager.getAppWidgetIds(mWidgetComponentName)[0];
        boolean isWhiteTheme = mWidgetManager.getAppWidgetOptions(id)
                .getInt(OPTION_WIDGET_THEME, WIDGET_THEME_BLACK) == WIDGET_THEME_WHITE;
        return isWhiteTheme ? Color.BLACK : Color.WHITE;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWidgetManager = AppWidgetManager.getInstance(this);
        Resources resources = getResources();
        mWords = resources.getStringArray(R.array.words);
        mExplanations = resources.getStringArray(R.array.meanings);
        mWidgetComponentName = new ComponentName(getPackageName(), EpdWidget.class.getName());
        Intent intent = new Intent(this, WordService.class).setAction(NEXT_WORD);
        PendingIntent pendingIntent =
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    @Override
    public void onHandleIntent(Intent intent) {
        String action = null == intent ? "" : intent.getAction();
        if (NEXT_WORD.equals(action)) {
            RemoteViews widget = new RemoteViews(getPackageName(), R.layout.epd_layout_fullscreen);
            int stringId = getId();
            if (mWords.length >= stringId) {
                stringId = FIRST;
            }
            saveNextId(stringId);
            widget.setTextViewText(R.id.text_word, mWords[stringId]);
            widget.setTextColor(R.id.text_word, getActiveElementColor());
            widget.setTextViewText(R.id.text_explanation, mExplanations[stringId]);
            widget.setTextColor(R.id.text_explanation, getActiveElementColor());
            updateWidget(widget);
        } else if (getId() == FIRST) {
            startService(new Intent(intent).setAction(NEXT_WORD));
        }
    }

    private void updateWidget(RemoteViews views) {
        mWidgetManager.updateAppWidget(mWidgetComponentName, views);
    }

    public int getId() {
        return getSharedPreferences(getPackageName() + TAG, MODE_PRIVATE).getInt(NEXT_WORD, FIRST);
    }

    private void saveNextId(int id) {
        getSharedPreferences(getPackageName() + TAG, MODE_PRIVATE)
                .edit()
                .putInt(NEXT_WORD, ++id)
                .apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
