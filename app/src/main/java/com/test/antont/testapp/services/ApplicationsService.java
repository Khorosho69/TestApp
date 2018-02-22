package com.test.antont.testapp.services;


import android.app.IntentService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.test.antont.testapp.activities.ListActivity;
import com.test.antont.testapp.databases.DBHelper;
import com.test.antont.testapp.enums.ActionType;
import com.test.antont.testapp.models.AppInfo;
import com.test.antont.testapp.models.AppStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplicationsService extends IntentService {

    private DBHelper mDBHelper;
    private SQLiteDatabase mDatabase;

    public ApplicationsService() {
        super("appService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mDBHelper = new DBHelper(this);
        mDatabase = mDBHelper.getWritableDatabase();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<AppInfo> mAppItems = getAppInfoList();

        Intent localIntent = new Intent(ActionType.ON_ALL_ITEMS_RETURNED.name());

        Bundle bundle = new Bundle();
        bundle.putSerializable(ListActivity.EXTRAS_SERIALIZED_APP_LIST, (Serializable) mAppItems);
        localIntent.putExtras(bundle);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private List<AppInfo> getAppInfoList() {
        List<AppInfo> actualItemsList = getActualPackagesNames();
        mDBHelper.writeAppInfoList(mDatabase, actualItemsList);

        List<AppStatus> appStatusList = mDBHelper.readAppInfoList(mDatabase);

        return actualItemsList;
    }

    private List<AppInfo> getActualPackagesNames() {
        List<AppInfo> appInfoList = new ArrayList<>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for(PackageInfo packageInfo: packs){
            if (!isSystemPackage(packageInfo)) {
                String appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                Drawable icon = packageInfo.applicationInfo.loadIcon(getPackageManager());
                appInfoList.add(new AppInfo(icon, packageInfo.packageName, appName, true));
            }
        }
        return appInfoList;
    }

    private boolean isSystemPackage(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
