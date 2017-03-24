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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * @author Vitalii Dmitriev
 * @since 15.03.2017
 */
public class EpdWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, WordService.class));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, WordService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        intent = new Intent(intent).setClass(context, WordService.class);
        context.startService(intent);
        super.onReceive(context, intent);
    }

}
