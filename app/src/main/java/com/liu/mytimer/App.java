package com.liu.mytimer;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.liu.mytimer.module.DaoMaster;
import com.liu.mytimer.module.DaoSession;

import org.greenrobot.greendao.database.Database;


/**
 * Created by kunming.liu on 2017/9/20.
 */

public class App extends Application {
    private DaoSession daoSession;
    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,"my_timer.db");
        Database db = helper.getEncryptedWritableDb("12345");
        daoSession = new DaoMaster(db).newSession();
    }
    public DaoSession getDaoSession(){
        return daoSession;
    }
}
