/************************************************************
 * * Hyphenate CONFIDENTIAL
 * __________________
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * NOTICE: All information contained herein is, and remains
 * the property of Hyphenate Inc.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Hyphenate Inc.
 */
package com.htmessage.yichatopen.manager;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;


import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * new message notifier class
 * <p>
 * this class is subject to be inherited and implement the relative APIs
 */
public class Notifier {
    private final static String TAG = "notify";
    Ringtone ringtone = null;

    protected final static String[] msg_eng = {"sent a message", "sent a picture", "sent a voice",
            "sent location message", "sent a video", "sent a file", "%1 contacts sent %2 messages"
    };
    protected final static String[] msg_ch = {"发来一条消息", "发来一张图片", "发来一段语音", "发来位置信息", "发来一个视频", "发来一个文件",
            "%1个联系人发来%2条消息"
    };

    protected static int notifyID = 0525; // start notification id
    protected static int foregroundNotifyID = 0555;

    protected NotificationManager notificationManager = null;

    protected HashSet<String> fromUsers = new HashSet<String>();
    protected int notificationNum = 0;

    protected Context appContext;
    protected String packageName;
    protected String[] msgs;
    protected long lastNotifiyTime;
    protected AudioManager audioManager;
    protected Vibrator vibrator;

    public Notifier() {
    }

    /**
     * this function can be override
     *
     * @param context
     * @return
     */
    public Notifier init(Context context) {
        appContext = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        packageName = appContext.getApplicationInfo().packageName;
        if (Locale.getDefault().getLanguage().equals("zh")) {
            msgs = msg_ch;
        } else {
            msgs = msg_eng;
        }

        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);

        return this;
    }

    /**
     * this function can be override
     */
    public void reset() {
        resetNotificationCount();
        cancelNotificaton();
    }

    void resetNotificationCount() {
        notificationNum = 0;
        fromUsers.clear();
    }

    void cancelNotificaton() {
        if (notificationManager != null)
            notificationManager.cancel(notifyID);
    }


    public static boolean isAppRunningForeground(Context context) {
        ActivityManager var1 = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List var2 = var1.getRunningTasks(1);
        return context.getPackageName().equalsIgnoreCase(((ActivityManager.RunningTaskInfo) var2.get(0)).baseActivity.getPackageName());
    }


//
//
//    /**
//     * vibrate and  play tone
//     */
//    public void vibrateAndPlayTone( String string) {
//
//
//        if (System.currentTimeMillis() - lastNotifiyTime < 1000) {
//            // received new messages within 2 seconds, skip play ringtone
//            return;
//        }
//
//        try {
//            lastNotifiyTime = System.currentTimeMillis();
//
//            // check if in silent mode
//            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
//                 return;
//            }
//
////                long[] pattern = new long[] { 0, 180, 80, 120 };
////                vibrator.vibrate(pattern, -1);
//            if (ringtone == null) {
//                Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//                ringtone = RingtoneManager.getRingtone(appContext, notificationUri);
//                if (ringtone == null) {
//                     return;
//                }
//            }
//
//            if (!ringtone.isPlaying()) {
//                String vendor = Build.MANUFACTURER;
//
//                ringtone.play();
//                // for samsung S3, we meet a bug that the phone will
//                // continue ringtone without stop
//                // so add below special handler to stop it after 3s if
//                // needed
//                if (vendor != null && vendor.toLowerCase().contains("samsung")) {
//                    Thread ctlThread = new Thread() {
//                        public void run() {
//                            try {
//                                Thread.sleep(3000);
//                                if (ringtone.isPlaying()) {
//                                    ringtone.stop();
//                                }
//                            } catch (Exception e) {
//                            }
//                        }
//                    };
//                    ctlThread.run();
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * vibrate and  play tone
     */
    public void vibrateAndPlayTone(String string) {

        if (System.currentTimeMillis() - lastNotifiyTime < 1000) { //时间间隔小于1秒钟的返回
            // received new messages within 2 seconds, skip play ringtone
            return;
        }
        lastNotifiyTime=System.currentTimeMillis();

         playSoundAndVibrator();

    }

    /**
     * 播放声音
     */
    private void playSound() {
        try {
            lastNotifiyTime = System.currentTimeMillis();
            // check if in silent mode
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                return;
            }
            if (ringtone == null) {
                Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                ringtone = RingtoneManager.getRingtone(appContext, notificationUri);
                if (ringtone == null) {
                    return;
                }
            }
            if (!ringtone.isPlaying()) {
                String vendor = Build.MANUFACTURER;
                ringtone.play();
                // for samsung S3, we meet a bug that the phone will
                // continue ringtone without stop
                // so add below special handler to stop it after 3s if
                // needed
                if (vendor != null && vendor.toLowerCase().contains("samsung")) {
                    Thread ctlThread = new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(3000);
                                if (ringtone.isPlaying()) {
                                    ringtone.stop();
                                }
                            } catch (Exception e) {
                            }
                        }
                    };
                    ctlThread.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 声音和震动
     */
    private void playSoundAndVibrator() {
        try {
            lastNotifiyTime = System.currentTimeMillis();
            // check if in silent mode
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                return;
            }
            if (vibrator == null) {
                vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
            }
            if (ringtone == null) {
                Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                ringtone = RingtoneManager.getRingtone(appContext, notificationUri);
                if (ringtone == null) {
                    return;
                }
            }
            /**
             * 四个参数就是——停止 开启 停止 开启
             * -1不重复，非-1为从pattern的指定下标开始重复
             */
            long[] pattern = new long[]{0, 180, 80, 120};
            vibrator.vibrate(pattern, -1);
            /**
             * 播放声音
             */
            if (!ringtone.isPlaying()) {
                String vendor = Build.MANUFACTURER;
                ringtone.play();
                // for samsung S3, we meet a bug that the phone will
                // continue ringtone without stop
                // so add below special handler to stop it after 3s if
                // needed
                if (vendor != null && vendor.toLowerCase().contains("samsung")) {
                    Thread ctlThread = new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(3000);
                                if (ringtone.isPlaying()) {
                                    ringtone.stop();
                                }
                            } catch (Exception e) {
                            }
                        }
                    };
                    ctlThread.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 震动
     */
    private void playVibrator() {
        lastNotifiyTime = System.currentTimeMillis();
        if (vibrator == null) {
            vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        }
        /**
         * 四个参数就是——停止 开启 停止 开启
         * -1不重复，非-1为从pattern的指定下标开始重复
         */
        long[] pattern = new long[]{0, 180, 80, 120};
        vibrator.vibrate(pattern, -1);
    }

}
