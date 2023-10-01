package com.tantalum.onefinance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.tantalum.onefinance.MainActivity
import com.tantalum.onefinance.R

/**
 * Implementation of App Widget functionality.
 */
class OpenAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
    val views = RemoteViews(context.packageName, R.layout.open_app_widget)
    views.setOnClickPendingIntent(R.id.add_income, pendingIntent)
    views.setOnClickPendingIntent(R.id.add_expense, pendingIntent)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}