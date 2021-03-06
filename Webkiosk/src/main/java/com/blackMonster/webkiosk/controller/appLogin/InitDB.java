package com.blackMonster.webkiosk.controller.appLogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.blackMonster.webkiosk.controller.RefreshBroadcasts;
import com.blackMonster.webkiosk.controller.RefreshStatus;
import com.blackMonster.webkiosk.utils.M;
import com.blackMonster.webkiosk.SharedPrefs.MainPrefs;
import com.blackMonster.webkiosk.SharedPrefs.RefreshDBPrefs;
import com.blackMonster.webkiosk.controller.Timetable.TimetableCreateRefresh;
import com.blackMonster.webkiosk.crawler.CrawlerDelegate;
import com.blackMonster.webkiosk.crawler.LoginStatus;
import com.blackMonster.webkiosk.services.AutoRefreshAlarmService;
import com.blackMonster.webkiosk.ui.Dialog.RefreshDbErrorDialogStore;

/**
 * Initializes database on first login of app.
 */
public class InitDB {

    public static final String BROADCAST_DATEBASE_CREATION_RESULT = "BROADCAST_DATEBASE_CREATION_RESULT";
    private static final String TAG = "InitDB";

    String enroll, pass, batch, colg;
    Context context;

    CrawlerDelegate crawlerDelegate;


    public InitDB(String enroll, String pass, String batch, String colg, Context context) {
        this.enroll = enroll;
        this.pass = pass;
        this.batch = batch;
        this.colg = colg;
        this.context = context;
    }


    /*
     * TEMPLATE:
     *
     * RefreshDBPrefs.setStatus(..);
     * do work....
     * broadcastResult(..);
     * Error handling
     *
     * RefreshDBPrefs.setStatus(..);
     *      .
     *      .
     */

    public boolean start() {
        int result;

        try {
            Log.d(TAG, "startKeAndar: ");
            RefreshDBPrefs.setStatus(RefreshStatus.LOGGING_IN, context);
            crawlerDelegate = new CrawlerDelegate(context);
            result = crawlerDelegate.login(colg, enroll, pass);
            broadcastResult(RefreshBroadcasts.BROADCAST_LOGIN_RESULT, result);

            if (result != LoginStatus.LOGIN_DONE) return false;
            M.log(TAG, "login done");

            RefreshDBPrefs.setStatus(RefreshStatus.CREATING_DB, context);
            result = CreateDatabase.start(colg, enroll, batch, crawlerDelegate, context);
            broadcastResult(BROADCAST_DATEBASE_CREATION_RESULT, result);

            if (isCreateDatabaseSuccessful(result)) {
                saveFirstTimeloginPreference();
                return true;
            } else return false;

        } finally {
            RefreshDBPrefs.setStatus(RefreshStatus.STOPPED,
                    context);
        }

    }

    public CrawlerDelegate getCrawlerDelegate() {
        return crawlerDelegate;
    }

    private void saveFirstTimeloginPreference() {
        SharedPreferences settings = context.getSharedPreferences(
                MainPrefs.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(MainPrefs.ENROLL_NO, enroll);
        editor.putString(MainPrefs.PASSWORD, pass);
        editor.putString(MainPrefs.BATCH, batch);
        editor.putString(AutoRefreshAlarmService.PREF_AUTO_UPDATE_OVER, "anyNetwork");
        editor.putString(MainPrefs.COLG, colg);
        editor.commit();
    }

   //Broadcast result of every step of DB initialization, so that UI elements can act accordingly.
    private void broadcastResult(String type, int result) {
        RefreshDbErrorDialogStore.store(type, result, context);

        Intent intent = new Intent(type).putExtra(type, result);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //Returns if attendance and timetable database both are successfully created.
    private boolean isCreateDatabaseSuccessful(int result) {
        return !(result == CreateDatabase.ERROR || TimetableCreateRefresh.isError(result));
    }
}

