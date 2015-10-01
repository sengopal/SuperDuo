package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

/**
 * Created by sengopal on 10/1/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FootballCollWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = FootballCollWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                Uri dateUri = DatabaseContract.scores_table.buildScoreWithDate();
                data = getContentResolver().query(dateUri, FootballWidgetService.DB_COLUMNS, "date", new String[]{FootballWidgetService.getToday()}, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_collection_item);

                String homeName = data.getString(FootballWidgetService.COL_HOME);
                String awayName = data.getString(FootballWidgetService.COL_AWAY);
                int homeGoal = data.getInt(FootballWidgetService.COL_HOME_GOALS);
                int awayGoal = data.getInt(FootballWidgetService.COL_AWAY_GOALS);

                views.setTextViewText(R.id.home_name, homeName);
                views.setTextViewText(R.id.away_name, awayName);

                String homeScore = "";
                String awayScore = "";
                if (homeGoal != -1) {
                    homeScore += homeGoal;
                    awayScore += awayGoal;
                } else {
                    homeScore = "-";
                    awayScore = "-";
                }

                views.setTextViewText(R.id.home_score, homeScore);
                views.setTextViewText(R.id.away_score, awayScore);

                final Intent fillInIntent = new Intent();

                Uri scoreUri = DatabaseContract.scores_table.buildScoreWithDate();

                fillInIntent.setData(scoreUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                // views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_collection_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(1);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
