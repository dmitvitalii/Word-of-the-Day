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

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

/**
 * The main EPD widget.
 *
 * @author Vitalii Dmitriev
 * @since 04.04.2017
 */
class EpdWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, widgetManager: AppWidgetManager, widgetIds: IntArray) {
        context.startService(Intent(context, WordService::class.java))
        super.onUpdate(context, widgetManager, widgetIds)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.stopService(Intent(context, WordService::class.java))
    }

    override fun onReceive(context: Context, intent: Intent) {
        context.startService(Intent(intent).setClass(context, WordService::class.java))
        super.onReceive(context, intent)
    }
}
