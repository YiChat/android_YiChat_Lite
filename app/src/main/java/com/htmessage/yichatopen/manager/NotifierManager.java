package com.htmessage.yichatopen.manager;

import android.content.Context;

/**
 * Created by huangfangyi on 2016/10/12.
 * qq 84543217
 */

public class NotifierManager {

    private static NotifierManager notifierManager = null;
    /**
     * application context
     */
    private Context appContext;

    /**
     * get notifierManager of EaseUI
     *
     * @return
     */
    public static NotifierManager getInstance() {
        if (notifierManager == null) {
            throw new RuntimeException("NotifierManager please init first!");
        }
        return notifierManager;
    }


    public static synchronized void init(Context context) {
        if (notifierManager == null) {

            notifierManager = new NotifierManager(context);
        }

    }

    public NotifierManager(Context context) {

        appContext = context;
        initNotifier();
    }

    /**
     * the notifier
     */
    private Notifier notifier = null;

    private void initNotifier() {
        notifier = createNotifier();
        notifier.init(appContext);
    }

    protected Notifier createNotifier() {
        return new Notifier();
    }

    public Notifier getNotifier() {
        return notifier;
    }



}
