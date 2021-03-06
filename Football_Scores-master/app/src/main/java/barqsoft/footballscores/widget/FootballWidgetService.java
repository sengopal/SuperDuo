package barqsoft.footballscores.widget;


import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by sengopal on 9/30/15.
 */

public class FootballWidgetService extends IntentService implements LoaderManager.LoaderCallbacks<Cursor> {
    public static String LOG_TAG = FootballWidgetService.class.getSimpleName();
    static final String[] DB_COLUMNS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.LEAGUE_COL
    };

    public static final int COL_DATE = 0;
    public static final int COL_HOME = 1;
    public static final int COL_AWAY = 2;
    public static final int COL_HOME_GOALS = 3;
    public static final int COL_AWAY_GOALS = 4;
    public static final int MATCH_ID = 5;
    public static final int COL_LEAGUE = 6;

    public FootballWidgetService() {
        super("FootballWidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, FootballWidget.class));
        Uri dateUri = DatabaseContract.scores_table.buildScoreWithDate();
        Log.i(LOG_TAG, "dateUri: " + dateUri.toString());
        //TODO: move to common constants for "date"
        Cursor data = getContentResolver().query(dateUri, DB_COLUMNS, "date", new String[]{getToday()}, null);

        if (data == null) {
            return;
        }

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        String homeName = data.getString(COL_HOME);
        String awayName = data.getString(COL_AWAY);
        int homeScore = data.getInt(COL_HOME_GOALS);
        int awayScore = data.getInt(COL_AWAY_GOALS);
        int matchId = data.getInt(MATCH_ID);
        String league = data.getString(COL_LEAGUE);

        data.close();

        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.football_widget);
            Log.i("INFO", "HOME NAME " + homeName);
            Log.i("INFO", "AWAY NAME " + awayName);
            String scores = Utilies.getScores(homeScore, awayScore);
            Log.i("INFO", "SCORES " + scores);

            views.setTextViewText(R.id.home_name, homeName);
            views.setTextViewText(R.id.away_name, awayName);
            views.setTextViewText(R.id.score_textview, scores);

            // Led to the MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    static String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
