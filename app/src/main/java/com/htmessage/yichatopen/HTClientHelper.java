package com.htmessage.yichatopen;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.htmessage.sdk.client.HTOptions;
import com.htmessage.sdk.manager.HTChatManager;
import com.htmessage.yichatopen.activity.main.MainActivity;
import com.htmessage.yichatopen.activity.chat.ChatActivity;

import com.htmessage.yichatopen.manager.MyNotification;
import com.htmessage.yichatopen.domain.InviteMessage;
import com.htmessage.yichatopen.domain.InviteMessgeDao;
import com.htmessage.yichatopen.domain.User;
import com.htmessage.yichatopen.manager.ContactsManager;
import com.htmessage.yichatopen.manager.NotifierManager;
import com.htmessage.sdk.ChatType;
import com.htmessage.sdk.client.HTClient;
import com.htmessage.sdk.listener.HTConnectionListener;
import com.htmessage.sdk.model.CallMessage;
import com.htmessage.sdk.model.CmdMessage;
import com.htmessage.sdk.model.HTMessage;
import com.htmessage.yichatopen.utils.CommonUtils;
import com.htmessage.yichatopen.utils.HTMessageUtils;

import java.util.List;

/**
 * Created by huangfangyi on 2017/3/3.
 * qq 84543217
 */

public class HTClientHelper {
    private NotifierManager notifierManager;
    private static Context applicationContext;

    private static HTClientHelper htClientHelper;

    public static void init(Context context) {
        htClientHelper = new HTClientHelper(context);
    }

    public HTClientHelper(Context context) {
        this.applicationContext = context;
        HTOptions options=new HTOptions(); //IM相关配置
        options.setHost(HTConstant.HOST_IM); //IP地址
        options.setOssInfo(HTConstant.endpoint,HTConstant.bucket,HTConstant.accessKeyId,HTConstant.accessKeySecret);//阿里云OSS相关
        options.setSinglePointUrl(HTConstant.DEVICE_URL_UPDATE,HTConstant.DEVICE_URL_GET);//设置单APP端登录
        options.setDebug(false); //是否打印log false不打印
        options.setKeepAlive(false); //是否开启保活  false 不开启
        HTClient.init(applicationContext,options);
        HTClient.getInstance().setMessageLisenter(messageLisenter);
        HTClient.getInstance().addConnectionListener(htConnectionListener);
        notifierManager = new NotifierManager(applicationContext);
    }

    public static HTClientHelper getInstance() {

        if (htClientHelper == null) {
            throw new RuntimeException("please init first!");
        }
        return htClientHelper;
    }

    private HTConnectionListener htConnectionListener = new HTConnectionListener() {
        @Override
        public void onConnected() {

            //   Toast.makeText(applicationContext,"连上啦",Toast.LENGTH_SHORT).show();
            notifyConnection(true);
        }

        @Override
        public void onDisconnected() {
            notifyConnection(false);
            // Toast.makeText(applicationContext,"断连啦",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConflict() {
            // Toast.makeText(applicationContext,"被踢啦",Toast.LENGTH_SHORT).show();
            notifyConflict();
        }
    };
    private HTClient.MessageLisenter messageLisenter = new HTClient.MessageLisenter() {
        @Override
        public void onHTMessage(HTMessage htMessage) {
            handleHTMessage(htMessage);
        }

        @Override
        public void onCMDMessgae(CmdMessage cmdMessage) {
            handleCmdMessage(cmdMessage);
        }

        @Override
        public void onCallMessgae(CallMessage callMessage) {
             //lite版没有音视频消息
        }
    };
    private void handleHTMessage(HTMessage htMessage){
        Intent intent=new Intent(IMAction.ACTION_NEW_MESSAGE);
        intent.putExtra("message",htMessage);
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);
        if (ChatActivity.activityInstance != null && htMessage.getUsername().equals(ChatActivity.activityInstance.getToChatUsername())) {
        } else {
            MyNotification.getInstance().onNewMessage(htMessage);
        }
        notifyHTMessage();
    }


    private void handleCmdMessage(CmdMessage cmdMessage) {
        InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(applicationContext);
        String data = cmdMessage.getBody();
        if (data != null) {
            JSONObject dataJSON = JSONObject.parseObject(data);
            if (dataJSON != null && dataJSON.containsKey("action")) {

                int action = dataJSON.getInteger("action");
                if (action == 1000) {
                    //收到好友申请的请求
                    List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
                    for (InviteMessage inviteMessage : msgs) {
                        if ( inviteMessage.getFrom().equals(cmdMessage.getFrom())) {
                            inviteMessgeDao.deleteMessage(cmdMessage.getFrom());
                        }
                    }
                    InviteMessage msg = new InviteMessage();
                    msg.setFrom(cmdMessage.getFrom());
                    msg.setTime(System.currentTimeMillis());
                    msg.setReason(dataJSON.getJSONObject("data").toJSONString());
                    // set invitation status
                    msg.setStatus(InviteMessage.Status.BEINVITEED);
                    notifyNewInviteMessage(msg,null);
                } else if (action == 1001) {
                    //收到好友同意的透传消息
                    List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
                    for (InviteMessage inviteMessage : msgs) {
                        if (inviteMessage.getFrom().equals(cmdMessage.getFrom())) {
                            inviteMessgeDao.deleteMessage(cmdMessage.getFrom());

                        }
                    }
                    // save invitation as message
                    InviteMessage msg = new InviteMessage();
                    msg.setFrom(cmdMessage.getFrom());
                    msg.setReason(dataJSON.getJSONObject("data").toJSONString());
                    msg.setTime(System.currentTimeMillis());
                    //   Log.d(TAG, message.getFrom() + "accept your request");
                    msg.setStatus(InviteMessage.Status.BEAGREED);
                    notifyNewInviteMessage(msg,dataJSON.getJSONObject("data"));
                } else if (action == 1002) {
                    //收到好友拒绝的透传消息
                    //Lite版没有拒绝好友申请的处理
                } else if (action == 1003) {
                    //收到删除好友的透传消息
                    //发送广播
                    if (HTApp.getInstance().getUsername().equals(cmdMessage.getTo())){
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(new Intent(IMAction.CMD_DELETE_FRIEND).putExtra(HTConstant.JSON_KEY_HXID, cmdMessage.getFrom()));
                    }
                } else if (action == 6000) {//收到撤回消息的透传
                    String msgId = dataJSON.getString("msgId");
                    String chatTo = cmdMessage.getTo();
                    if (cmdMessage.getChatType() == ChatType.singleChat) {
                        chatTo = cmdMessage.getFrom();
                    }
                    HTMessage htMessage = HTClient.getInstance().messageManager().getMssage(chatTo, msgId);
                    HTMessageUtils.creatWithDrowMsg(htMessage);
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(new Intent(IMAction.ACTION_MESSAGE_WITHDROW).putExtra("msgId", msgId));
                }
            }
        }
    }




    /**
     * user has logged into another device
     */
    protected void notifyConflict() {
        Intent intent = new Intent(applicationContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IMAction.ACTION_CONFLICT, true);
        applicationContext.startActivity(intent);
    }

    /**
     * user has logged into another device
     */
    protected void notifyConnection(boolean isConnected) {
        Intent intent = new Intent(IMAction.ACTION_CONNECTION_CHANAGED);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("state", isConnected);
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);
    }

    /**
     * save and notify invitation message
     *
     * @param msg
     */
    private void notifyNewInviteMessage(final InviteMessage msg, final JSONObject jsonObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(applicationContext);
                inviteMessgeDao.saveMessage(msg);
                inviteMessgeDao.saveUnreadMessageCount(1);
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(new Intent(IMAction.ACTION_INVITE_MESSAGE));
                notifierManager.getNotifier().vibrateAndPlayTone(null);

                if(jsonObject!=null){
                     User user = CommonUtils.Json2User(jsonObject);
                    ContactsManager.getInstance().saveContact(user);
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(new Intent(IMAction.ACTION_CONTACT_CHANAGED));
                }
            }
        }).start();
    }

    /**
     * save and notify invitation message
     */
    private void notifyHTMessage() {
        notifierManager.getNotifier().vibrateAndPlayTone(null);
    }
}
