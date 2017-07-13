package com.htmessage.yichatopen.activity.chat.weight;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.htmessage.yichatopen.HTClientHelper;
import com.htmessage.yichatopen.R;
import com.htmessage.sdk.ChatType;
import com.htmessage.sdk.client.HTClient;
import com.htmessage.sdk.model.HTMessageVoiceBody;
import com.htmessage.sdk.model.HTMessage;
import com.htmessage.yichatopen.manager.SettingsManager;
import com.htmessage.yichatopen.utils.HTMessageUtils;

import java.io.File;

/**
 * Created by huangfangyi on 2016/12/5.
 * qq 84543217
 */

public class VoicePlayClickListener implements View.OnClickListener {
    private static final String TAG = "VoicePlayClickListener";
    HTMessage message;
    ImageView voiceIconView;

    private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    ImageView iv_read_status;
    Activity activity;
    private ChatType chatType;
    private BaseAdapter adapter;

    public static boolean isPlaying = false;
    public static VoicePlayClickListener currentPlayListener = null;
    public static String playMsgId;
    String chatTo;

    public VoicePlayClickListener(HTMessage message, String chatTo, ImageView v, ImageView iv_read_status, BaseAdapter adapter, Activity context) {
        this.message = message;
        //     voiceBody = (EMVoiceMessageBody) message.getBody();
        this.iv_read_status = iv_read_status;
        this.adapter = adapter;
        voiceIconView = v;
        this.activity = context;
        this.chatType = message.getChatType();
        this.chatTo = chatTo;
    }

    public void stopPlayVoice() {
        voiceAnimation.stop();
        if (message.getDirect() == HTMessage.Direct.RECEIVE) {
            voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
        } else {
            voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
        }
        // stop play voice
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
        playMsgId = null;
        adapter.notifyDataSetChanged();
    }

    public void playVoice(String filePath) {
        if (!(new File(filePath).exists())) {
            return;
        }
        playMsgId = message.getMsgId();
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = new MediaPlayer();
        if (SettingsManager.getInstance().getSettingMsgSpeaker()) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        } else {
            audioManager.setSpeakerphoneOn(false);// 关闭扬声器
            // 把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    stopPlayVoice(); // stop animation
                }

            });
            isPlaying = true;
            currentPlayListener = this;
            mediaPlayer.start();
            showAnimation();

            // 如果是接收的消息
            if (message.getDirect() == HTMessage.Direct.RECEIVE) {
//                if (!message.isAcked() && chatType == ChatType.Chat) {
//                    // 告知对方已读这条消息
//                    EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
//                }
                if (message.getStatus() != HTMessage.Status.SUCCESS && iv_read_status != null && iv_read_status.getVisibility() == View.VISIBLE) {
                    // 隐藏自己未播放这条语音消息的标志
                    iv_read_status.setVisibility(View.INVISIBLE);
                    HTClient.getInstance().messageManager().updateSuccess(message);
//                    message.setListened(true);
//                    EMClient.getInstance().chatManager().setMessageListened(message);
                }

            }

        } catch (Exception e) {
            System.out.println();
        }
    }

    // show the voice playing animation
    private void showAnimation() {
        // play voice, and start animation
        if (message.getDirect() == HTMessage.Direct.RECEIVE) {
            voiceIconView.setImageResource(R.anim.voice_from_icon);
        } else {
            voiceIconView.setImageResource(R.anim.voice_to_icon);
        }
        voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
        voiceAnimation.start();
    }

    @Override
    public void onClick(View v) {
        String st = activity.getResources().getString(R.string.Is_download_voice_click_later);
        if (isPlaying) {
            if (playMsgId != null && playMsgId.equals(message.getMsgId())) {
                currentPlayListener.stopPlayVoice();
                return;
            }
            currentPlayListener.stopPlayVoice();
        }
        HTMessageVoiceBody htMessageVoiceBody = (HTMessageVoiceBody) message.getBody();
        String loaclPath = htMessageVoiceBody.getLocalPath();
        if (message.getDirect() == HTMessage.Direct.SEND) {

            //   Log.d("getBodyJSON--->",message.getBodyJSON().getString(MessageUtils.LOCAL_PATH));
            // for sent msg, we will try to play the voice file directly
            playVoice(loaclPath);
        } else {

            if (!TextUtils.isEmpty(loaclPath)) {
                if (message.getStatus() == HTMessage.Status.SUCCESS) {
                    File file = new File(loaclPath);
                    if (file.exists() && file.isFile())
                        playVoice(loaclPath);
                    else
                        Log.e(TAG, "file not exist");

                }
            } else {
//                if(message.getBodyJSON().containsKey(MessageUtils.REMOTE_PATH)){

//                    String remotePath=message.getBodyJSON().getString(MessageUtils.REMOTE_PATH);
                downLoadVoiceFileFromServer(message);
                //}


            }

//
//                else if (message.status() == HTMessage.Status.INPROGRESS) {
//                    Toast.makeText(activity, st, Toast.LENGTH_SHORT).show();
//                } else if (message.status() == HTMessage.Status.FAIL) {
//                    Toast.makeText(activity, st, Toast.LENGTH_SHORT).show();
//                    new AsyncTask<Void, Void, Void>() {
//
//                        @Override
//                        protected Void doInBackground(Void... params) {
//                            EMClient.getInstance().chatManager().downloadAttachment(message);
//                            ImageUtils.getScaledImage()
//                            return null;
//                        }
//
//                        @Override
//                        protected void onPostExecute(Void result) {
//                            super.onPostExecute(result);
//                            adapter.notifyDataSetChanged();
//                        }
//
//                    }.execute();
//
//                }


        }


    }


    private void downLoadVoiceFileFromServer(final HTMessage htMessage) {

        HTMessageUtils.loadMessageFile(htMessage, chatTo, activity,
         new HTMessageUtils.CallBack() {
            @Override
            public void error() {

            }

            @Override
            public void completed(String localPath) {
                playVoice(localPath);
                HTMessageVoiceBody htMessageVoiceBody = (HTMessageVoiceBody) htMessage.getBody();
                //JSONObject jsonObject=htMessage.getBodyJSON();
                // jsonObject.put(MessageUtils.LOCAL_PATH,localPath);
                htMessageVoiceBody.setLocalPath(localPath);
                htMessage.setBody(htMessageVoiceBody);
                HTClient.getInstance().messageManager().updateSuccess(htMessage);
            }


        });

//
//        final JSONObject jsonObject = htMessage.getBodyJSON();
//        if (!jsonObject.containsKey(MessageUtils.REMOTE_PATH) || !jsonObject.containsKey(MessageUtils.FILE_NAME)) {
//
//            return;
//        }
//        final ProgressDialog progressDialog = new ProgressDialog(activity);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.setMessage("正在加载");
//        progressDialog.show();
//        HTPathUtils pathUtils = new HTPathUtils(chatTo, activity);
//        String remotePath = jsonObject.getString(MessageUtils.REMOTE_PATH);
//        String fileName = jsonObject.getString(MessageUtils.FILE_NAME);
//        final String filePath = pathUtils.getImagePath().getAbsolutePath() + fileName;
//        Log.d("filePath22--->", filePath);
//        FileDownloader.getImpl().create(remotePath)
//                .setPath(filePath)
//                .setListener(new FileDownloadListener() {
//                    @Override
//                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//                    }
//
//                    @Override
//                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
//                    }
//
//                    @Override
//                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//                    }
//
//                    @Override
//                    protected void blockComplete(BaseDownloadTask task) {
//                    }
//
//                    @Override
//                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
//                    }
//
//                    @Override
//                    protected void completed(BaseDownloadTask task) {
//                        //下载完成后,更新本地的图片cache,已经消息体的localPath
//                        // jsonObject.put(MessageUtils.IMAGE_LOCAL_PATH,filePath);
//                        ((Activity) context).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                progressDialog.dismiss();
//
//                            }
//                        });
//                        if (new File(filePath).exists()) {
//                            final Bitmap bitmap = com.fanxin.tigase.utils.ImageUtils.decodeScaleImage(filePath, 240, 240);
//                            ACache.get(context).put(htMessage.getMsgId(), bitmap);
//                            jsonObject.put(MessageUtils.LOCAL_PATH, filePath);
//                            htMessage.setBody(jsonObject.toJSONString());
//                            ((Activity) context).runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    notifyDataSetChanged();
//                                }
//                            });
//                            MessageManager.getInstance().saveMessage(chatTo, htMessage, false);
//                            context.startActivity(new Intent(context, ShowBigImageActivity.class).putExtra("localPath",filePath));
//                        }
//
//
//
//                    }
//
//                    @Override
//                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//                    }
//
//                    @Override
//                    protected void error(BaseDownloadTask task, Throwable e) {
//                        ((Activity) context).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                progressDialog.dismiss();
//                            }
//                        });
//                    }
//
//                    @Override
//                    protected void warn(BaseDownloadTask task) {
//                    }
//                }).start();


//        LinearLayout linearLayout;
//        linearLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//            @Override
//            public void onViewAttachedToWindow(View v) {
//
//            }
//
//            @Override
//            public void onViewDetachedFromWindow(View v) {
//
//            }
//        });

    }

}
