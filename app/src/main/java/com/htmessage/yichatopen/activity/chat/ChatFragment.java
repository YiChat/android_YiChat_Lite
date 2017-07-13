package com.htmessage.yichatopen.activity.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.htmessage.sdk.ChatType;
import com.htmessage.sdk.client.HTClient;
import com.htmessage.sdk.manager.HTChatManager;
import com.htmessage.sdk.model.CmdMessage;
import com.htmessage.sdk.model.HTMessage;
import com.htmessage.sdk.model.HTMessageImageBody;
import com.htmessage.sdk.model.HTMessageVoiceBody;
import com.htmessage.sdk.utils.MessageUtils;
import com.htmessage.yichatopen.HTApp;
import com.htmessage.yichatopen.HTConstant;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.IMAction;

import com.htmessage.yichatopen.activity.chat.weight.emoji.Emojicon;
import com.htmessage.yichatopen.activity.chat.weight.ChatInputView;
import com.htmessage.yichatopen.activity.chat.weight.loadmore.PullToLoadMoreListView;
import com.htmessage.yichatopen.utils.ACache;
import com.htmessage.yichatopen.utils.HTMessageUtils;
import com.htmessage.yichatopen.utils.PathUtils;
import com.htmessage.yichatopen.activity.chat.weight.ChatExtendMenu;
import com.htmessage.yichatopen.widget.HTAlertDialog;
import com.htmessage.yichatopen.activity.chat.weight.VoiceRecorderView;
import com.htmessage.yichatopen.utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class ChatFragment extends Fragment {

    private static final int REQUEST_CODE_CAMERA = 2;
    private static final int REQUEST_CODE_LOCAL = 3;
    /**
     * params to fragment
     */
    private Bundle fragmentArgs;
    private int chatType;
    private String toChatUsername;
    private File cameraFile;
    private ListView listView;
    static final int ITEM_TAKE_PICTURE = 1;
    static final int ITEM_PICTURE = 2;

    private int[] itemStrings = {R.string.attach_take_pic, R.string.attach_picture};
    private int[] itemdrawables = {R.drawable.chat_takepic_selector, R.drawable.chat_image_selector};
    private int[] itemIds = {ITEM_TAKE_PICTURE, ITEM_PICTURE};
    private MyItemClickListener extendMenuItemClickListener;

    private List<HTMessage> htMessages;
    private ChatAdapter adapter;
    private PullToLoadMoreListView pullToLoadMoreListView;
    private ChatInputView chatInputView;
    private MyInputViewLisenter inputViewLisenter;
    private VoiceRecorderView voiceRecorderView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        voiceRecorderView = (VoiceRecorderView) root.findViewById(R.id.voice_recorder);
        pullToLoadMoreListView = (PullToLoadMoreListView) root.findViewById(R.id.list);
        pullToLoadMoreListView.setOnRefreshListener(new PullToLoadMoreListView.OnRefreshListener() {

            @Override
            public void onPullDownLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        HTMessage message = getLastMessage();
                        if (message != null) {
                            List<HTMessage> htMessages = HTClient.getInstance().messageManager().loadMoreMsgFromDB(toChatUsername, message.getTime(), 20);
                            if (htMessages.size() == 0) {
                                Toast.makeText(getActivity(), R.string.not_more_msg, Toast.LENGTH_SHORT).show();

                            } else {
                                Collections.reverse(htMessages);
                                htMessages.addAll(0, htMessages);
                                adapter.notifyDataSetChanged();

                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.not_more_msg, Toast.LENGTH_SHORT).show();

                        }

                        pullToLoadMoreListView.onRefreshComplete();

                    }
                }, 1000);
            }
        });

        listView = pullToLoadMoreListView.getListView();
        extendMenuItemClickListener = new MyItemClickListener();
        chatInputView = (ChatInputView) root.findViewById(R.id.inputView);
        inputViewLisenter = new MyInputViewLisenter();
        chatInputView.initView(getActivity(), pullToLoadMoreListView, inputViewLisenter, getExtendMenuItem());
        listView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                chatInputView.hideSoftInput();
                chatInputView.interceptBackPress();

                return false;
            }
        });
        return root;
    }

    private JSONObject extJSON = new JSONObject();

    private boolean isHolder = false;
    private MyBroadcastReciver myBroadcastReciver;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        extJSON.put(HTConstant.JSON_KEY_HXID, HTApp.getInstance().getUserJson().getString(HTConstant.JSON_KEY_HXID));
        extJSON.put(HTConstant.JSON_KEY_NICK, HTApp.getInstance().getUserJson().getString(HTConstant.JSON_KEY_NICK));
        String avatar = HTApp.getInstance().getUserJson().getString(HTConstant.JSON_KEY_AVATAR);
        if (!TextUtils.isEmpty(avatar)) {
            if (!avatar.contains("http:")) {
                avatar = HTConstant.URL_AVATAR + avatar;
            }
        }
        extJSON.put(HTConstant.JSON_KEY_AVATAR, avatar);
        fragmentArgs = getArguments();
        chatType = fragmentArgs.getInt("chatType", MessageUtils.CHAT_SINGLE);
        toChatUsername = fragmentArgs.getString("userId");
        super.onActivityCreated(savedInstanceState);
        setUpView();
        myBroadcastReciver = new MyBroadcastReciver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IMAction.ACTION_MESSAGE_WITHDROW);
        intentFilter.addAction(IMAction.ACTION_MESSAGE_FORWORD);
        intentFilter.addAction(IMAction.ACTION_NEW_MESSAGE);
        intentFilter.addAction(IMAction.ACTION_MESSAGE_EMPTY);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(myBroadcastReciver, intentFilter);
    }


    private void setUpView() {
        getAllMessage();
        adapter = new ChatAdapter(htMessages, this, toChatUsername, chatType);
        listView.setAdapter(adapter);
        listView.setSelection(listView.getCount() - 1);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                HTMessage htMessage = adapter.getItem(i);
                if (htMessage != null) {
                    showMsgDialog(htMessage, i);
                }

                return true;
            }
        });


    }

    private void getAllMessage() {
        htMessages = HTClient.getInstance().messageManager().getMessageList(toChatUsername);
        HTClient.getInstance().conversationManager().markAllMessageRead(toChatUsername);
    }

    private class MyInputViewLisenter implements ChatInputView.InputViewLisenter {

        @Override
        public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
            return voiceRecorderView.onPressToSpeakBtnTouch(v, event, new VoiceRecorderView.EaseVoiceRecorderCallback() {

                @Override
                public void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength) {
                    //Log.d("voiceFilePath--->", voiceFilePath);
                    sendVoiceMessage(voiceFilePath, voiceTimeLength);
                }
            });
        }

        @Override
        public void onBigExpressionClicked(Emojicon emojicon) {

        }

        @Override
        public void onSendButtonClicked(String content) {
            sendTextMessage(content);
        }

        @Override
        public boolean onEditTextLongClick() {
            String myCopy = ACache.get(getActivity()).getAsString("myCopy");
            if (!TextUtils.isEmpty(myCopy)) {
                JSONObject jsonObject = JSONObject.parseObject(myCopy);
                String msgId = jsonObject.getString("msgId");
                String imagePath = jsonObject.getString("imagePath");
                HTMessage emMessage = getCopyMessage(msgId);
                if (emMessage == null) {
                    return true;
                }
                showCopyContent(jsonObject.getString("copyType"), jsonObject.getString("localPath"), emMessage, imagePath);
                return true;
            }
            return false;
        }

        @Override
        public void onEditTextUp() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listView.smoothScrollToPosition(listView.getCount() - 1);
                }
            }, 400);
        }
    }

    private HTMessage getCopyMessage(String msgId) {
        for (HTMessage htMessage : htMessages) {
            if (htMessage.getMsgId().equals(msgId)) {
                return htMessage;
            }
        }
        return null;
    }

    private HTMessage getLastMessage() {
        if (htMessages != null && htMessages.size() != 0) {
            return adapter.getItem(0);
        }
        return null;
    }

    private ChatExtendMenu getExtendMenuItem() {

        ChatExtendMenu chatExtendMenu = new ChatExtendMenu(getContext());
        //use the menu in base class
        for (int i = 0; i < itemStrings.length; i++) {
            chatExtendMenu.registerMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], extendMenuItemClickListener);
        }
        chatExtendMenu.init();
        return chatExtendMenu;
    }


    /**
     * handle the click event for extend menu
     */
    class MyItemClickListener implements ChatExtendMenu.EaseChatExtendMenuItemClickListener {

        @Override
        public void onClick(int itemId, View view) {

            switch (itemId) {
                case ITEM_TAKE_PICTURE:
                    selectPicFromCamera();
                    isHolder = true;
                    break;
                case ITEM_PICTURE:
                    selectPicFromLocal();
                    isHolder = true;
                    break;

                default:
                    break;
            }
        }

    }


    @Override
    public void onStop() {
        if (!isHolder) {
            ChatActivity.activityInstance = null;
        }
        super.onStop();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isHolder = false;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
                if (cameraFile != null && cameraFile.exists())
                    sendImageMessage(cameraFile.getAbsolutePath());
            } else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        sendPicByUri(selectedImage);
                    }
                }
            }
        }
    }

    private void showMsgDialog(final HTMessage message, final int i) {

        HTAlertDialog fxAlertDialog = new HTAlertDialog(getActivity(), null, new String[]{getActivity().getString(R.string.delete), getActivity().getString(R.string.copy), getActivity().getString(R.string.forward)});
        if (message.getDirect() == HTMessage.Direct.SEND) {
            fxAlertDialog = new HTAlertDialog(getActivity(), null, new String[]{getActivity().getString(R.string.delete), getActivity().getString(R.string.copy), getActivity().getString(R.string.forward), getActivity().getString(R.string.reback)});
        }
        fxAlertDialog.init(new HTAlertDialog.OnItemClickListner() {
            @Override
            public void onClick(int position) {
                if (position == 0) { //删除
                    HTClient.getInstance().messageManager().deleteMessage(toChatUsername, message.getMsgId());
                    htMessages.remove(message);
                    adapter.notifyDataSetChanged();
                } else if (position == 1) { //复制
                    HTMessageUtils.getCopyMsg(getActivity(), message, toChatUsername);
                } else if (position == 2) {//转发
                    HTMessageUtils.getForWordMessage(getActivity(), message, toChatUsername, extJSON);
                } else if (position == 3) {//撤回
                    long msgTime = message.getTime();
                    long nowTime = System.currentTimeMillis();
                    if ((nowTime - msgTime) / (1000 * 60) < 2) {
                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setMessage(getString(R.string.rebacking));
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        CmdMessage cmdMessage = new CmdMessage();
                        cmdMessage.setTo(toChatUsername);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("action", 6000);
                        jsonObject.put("msgId", message.getMsgId());
                        cmdMessage.setBody(jsonObject.toString());
                        if (chatType == MessageUtils.CHAT_GROUP) {
                            cmdMessage.setChatType(ChatType.groupChat);
                        }
                        HTClient.getInstance().chatManager().sendCmdMessage(cmdMessage, new HTChatManager.HTMessageCallBack() {
                            @Override
                            public void onProgress() {

                            }

                            @Override
                            public void onSuccess() {
                                HTClient.getInstance().messageManager().deleteMessage(toChatUsername, message.getMsgId());
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        HTMessage htMessage = HTMessageUtils.creatWithDrowMsg(message);
                                        htMessages.set(i, htMessage);
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                            }

                            @Override
                            public void onFailure() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), R.string.reback_failed, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    } else {
                        Toast.makeText(getActivity(), R.string.reback_not_more_than_30, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        HTClient.getInstance().conversationManager().markAllMessageRead(toChatUsername);
        ChatActivity.activityInstance = (ChatActivity) getActivity();
    }


    public void onBackPressed() {
        if (!chatInputView.interceptBackPress()) {
            getActivity().finish();
        }
    }

    @Override
    public void onPause() {
        if (chatInputView != null) {
            chatInputView.hideSoftInput();
            chatInputView.interceptBackPress();
        }
        super.onPause();
    }

    private void sendTextMessage(final String content) {
        HTMessage htMessage = HTMessage.createTextSendMessage(toChatUsername, content);
        sendMessage(htMessage);
    }

    public void sendMessage(final HTMessage htMessage) {
        htMessage.setAttributes(extJSON.toJSONString());
        if (chatType == MessageUtils.CHAT_GROUP) {
            htMessage.setChatType(ChatType.groupChat);
        }
        HTClient.getInstance().chatManager().sendMessage(htMessage, new HTChatManager.HTMessageCallBack() {
            @Override
            public void onProgress() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        htMessages.add(htMessage);
                        adapter.notifyDataSetChanged();
                        if (htMessages.size() > 0) {
                            listView.setSelection(listView.getCount() - 1);
                        }
                    }
                });
            }

            @Override
            public void onSuccess() {
                htMessage.setStatus(HTMessage.Status.SUCCESS);
                HTClient.getInstance().messageManager().saveMessage(htMessage, false);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        if (htMessages.size() > 0) {
                            listView.setSelection(listView.getCount() - 1);
                        }
                    }
                });

            }

            @Override
            public void onFailure() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        htMessage.setStatus(HTMessage.Status.FAIL);
                        HTClient.getInstance().messageManager().saveMessage(htMessage, false);
                        adapter.notifyDataSetChanged();
                        if (htMessages.size() > 0) {
                            listView.setSelection(listView.getCount() - 1);
                        }
                    }
                });
            }

        });


    }


    private void sendVoiceMessage(String filePath, int length) {
        HTMessage htMessage = HTMessage.createVoiceSendMessage(toChatUsername, filePath, length);
        sendMessage(htMessage);
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return degree;
        }
        return degree;
    }

    /**
     * 旋转图片，使图片保持正确的方向。
     *
     * @param bitmap  原始图片
     * @param degrees 原始图片的角度
     * @return Bitmap 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0 || null == bitmap) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (null != bitmap) {
            bitmap.recycle();
        }
        return bmp;
    }

    private void sendImageMessage(String imagePath) {

        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        Bitmap bitmap = rotateBitmap(bmp, readPictureDegree(imagePath));
        String size = bitmap.getWidth() + "," + bitmap.getHeight();
        Log.d("size---->", size);
        HTMessage htMessage = HTMessage.createImageSendMessage(toChatUsername, imagePath, size);
        sendMessage(htMessage);
    }

    //===================================================================================


    /**
     * send image
     *
     * @param selectedImage
     */
    private void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(getActivity(), R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendImageMessage(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(getActivity(), R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendImageMessage(file.getAbsolutePath());
        }

    }

    /**
     * capture new image
     */
    private void selectPicFromCamera() {
        if (!CommonUtils.isSdcardExist()) {
            Toast.makeText(getActivity(), R.string.sd_card_does_not_exist, Toast.LENGTH_SHORT).show();
            return;
        }

        cameraFile = new File(new PathUtils(toChatUsername, getContext()).getImagePath() + "/" + HTApp.getInstance().getUsername()
                + System.currentTimeMillis() + ".jpg");
        //   cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                REQUEST_CODE_CAMERA);
    }

    /**
     * select local image
     */
    private void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }


    @Override
    public void onDestroy() {

        if (myBroadcastReciver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(myBroadcastReciver);
        }
        super.onDestroy();
    }


    /**
     * 复制
     *
     * @param copyType
     * @param localPath
     * @param message1
     * @param imagePath
     */
    private void showCopyContent(final String copyType, final String localPath, final HTMessage message1, String imagePath) {
        AlertDialog.Builder buidler = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.item_dialog_gridview, null);
        TextView tv_forward = (TextView) view.findViewById(R.id.tv_forward);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        TextView tv_ok = (TextView) view.findViewById(R.id.tv_ok);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setVisibility(View.GONE);
        tv_forward.setText(R.string.copy);
        textView.setText(R.string.really_copy_and_send);
        buidler.setView(view);
        if ("image".equals(copyType) && imagePath != null) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(imagePath).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        }
        final AlertDialog dialog = buidler.show();
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                switch (copyType) {
                    case "text":
                        sendTextMessage(localPath);
                        break;
                    case "voice":
                        HTMessageVoiceBody voiceBody = (HTMessageVoiceBody) message1.getBody();
                        HTMessage voiceMSg = HTMessage.createVoiceSendMessage(toChatUsername, localPath, voiceBody.getAudioDuration());
                        sendMessage(voiceMSg);
                        break;
                    case "image":
                        HTMessageImageBody imageBody = (HTMessageImageBody) message1.getBody();
                        HTMessage message = HTMessage.createImageSendMessage(toChatUsername, localPath, imageBody.getSize());
                        sendMessage(message);
                        break;
                }
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    /**
     * 撤回消息
     *
     * @param msgId
     */
    private void onMessageWithdrow(String msgId) {
        for (int i = 0; i < htMessages.size(); i++) {
            HTMessage htMessage = htMessages.get(i);
            if (htMessage.getMsgId().equals(msgId)) {
                HTMessage message = HTMessageUtils.creatWithDrowMsg(htMessage);
                htMessages.set(i, message);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class MyBroadcastReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IMAction.ACTION_MESSAGE_WITHDROW)) {
                String msgId = intent.getStringExtra("msgId");
                onMessageWithdrow(msgId);
            } else if (intent.getAction().equals(IMAction.ACTION_MESSAGE_FORWORD)) {
                HTMessage message = intent.getParcelableExtra("message");
                if (message.getTo().equals(toChatUsername)) {
                    if (!htMessages.contains(message)) {
                        htMessages.add(message);
                    }
                    adapter.notifyDataSetChanged();
                    if (htMessages.size() > 0) {
                        listView.setSelection(listView.getCount() - 1);
                    }
                }
            } else if (intent.getAction().equals(IMAction.ACTION_NEW_MESSAGE)) {
                HTMessage message = intent.getParcelableExtra("message");
                if ((message.getChatType() == ChatType.singleChat) && (message.getFrom().equals(toChatUsername)) || ((message.getChatType() == ChatType.groupChat) && (message.getTo().equals(toChatUsername)))) {
                    if (!htMessages.contains(message)) {
                        htMessages.add(message);
                    }
                    adapter.notifyDataSetChanged();
                    if (htMessages.size() > 0) {
                        listView.setSelection(listView.getCount() - 1);
                    }
                    HTClient.getInstance().conversationManager().markAllMessageRead(toChatUsername);
                }
            } else if (IMAction.ACTION_MESSAGE_EMPTY.equals(intent.getAction())) {
                htMessages.clear();
                adapter.notifyDataSetChanged();
            }
        }
    }
}
