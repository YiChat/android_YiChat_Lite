package com.htmessage.yichatopen.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.htmessage.sdk.ChatType;
import com.htmessage.sdk.client.HTClient;
import com.htmessage.sdk.model.HTMessage;
import com.htmessage.sdk.model.HTMessageImageBody;
import com.htmessage.sdk.model.HTMessageTextBody;
import com.htmessage.sdk.model.HTMessageVoiceBody;
import com.htmessage.yichatopen.HTApp;
import com.htmessage.yichatopen.HTClientHelper;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.chat.activity.ChooseContactActivity;
import com.htmessage.yichatopen.HTConstant;

import java.io.File;

/**
 * Created by huangfangyi on 2017/7/8.
 * qq 84543217
 */

public class HTMessageUtils {

    public static HTMessage creatWithDrowMsg(HTMessage htMessage) {
        String text = null;
        JSONObject jsonObject = htMessage.getAttributes();
        String userId = jsonObject.getString(HTConstant.JSON_KEY_HXID);
        String nick = jsonObject.getString(HTConstant.JSON_KEY_NICK);
        if (HTApp.getInstance().getUsername().equals(userId)) {
            text = HTApp.getContext().getString(R.string.revoke_content);
        } else {
            text = String.format(HTApp.getContext().getString(R.string.revoke_content_someone), nick);
        }
        jsonObject.put("action", 6001);
        HTMessage message1 = HTMessage.createTextSendMessage(htMessage.getUsername(), text);
        message1.setMsgId(htMessage.getMsgId());
        message1.setChatType(htMessage.getChatType());
        message1.setDirect(htMessage.getDirect());
        message1.setAttributes(jsonObject.toJSONString());
        message1.setTime(htMessage.getTime());
        message1.setLocalTime(htMessage.getLocalTime());
        message1.setFrom(htMessage.getFrom());
        message1.setTo(htMessage.getTo());
        message1.setStatus(htMessage.getStatus());
        message1.setExt(htMessage.getExt());
        HTClient.getInstance().messageManager().updateMessageInDB(message1);
        return message1;
    }


    /**
     * 获取copy的信息
     *
     * @param context
     * @param message
     */
    public static void getCopyMsg(Activity context, HTMessage message, String toChatUserName) {
        String copyType = "";
        String fileName = "";
        String localUrl = "";
        String remotePath = "";
        HTMessage.Type type = message.getType();
        if (type == HTMessage.Type.IMAGE) {
            copyType = "image";
            HTMessageImageBody body = (HTMessageImageBody) message.getBody();
            fileName = body.getFileName();
            remotePath = body.getRemotePath();
            localUrl = body.getLocalPath();
        } else if (type == HTMessage.Type.VOICE) {
            HTMessageVoiceBody body = (HTMessageVoiceBody) message.getBody();
            copyType = "voice";
            remotePath = body.getRemotePath();
            fileName = body.getFileName();
            localUrl = body.getLocalPath();
        } else if (type == HTMessage.Type.TEXT) {
            copyType = "text";
            localUrl = ((HTMessageTextBody) message.getBody()).getContent();
        }
        String msgId = message.getMsgId();
        if (!TextUtils.isEmpty(localUrl)) {
            switch (copyType) {
                case "text":
                    showCopySendDialog(context, copyType, localUrl, message, null);
                    break;
                default:
                    getFilePath(context, copyType, message, msgId, fileName, remotePath, toChatUserName, null);
                    break;
            }
        } else {
            switch (copyType) {
                case "text":
                    showCopySendDialog(context, copyType, localUrl, message, null);
                    break;
                default:
                    getFilePath(context, copyType, message, msgId, fileName, remotePath, toChatUserName, null);
                    break;
            }
        }
    }

    /**
     * 下载copy文件并复制
     *
     * @param context
     * @param copyType
     * @param message
     * @param msgId
     * @param fileName
     * @param remotePath
     */
    public static void getFilePath(final Activity context, final String copyType, final HTMessage message, String msgId, final String fileName, final String remotePath) {
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (!TextUtils.isEmpty(msgId) && !TextUtils.isEmpty(fileType) && !TextUtils.isEmpty(remotePath)) {
//            final File file = new File(HTApp.getInstance().getDirFilePath() + msgId + fileType);
            final File file = new File(HTApp.getInstance().getDirFilePath() + fileName);
            if (file.exists()) {
                switch (copyType) {
                    case "image":
                        showCopySendDialog(context, copyType, file.getAbsolutePath(), message, file.getAbsolutePath());
                        break;
                    case "voice":
                        showCopySendDialog(context, copyType, file.getAbsolutePath(), message, null);
                        break;
                }
                return;
            }
            final ProgressDialog dialog = new ProgressDialog(context);
            dialog.setMessage(context.getString(R.string.copying));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            final String path = HTApp.getInstance().getDirFilePath() + fileName;
            dialog.show();
            new OkHttpUtils(context).loadFile(remotePath, path, new OkHttpUtils.DownloadCallBack() {
                @Override
                public void onSuccess() {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    File file1 = new File(path);
                    switch (copyType) {
                        case "image":
                            showCopySendDialog(context, copyType, file1.getAbsolutePath(), message, file1.getAbsolutePath());
                            break;
                        case "voice":
                            showCopySendDialog(context, copyType, file1.getAbsolutePath(), message, null);
                            break;
                    }
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(String message) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, R.string.copy_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    /**
     * 复制并转发
     *
     * @param copyType
     * @param localPath
     * @param message1
     */
    public static void showCopySendDialog(Activity context, final String copyType, String localPath, final HTMessage message1, String imagePath) {
        if (message1.getType() == HTMessage.Type.TEXT) {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 将文本内容放到系统剪贴板里。
            cm.setText(localPath);
            ACache.get(context.getApplicationContext()).remove("myCopy");
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("copyType", copyType);
            jsonObject.put("localPath", localPath);
            jsonObject.put("msgId", message1.getMsgId());
            jsonObject.put("imagePath", imagePath);
            ACache.get(context.getApplicationContext()).put("myCopy", jsonObject.toJSONString());
        }
    }

    /**
     * 获取copy的信息
     *
     * @param context
     * @param message
     */
    public static void getForWordMessage(Activity context, HTMessage message, final String toChatUsername, final JSONObject userJson) {
        String copyType = "";
        String fileName = "";
        String localUrl = "";
        String remotePath = "";
        HTMessage.Type type = message.getType();
        if (type == HTMessage.Type.IMAGE) {
            copyType = "image";
            HTMessageImageBody body = (HTMessageImageBody) message.getBody();
            fileName = body.getFileName();
            remotePath = body.getRemotePath();
            localUrl = body.getLocalPath();
        } else if (type == HTMessage.Type.VOICE) {
            HTMessageVoiceBody body = (HTMessageVoiceBody) message.getBody();
            copyType = "voice";
            remotePath = body.getRemotePath();
            fileName = body.getFileName();
            localUrl = body.getLocalPath();
        } else if (type == HTMessage.Type.TEXT) {
            copyType = "text";
            localUrl = ((HTMessageTextBody) message.getBody()).getContent();
        }
        String msgId = message.getMsgId();
        if (!TextUtils.isEmpty(localUrl)) {
            switch (copyType) {
                case "text":
                    showForwordDialog(context, copyType, localUrl, message, null, toChatUsername, userJson);
                    break;
                default:
                    getFilePath(context, copyType, message, msgId, fileName, remotePath, toChatUsername, userJson);
                    break;
            }
        } else {
            switch (copyType) {
                case "text":
                    showForwordDialog(context, copyType, localUrl, message, null, toChatUsername, userJson);
                    break;
                default:
                    getFilePath(context, copyType, message, msgId, fileName, remotePath, toChatUsername, userJson);
                    break;
            }
        }
    }

    /**
     * 下载copy文件并复制
     *
     * @param context
     * @param copyType
     * @param message
     * @param msgId
     * @param fileName
     * @param remotePath
     */
    public static void getFilePath(final Activity context, final String copyType, final HTMessage message, String msgId, final String fileName, final String remotePath, final String toChatUsername, final JSONObject userJson) {
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        String filePath = null;
        if (!TextUtils.isEmpty(msgId) && !TextUtils.isEmpty(fileType) && !TextUtils.isEmpty(remotePath)) {
            PathUtils pathUtils = new PathUtils(toChatUsername, context);
            if (message.getType() == HTMessage.Type.VOICE) {
                filePath = pathUtils.getVoicePath().getAbsolutePath() + "/" + fileName;
            } else if (message.getType() == HTMessage.Type.IMAGE) {
                filePath = pathUtils.getImagePath().getAbsolutePath() + "/" + fileName;
            }
            File file = new File(filePath);
            if (file.exists()) {
                switch (copyType) {
                    case "image":
                        if (userJson != null) {
                            showForwordDialog(context, copyType, file.getAbsolutePath(), message, file.getAbsolutePath(), toChatUsername, userJson);
                        } else {
                            showCopySendDialog(context, copyType, file.getAbsolutePath(), message, file.getAbsolutePath());
                        }
                        break;
                    case "voice":
                        if (userJson != null) {
                            showForwordDialog(context, copyType, file.getAbsolutePath(), message, null, toChatUsername, userJson);
                        } else {
                            showCopySendDialog(context, copyType, file.getAbsolutePath(), message, null);
                        }
                        break;
                }
                return;
            }

            loadMessageFile(message, toChatUsername, context, new CallBack() {
                @Override
                public void error() {
                    if (userJson == null) {
                        Toast.makeText(context, R.string.copy_failed, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void completed(String localPath) {
                    switch (copyType) {
                        case "image":
                            if (userJson != null) {
                                showForwordDialog(context, copyType, localPath, message, localPath, toChatUsername, userJson);
                            } else {
                                showCopySendDialog(context, copyType,localPath, message, localPath);
                            }
                            break;
                        case "voice":
                            if (userJson != null) {
                                showForwordDialog(context, copyType, localPath, message, null, toChatUsername, userJson);
                            } else {
                                Log.d("slj","===复制:"+userJson);
                                showCopySendDialog(context, copyType, localPath, message, null);
                            }
                            break;
                    }
                    if (userJson == null) {
                        Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
//        if (!TextUtils.isEmpty(msgId) && !TextUtils.isEmpty(fileType) && !TextUtils.isEmpty(remotePath)) {
//            final File file = new File(HTApp.getInstance().getDirFilePath() + fileName);
//            if (file.exists()) {
//                switch (copyType) {
//                    case "image":
//                        showForwordDialog(context, copyType, file.getAbsolutePath(), message, file.getAbsolutePath(),toChatUsername,userJson);
//                        break;
//                    case "voice":
//                        showForwordDialog(context, copyType, file.getAbsolutePath(), message, null,toChatUsername,userJson);
//                        break;
//                }
//                return;
//            }
//            final ProgressDialog dialog = new ProgressDialog(context);
//            dialog.setMessage(context.getString(R.string.forword_get));
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
////            final String path = HTApp.getInstance().getDirFilePath() + msgId + fileType;
//            final String path = HTApp.getInstance().getDirFilePath() + fileName;
//            dialog.show();
//            new OkHttpUtils(context).loadFile(remotePath, path, new OkHttpUtils.DownloadCallBack() {
//                @Override
//                public void onSuccess() {
//                    if (dialog != null && dialog.isShowing()) {
//                        dialog.dismiss();
//                    }
//                    File file1 = new File(path);
//                    switch (copyType) {
//                        case "image":
//                            showForwordDialog(context, copyType, file1.getAbsolutePath(), message, file1.getAbsolutePath(),toChatUsername,userJson);
//                            break;
//                        case "voice":
//                            showForwordDialog(context, copyType, file1.getAbsolutePath(), message, null,toChatUsername,userJson);
//                            break;
//                    }
//                 }
//
//                @Override
//                public void onFailure(String message) {
//                    if (dialog != null && dialog.isShowing()) {
//                        dialog.dismiss();
//                    }
//                }
//            });
//        }
    }


    /**
     * 转发
     *
     * @param forwordType
     * @param localPath
     * @param message1
     * @param imagePath
     * @param toChatUsername
     * @param extJSON
     */
    private static void showForwordDialog(Activity context, final String forwordType, final String localPath, final HTMessage message1, String imagePath, String toChatUsername, JSONObject extJSON) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("forwordType", forwordType);
        jsonObject.put("localPath", localPath);
        jsonObject.put("msgId", message1.getMsgId());
        jsonObject.put("imagePath", imagePath);
        jsonObject.put("toChatUsername", toChatUsername);
        jsonObject.put("exobj", extJSON.toJSONString());
        Intent intent = new Intent(context, ChooseContactActivity.class);
        intent.putExtra("obj", jsonObject.toJSONString());
        context.startActivity(intent);
    }


    public static void loadMessageFile(HTMessage htMessage, String chatTo, final Activity context, final CallBack callBack) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(context.getString(R.string.loading));
        progressDialog.show();
        PathUtils pathUtils = new PathUtils(chatTo, context);
        String remotePath = "";

        String fileName = "";
        String filePath = null;
        if (htMessage.getType() == HTMessage.Type.VOICE) {
            HTMessageVoiceBody htMessageVoiceBody = (HTMessageVoiceBody) htMessage.getBody();
            remotePath = htMessageVoiceBody.getRemotePath();
            fileName = htMessageVoiceBody.getFileName();
            filePath = pathUtils.getVoicePath().getAbsolutePath() + "/" + fileName;
        } else if (htMessage.getType() == HTMessage.Type.IMAGE) {
            HTMessageImageBody htMessageImageBody = (HTMessageImageBody) htMessage.getBody();
            remotePath = htMessageImageBody.getRemotePath();
            fileName = htMessageImageBody.getFileName();
            filePath = pathUtils.getImagePath().getAbsolutePath() + "/" + fileName;
        }
        final String finalFilePath = filePath;
        new OkHttpUtils(context).loadFile(remotePath, filePath, new OkHttpUtils.DownloadCallBack() {
            @Override
            public void onSuccess() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        callBack.completed(finalFilePath);
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.error();
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }


    public interface CallBack {
        void error();

        void completed(String localPath);
    }

}
