package com.htmessage.yichatopen.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.domain.User;
import com.htmessage.yichatopen.activity.chat.ChatActivity;
import com.htmessage.sdk.ChatType;
import com.htmessage.sdk.client.HTClient;
import com.htmessage.sdk.model.HTConversation;
import com.htmessage.sdk.model.HTMessage;
import com.htmessage.sdk.model.HTMessageTextBody;

/**
 * Created by huangfangyi on 2016/12/19.
 * qq 84543217
 */

public class MyNotification {

    private static MyNotification myNotification;


    private static NotificationManager manager = null;
    private Context mContext;
    private Notification.Builder nBuilder;
    private NotificationCompat.Builder builder;
    private boolean isOpenOutGoing = false;//默认不打开常驻  打开滑动删除
    private static Notification notification;

    public MyNotification(Context context) {

        this.mContext = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //为了版本兼容  选择V4包下的NotificationCompat进行构造
        builder = new NotificationCompat.Builder(mContext);
        nBuilder = new Notification.Builder(mContext);
        setOutGoingAndAutoCancle(isOpenOutGoing);// 默认不打开常驻
    }

    public static void init(Context context) {
        if (myNotification == null) {
            myNotification = new MyNotification(context);
        }

    }

    public static MyNotification getInstance() {
        if (myNotification == null) {
            throw new RuntimeException("please init first~!");

        }
        return myNotification;
    }

    /**
     * 设置常驻和滑动删除是否打开
     *
     * @param flag false 打开滑动删除,不设置常驻  true 设置常驻,不设置滑动删除
     */
    public void setOutGoingAndAutoCancle(@NonNull boolean flag) {
        if (builder != null) {
            builder.setAutoCancel(!flag);
            builder.setOngoing(flag);
        }
        if (nBuilder != null) {
            nBuilder.setAutoCancel(!flag);
            nBuilder.setOngoing(flag);
        }
    }

    public void onNewMessage(HTMessage htMessage) {
        String userId = htMessage.getUsername();
        String userNick = userId;
        Intent intent = new Intent();
        intent.setClass(mContext, ChatActivity.class);
        intent.putExtra("userId", userId);
        if (htMessage.getChatType() == ChatType.singleChat) {
            User user = ContactsManager.getInstance().getContactList().get(userId);
            if (user != null) {
                userNick = user.getNick();
            }
        }
        // 如果当前Activity启动在前台，则不开启新的Activity。
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, Integer.valueOf(userId), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder
                .setContentIntent(pendingIntent)
                .setContentTitle(userNick)
                //  .setTicker("发来一个新消息")
                .setContentText(getContent(htMessage))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(mContext.getApplicationInfo().icon);
        notification = builder.build();


        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        manager.notify(Integer.valueOf(userId), notification);//发送通知
    }

    public void cancel(int id) {
        manager.cancel(id);
    }

    protected final static String[] msgs = { "发来一张图片", "发来一段语音"};

    private String getContent(HTMessage message) {
        HTConversation htConversation = HTClient.getInstance().conversationManager().getConversation(message.getFrom());
        String notifyText = "";
        if (htConversation != null) {
            if (htConversation.getUnReadCount() > 0) {
                notifyText = mContext.getString(R.string.zhongkuohao) + htConversation.getUnReadCount() + mContext.getString(R.string.zhongkuohao_msg);

            }

        }

        switch (message.getType()) {
            case TEXT:
                HTMessageTextBody htMessageTextBody = (HTMessageTextBody) message.getBody();

                String content = htMessageTextBody.getContent();
                if (content != null) {
                    notifyText += content;
                } else {
                    notifyText += msgs[0];
                }

                break;
            case IMAGE:
                notifyText += msgs[0];
                break;
            case VOICE:
                notifyText += msgs[1];
                break;
        }
        return notifyText;
    }
}
