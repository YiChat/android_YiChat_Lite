package com.htmessage.yichatopen;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.htmessage.yichatopen.manager.MyNotification;
import com.htmessage.yichatopen.manager.LocalUserManager;
import com.htmessage.yichatopen.manager.Manager;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class HTApp extends Application {
    private static Context applicationContext;
    private static HTApp instance;
    private JSONObject userJson = null;
     private List<Activity> activities = new ArrayList<>();
    public static boolean isCalling = false;

    @Override
    public void onCreate() {
        //sdk采用双进程守护,因此不要在守护进程中初始Application
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid, this);
        if (processAppName == null || !processAppName.equalsIgnoreCase(this.getPackageName())) {
            return;
        }
        super.onCreate();
        applicationContext = this;
        instance = this;
        MyNotification.init(applicationContext);
        LocalUserManager.init(applicationContext);
         if (getUserJson() != null) {
            Manager.initManagerList(applicationContext);
        }
        //异常上报
        CrashReport.initCrashReport(applicationContext, "a7b4566fd9", false);
        HTClientHelper.init(applicationContext);
    }

    public static Context getContext() {
        return applicationContext;
    }




    public static HTApp getInstance() {
        return instance;
    }

    public String getUsername() {
        String username = null;
        if (getUserJson() != null) {
            username = getUserJson().getString(HTConstant.JSON_KEY_HXID);
        }
        return username;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void setUserJson(JSONObject userJson) {
        this.userJson = userJson;
        if (userJson != null) {
            Manager.initManagerList(applicationContext);
        }
        LocalUserManager.getInstance().setUserJson(userJson);
    }

    public JSONObject getUserJson() {
        if (userJson == null) {
            userJson = LocalUserManager.getInstance().getUserJson();
        }
        return userJson;
    }






    public void saveActivity(Activity activity) {
        if (activity != null) {
            activities.add(activity);
        }

    }

    public void finishActivities() {
        for (Activity activity : activities) {
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }

    }



    /**
     * check the application process name if process name is not qualified, then we think it is a service process and we will not init SDK
     *
     * @param pID
     * @return
     */
    private static String getAppName(int pID, Context appContext) {
        String processName = null;
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = appContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
            }
        }
        return processName;
    }

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg
     * @return
     */
    public Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog
        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
      
        return loadingDialog;
    }

    public String getDirFilePath() {
        File file = new File(HTConstant.DIR_AVATAR);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.mkdir();
        }
        return file.getAbsolutePath() + File.separator;
    }



}
