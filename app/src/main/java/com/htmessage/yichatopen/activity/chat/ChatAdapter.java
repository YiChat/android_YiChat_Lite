package com.htmessage.yichatopen.activity.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.htmessage.yichatopen.HTApp;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.main.details.UserDetailsActivity;
import com.htmessage.yichatopen.HTConstant;
import com.htmessage.yichatopen.activity.chat.weight.VoicePlayClickListener;
import com.htmessage.yichatopen.activity.chat.weight.emoji.SmileUtils;
import com.htmessage.yichatopen.utils.ACache;
import com.htmessage.yichatopen.utils.OkHttpUtils;
import com.htmessage.yichatopen.utils.DateUtils;
 import com.htmessage.sdk.client.HTClient;
import com.htmessage.sdk.model.HTMessageBody;
import com.htmessage.sdk.model.HTMessageImageBody;
import com.htmessage.sdk.model.HTMessageTextBody;
import com.htmessage.sdk.model.HTMessageVoiceBody;
import com.htmessage.sdk.utils.MessageUtils;
import com.htmessage.sdk.model.HTMessage;
import com.htmessage.yichatopen.utils.PathUtils;
import com.htmessage.yichatopen.utils.ImageUtils;
import com.htmessage.yichatopen.activity.ShowBigImageActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by huangfangyi on 2016/11/24.
 * qq 84543217
 */

public class ChatAdapter extends BaseAdapter {
    private List<HTMessage> msgs;
    private Activity context;
    private LayoutInflater inflater;
    private static final int MESSAGE_TEXT_RECEIVED = 0;
    private static final int MESSAGE_TEXT_SEND = 1;
    private static final int MESSAGE_IMAGE_RECEIVED = 2;
    private static final int MESSAGE_IMAGE_SEND = 3;
    private static final int MESSAGE_VOICE_RECEIVED = 4;
    private static final int MESSAGE_VOICE_SEND = 5;

    private String chatTo;
    private int chatType;
    private List<HTMessage> imageMsgs = new ArrayList<>();
    private ChatFragment fragment;
    public ChatAdapter(List<HTMessage> msgs, ChatFragment fragment, String chatTo, int chatType) {
        this.msgs = msgs;
        this.context = fragment.getActivity();
        this.fragment=fragment;
        inflater = LayoutInflater.from(context);
        this.chatTo = chatTo;
        this.chatType = chatType;
        imageMsgs.clear();
        for (int i = 0; i < msgs.size(); i++) {
            HTMessage emMessage = msgs.get(i);
            if (emMessage.getType() == HTMessage.Type.IMAGE) {
                imageMsgs.add(emMessage);
            }
        }

    }

    @Override
    public int getCount() {
        return msgs.size();
    }

    @Override
    public HTMessage getItem(int position) {
        return msgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        HTMessage message = getItem(position);
        return getItemViewType(message);

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for (int i = 0; i < msgs.size(); i++) {
            HTMessage emMessage = msgs.get(i);
            if (emMessage.getType() == HTMessage.Type.IMAGE) {
                if (!imageMsgs.contains(emMessage)) {
                    imageMsgs.add(emMessage);
                }
            }
        }

    }

    @Override
    public int getViewTypeCount() {
        return 14;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HTMessage message = getItem(position);
        int viewType = getItemViewType(position);
        if (convertView == null) {
            convertView = getViewByType(viewType, parent);
        }
        ChatViewHolder holder = (ChatViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new ChatViewHolder();
            handleViewAndHolder(viewType, convertView, holder, message);
        }
        handleData(holder, viewType, message, position);

        return convertView;
    }


    private int getItemViewType(HTMessage htMessage) {
        HTMessage.Type type = htMessage.getType();
        if (type == HTMessage.Type.TEXT) {
            return htMessage.getDirect() == HTMessage.Direct.RECEIVE ? MESSAGE_TEXT_RECEIVED : MESSAGE_TEXT_SEND;
        } else if (type == HTMessage.Type.IMAGE) {
            return htMessage.getDirect() == HTMessage.Direct.RECEIVE ? MESSAGE_IMAGE_RECEIVED : MESSAGE_IMAGE_SEND;
        } else if (type == HTMessage.Type.VOICE) {
            return htMessage.getDirect() == HTMessage.Direct.RECEIVE ? MESSAGE_VOICE_RECEIVED : MESSAGE_VOICE_SEND;
        }
        return 0;

    }


    private View getViewByType(int viewType, ViewGroup parent) {
        switch (viewType) {
            case MESSAGE_TEXT_RECEIVED:
                return inflater.inflate(R.layout.row_received_message, parent, false);
            case MESSAGE_IMAGE_RECEIVED:
                return inflater.inflate(R.layout.row_received_picture, parent, false);
            case MESSAGE_VOICE_RECEIVED:
                return inflater.inflate(R.layout.row_received_voice, parent, false);
            case MESSAGE_TEXT_SEND:
                return inflater.inflate(R.layout.row_sent_message, parent, false);
            case MESSAGE_IMAGE_SEND:
                return inflater.inflate(R.layout.row_sent_picture, parent, false);
            case MESSAGE_VOICE_SEND:
                return inflater.inflate(R.layout.row_sent_voice, parent, false);
            default:
                return inflater.inflate(R.layout.row_sent_message, parent, false);
        }
    }


    private void handleViewAndHolder(int viewType, View convertView, ChatViewHolder holder, final HTMessage htMessage) {
        holder.reBubble = (RelativeLayout) convertView.findViewById(R.id.bubble);
        holder.reBubble.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        holder.ivAvatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
        holder.timeStamp = (TextView) convertView.findViewById(R.id.timestamp);
        holder.tv_ack_msg = (TextView) convertView.findViewById(R.id.tv_ack_msg);
        holder.tv_delivered = (TextView) convertView.findViewById(R.id.tv_delivered);
        if (htMessage.getDirect() == HTMessage.Direct.RECEIVE) {
            //接收消息,可以显示成员名称
            holder.tvNick = (TextView) convertView.findViewById(R.id.tv_userid);
        } else {
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            holder.ivMsgStatus = (ImageView) convertView.findViewById(R.id.msg_status);
        }

        if (htMessage.getType() == HTMessage.Type.TEXT) {
            holder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
        }
        if (viewType == MESSAGE_IMAGE_SEND || viewType == MESSAGE_IMAGE_RECEIVED) {
            holder.ivContent = (ImageView) convertView.findViewById(R.id.image);
        }
        if (viewType == MESSAGE_VOICE_SEND || viewType == MESSAGE_VOICE_RECEIVED) {
            holder.tvDuration = (TextView) convertView.findViewById(R.id.tv_length);
            holder.ivVoice = (ImageView) convertView.findViewById(R.id.iv_voice);
            if (viewType == MESSAGE_VOICE_RECEIVED) {
                holder.ivUnread = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
            }
        }
        if (htMessage.getType() == HTMessage.Type.TEXT) {
            holder.reMain = (RelativeLayout) convertView.findViewById(R.id.re_main);
        }
        holder.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = htMessage.getAttributes();
                if (jsonObject!=null){
                    context.startActivity(new Intent(context, UserDetailsActivity.class).putExtra(HTConstant.KEY_USER_INFO,jsonObject.toJSONString()));
                }
            }
        });
        convertView.setTag(holder);

    }


    private void handleData(ChatViewHolder holder, int viewType, final HTMessage message, int position) {

        if (message.getDirect() == HTMessage.Direct.SEND && message.getStatus() == HTMessage.Status.FAIL) {
            holder.ivMsgStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReSendDialog(message);
                }
            });
        }
        HTMessageBody htMessageBody = message.getBody();
        if (htMessageBody == null) {
            return;
        }
        if (position == 0) {
            holder.timeStamp.setText(DateUtils.getTimestampString(new Date(message
                    .getTime())));
            holder.timeStamp.setVisibility(View.VISIBLE);
        } else {
            // 两条消息时间离得如果稍长，显示时间
            if (DateUtils.isCloseEnough(message.getTime(), getItem(position - 1).getTime())) {
                holder.timeStamp.setVisibility(View.GONE);
            } else {
                holder.timeStamp.setText(DateUtils.getTimestampString(new Date(
                        message.getTime())));
                holder.timeStamp.setVisibility(View.VISIBLE);
            }
        }

        if (chatType == MessageUtils.CHAT_GROUP && message.getDirect() == HTMessage.Direct.RECEIVE) {
            holder.tvNick.setVisibility(View.VISIBLE);

        } else if (chatType == MessageUtils.CHAT_SINGLE && message.getDirect() == HTMessage.Direct.RECEIVE) {
            holder.tvNick.setVisibility(View.GONE);
        }
        String avatar = message.getStringAttribute(HTConstant.JSON_KEY_AVATAR);
        if (message.getDirect() == HTMessage.Direct.SEND){
            if (message.getFrom().equals(HTApp.getInstance().getUsername())){
                JSONObject userJson = HTApp.getInstance().getUserJson();
                if (userJson.containsKey(HTConstant.JSON_KEY_AVATAR)){
                    avatar = userJson.getString(HTConstant.JSON_KEY_AVATAR);
                }
            }
        }
        Glide.with(context).load(avatar).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.default_avatar).into(holder.ivAvatar);
        HTMessage.Status status = message.getStatus();
        if (message.getDirect() == HTMessage.Direct.SEND) {
            if (status == HTMessage.Status.CREATE) {
                holder.progressBar.setVisibility(View.VISIBLE);
            } else {
                holder.progressBar.setVisibility(View.GONE);
            }
            if (status == HTMessage.Status.FAIL) {
                holder.ivMsgStatus.setVisibility(View.VISIBLE);
            } else {
                holder.ivMsgStatus.setVisibility(View.GONE);
            }
//            if (status == HTMessage.Status.SUCCESS){
//                holder.tv_delivered.setVisibility(View.VISIBLE);
//            } else {
//                holder.tv_delivered.setVisibility(View.INVISIBLE);
//            }
        }
        if (message.getType() == HTMessage.Type.TEXT) {
            int action = message.getIntAttribute("action", 0);
             if (action == 3000) {

            } else if (action == 6001) {
                holder.timeStamp.setVisibility(View.VISIBLE);
                holder.reMain.setVisibility(View.GONE);
                holder.timeStamp.setText(((HTMessageTextBody) htMessageBody).getContent());
            } else {
                holder.reMain.setVisibility(View.VISIBLE);
                holder.tvContent.setText(SmileUtils.getSmiledText(context, ((HTMessageTextBody) htMessageBody).getContent()),
                        TextView.BufferType.SPANNABLE);
            }
        }
        if (message.getType() == HTMessage.Type.IMAGE) {
            showImageView(message, holder, true, false);
        } else if (message.getType() == HTMessage.Type.VOICE) {
            showVoiceView(message, holder);
        }
    }

    private void showImageView(final HTMessage htMessage, ChatViewHolder holder, boolean isReSize, boolean isLocMsg) {
        if (!isReSize) {
            holder.ivContent.setImageResource(R.drawable.ht_location);
        } else {
            holder.ivContent.setImageResource(R.drawable.default_image);
        }
        String localPath = null;
        if (!isLocMsg) {
            HTMessageImageBody htMessageImageBody = (HTMessageImageBody) htMessage.getBody();
            localPath = htMessageImageBody.getLocalPath();
        }
        if (!TextUtils.isEmpty(localPath)) {
            Bitmap bitmap = ACache.get(context.getApplicationContext()).getAsBitmap(htMessage.getMsgId());
            if (bitmap == null) {
                Log.d("bitmap3---->", "null");
                if (new File(localPath).exists()) {
                    bitmap = ImageUtils.decodeScaleImage(localPath);
                    if (bitmap != null) {
                        ACache.get(context.getApplicationContext()).put(htMessage.getMsgId(), bitmap);
                        holder.ivContent.setImageBitmap(bitmap);
                    }
                } else {
                    downLoadImageFromServer(htMessage, holder.ivContent, isReSize, isLocMsg);
                }

            } else {
                holder.ivContent.setImageBitmap(bitmap);
            }
        } else {
            downLoadImageFromServer(htMessage, holder.ivContent, isReSize, isLocMsg);
        }


        final String finalLocalPath = localPath;
        holder.reBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalLocalPath == null || !new File(finalLocalPath).exists()) {
                    downLoadBigImageAndOpen(htMessage);
                } else {
                    context.startActivity(new Intent(context, ShowBigImageActivity.class).putExtra("localPath", finalLocalPath));
                }
            }
        });
    }


    private void downLoadImageFromServer(final HTMessage htMessage, final ImageView imageView, boolean isReSize, final boolean isLocMsg) {
        //下载缩略图,显示并且保存至缓存.
        String remotePath = null;
        String fileName = null;
        if (!isLocMsg) {
            HTMessageImageBody htMessageImageBody = (HTMessageImageBody) htMessage.getBody();
            remotePath = htMessageImageBody.getRemotePath();
            fileName = htMessageImageBody.getFileName();
        }
        if (!TextUtils.isEmpty(remotePath)) {
            if (isReSize) {
                remotePath = remotePath + HTConstant.baseImgUrl_set;

            }

            PathUtils pathUtils = new PathUtils(chatTo, context);

            final String filePath = pathUtils.getImagePath().getAbsolutePath() + "/mini" + fileName;
            new OkHttpUtils(context).loadFile(remotePath, filePath, new OkHttpUtils.DownloadCallBack() {
                @Override
                public void onSuccess() {
                    if (new File(filePath).exists()) {

                        final Bitmap bitmap = ImageUtils.decodeScaleImage(filePath);
                        if (!isLocMsg) {

                            HTMessageImageBody htMessageImageBody = (HTMessageImageBody) htMessage.getBody();
                            htMessageImageBody.setLocalPath(filePath);
                            htMessage.setBody(htMessageImageBody);
                        }
                        HTClient.getInstance().messageManager().saveMessage(htMessage, false);
                        ACache.get(context.getApplicationContext()).put(htMessage.getMsgId(), bitmap);

                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(String message) {

                }
            });
        }
    }

    private void downLoadBigImageAndOpen(final HTMessage htMessage) {
        final HTMessageImageBody htMessageImageBody = (HTMessageImageBody) htMessage.getBody();

        String remotePath = htMessageImageBody.getRemotePath();
        String fileName = htMessageImageBody.getFileName();
        if (TextUtils.isEmpty(remotePath) || TextUtils.isEmpty(fileName)) {
            return;
        }
        final Dialog progressDialog = HTApp.getInstance().createLoadingDialog(context, context.getString(R.string.loading));
        progressDialog.show();
        PathUtils pathUtils = new PathUtils(chatTo, context);
        final String filePath = pathUtils.getImagePath().getAbsolutePath() + "/" + fileName;
        new OkHttpUtils(context).loadFile(remotePath, filePath, new OkHttpUtils.DownloadCallBack() {
            @Override
            public void onSuccess() {
                (context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
                if (new File(filePath).exists()) {
                    final Bitmap bitmap = ImageUtils.decodeScaleImage(filePath);
                    ACache.get(context.getApplicationContext()).put(htMessage.getMsgId(), bitmap);
                    htMessageImageBody.setLocalPath(filePath);
                    htMessage.setBody(htMessageImageBody);
                    (context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                    HTClient.getInstance().messageManager().saveMessage(htMessage, false);
                    context.startActivity(new Intent(context, ShowBigImageActivity.class).putExtra("localPath", filePath));
                }
            }

            @Override
            public void onFailure(String message) {
                (context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }


    private void showVoiceView(final HTMessage htMessage, final ChatViewHolder holder) {
        HTMessageVoiceBody htMessageVoiceBody = (HTMessageVoiceBody) htMessage.getBody();

        if (htMessageVoiceBody == null) {
            return;
        }
        int len = htMessageVoiceBody.getAudioDuration();

        if (len > 0) {
            holder.tvDuration.setText(len + "\"");
            holder.tvDuration.setVisibility(View.VISIBLE);
        } else {
            holder.tvDuration.setVisibility(View.INVISIBLE);
        }
        if (VoicePlayClickListener.playMsgId != null
                && VoicePlayClickListener.playMsgId.equals(htMessage.getMsgId()) && VoicePlayClickListener.isPlaying) {
            AnimationDrawable voiceAnimation;
            if (htMessage.getDirect() == HTMessage.Direct.RECEIVE) {
                holder.ivVoice.setImageResource(R.anim.voice_from_icon);
            } else {
                holder.ivVoice.setImageResource(R.anim.voice_to_icon);
            }
            voiceAnimation = (AnimationDrawable) holder.ivVoice.getDrawable();
            voiceAnimation.start();
        } else {
            if (htMessage.getDirect() == HTMessage.Direct.RECEIVE) {
                holder.ivVoice.setImageResource(R.drawable.chatfrom_voice_playing);
            } else {
                holder.ivVoice.setImageResource(R.drawable.chatto_voice_playing);
            }
        }

        if (htMessage.getDirect() == HTMessage.Direct.RECEIVE) {
            if (htMessage.getStatus() == HTMessage.Status.SUCCESS) {
                holder.ivUnread.setVisibility(View.INVISIBLE);
            } else {
                holder.ivUnread.setVisibility(View.VISIBLE);
            }
        }

        holder.reBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new VoicePlayClickListener(htMessage, chatTo, holder.ivVoice, holder.ivUnread, ChatAdapter.this, (context)).onClick(holder.reBubble);
            }
        });
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
            holder.reBubble.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    if (VoicePlayClickListener.currentPlayListener != null && VoicePlayClickListener.isPlaying) {
                        VoicePlayClickListener.currentPlayListener.stopPlayVoice();
                    }
                }
            });
        }
    }

    public static class ChatViewHolder {

        public RelativeLayout reMain;
        public RelativeLayout reBubble;

        public ImageView ivAvatar;
        public TextView tvNick;
        public TextView timeStamp;
        public ImageView ivMsgStatus;
        //文本消息,位置消息,文件消息
        public TextView tvContent;
        //图片消息,视频消息,位置消息,文件消息
        public ImageView ivContent;
        //语音消息
        public TextView tvDuration;
        public ImageView ivUnread;
        public ImageView ivVoice;
        //发送消息
        public ProgressBar progressBar;
        //已读显示
        public TextView tv_ack_msg;
        //送达显示
        public TextView tv_delivered;
    }

    /**
     * 重新发送消息
     *
     * @param htMessage
     */
    private void showReSendDialog(final HTMessage htMessage) {
        AlertDialog.Builder buidler = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.item_diaolog_gridview, null);
        TextView tv_forward = (TextView) view.findViewById(R.id.tv_forward);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        TextView tv_ok = (TextView) view.findViewById(R.id.tv_ok);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setVisibility(View.GONE);
        tv_forward.setText(R.string.prompt);
        textView.setText(R.string.resend_text);
        buidler.setView(view);
        final AlertDialog dialog = buidler.show();
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                msgs.remove(htMessage);
                notifyDataSetChanged();
                HTClient.getInstance().messageManager().deleteMessage(htMessage.getUsername(), htMessage.getMsgId());
                htMessage.setLocalTime(System.currentTimeMillis());
                htMessage.setStatus(HTMessage.Status.CREATE);
                fragment.sendMessage(htMessage);
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


}




 
