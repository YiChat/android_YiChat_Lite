package com.htmessage.yichatopen.manager;

import android.content.Context;

/**
 * Created by huangfangyi on 2016/12/8.
 * qq 84543217
 */

public class Manager {

    public static void initManagerList(Context context){
        NotifierManager.init(context);
        ContactsManager.init(context);
        SettingsManager.init(context);
    }
}
