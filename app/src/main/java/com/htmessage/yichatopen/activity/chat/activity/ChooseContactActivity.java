package com.htmessage.yichatopen.activity.chat.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.IMAction;
import com.htmessage.yichatopen.activity.BaseActivity;
import com.htmessage.yichatopen.HTConstant;
import com.htmessage.yichatopen.domain.User;
import com.htmessage.yichatopen.manager.ContactsManager;
import com.htmessage.yichatopen.utils.CommonUtils;
import com.htmessage.yichatopen.utils.OkHttpUtils;
import com.htmessage.yichatopen.utils.Param;
import com.htmessage.sdk.ChatType;
import com.htmessage.sdk.client.HTClient;
import com.htmessage.sdk.manager.HTChatManager;
import com.htmessage.sdk.model.HTMessage;
import com.htmessage.sdk.model.HTMessageImageBody;
import com.htmessage.sdk.model.HTMessageVoiceBody;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：yichat
 * 类描述：CheckPeopleActivity 描述: 选择转发人员
 * 创建人：songlijie
 * 创建时间：2017/3/18 17:04
 * 邮箱:814326663@qq.com
 */
public class ChooseContactActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<User> friends = new ArrayList<>();
    private TextView tv_group_check, tv_title;
    private ListView list;
    private ChooseContactAdapter adAdapter;
    private String forwordType;
    private String localUrl,obj,exobj;
    private HTMessage message1;
    private JSONObject object,extJSON;
    private String imagePath,msgId,toChatUsername;
    private Button btn_rtc;
    private List<HTMessage> msgList;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_check_people);
        getData();
        initView();
        initData();
        setListener();
    }

    private void initData() {
        btn_rtc.setVisibility(View.VISIBLE);
        btn_rtc.setText(R.string.str_send);
        msgList = HTClient.getInstance().messageManager().getMessageList(toChatUsername);
        extJSON = JSONObject.parseObject(exobj);
        message1 = getCopyMessage(msgId);
        getContacts();
        adAdapter = new ChooseContactAdapter(ChooseContactActivity.this, friends);
        list.setAdapter(adAdapter);
    }

    private void getData() {
        obj = getIntent().getStringExtra("obj");
        object = JSONObject.parseObject(obj);
        imagePath = object.getString("imagePath");
        forwordType = object.getString("forwordType");
        localUrl = object.getString("localPath");
        msgId = object.getString("msgId");
        toChatUsername =object.getString("toChatUsername");
        exobj =object.getString("exobj");
        getContacts();
    }

    private void getContacts() {
        friends.clear();
        // 获取本地好友列表
        Map<String, User> users = ContactsManager.getInstance().getContactList();
        Iterator<Map.Entry<String, User>> iterator = users.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, User> entry = iterator.next();

                friends.add(entry.getValue());
        }
        // 对list进行排序
        Collections.sort(friends, new PinyinComparator() {});
    }

    private void initView() {
        tv_group_check = (TextView) findViewById(R.id.tv_group_check);
        tv_title = (TextView) findViewById(R.id.tv_title);
        list = (ListView) findViewById(R.id.list);
        btn_rtc = (Button) findViewById(R.id.btn_rtc);
    }

    private void setListener() {
        tv_group_check.setOnClickListener(this);
        list.setOnItemClickListener(this);
        btn_rtc.setOnClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.toggle();
        ChooseContactAdapter.getIsSelected().put(position, checkBox.isChecked());//将CheckBox的选中状况记录下来
        // 调整选定条目
        if (checkBox.isChecked() == true) {
            users.add(adAdapter.getItem(position));
        } else {
            users.remove(adAdapter.getItem(position));
        }
    }


    public void getContactsInServer() {
        if (!HTClient.getInstance().isLogined()) {
            return;
        }
        List<Param> params = new ArrayList<Param>();
       new OkHttpUtils(this).post(params, HTConstant.URL_FriendList, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int code = jsonObject.getIntValue("code");
                switch (code) {
                    case 1:
                        JSONArray friends = jsonObject.getJSONArray("user");
                        if (friends != null || friends.size() != 0) {
                            List<User> users = new ArrayList<User>();
                            for (int i = 0; i < friends.size(); i++) {
                                JSONObject friend = friends.getJSONObject(i);
                                User user = CommonUtils.Json2User(friend);
                                users.add(user);
                            }
                            ContactsManager.getInstance().saveContactList(users);
                        }
                        break;
                }
            }

            @Override
            public void onFailure(String errorMsg) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rtc:
                if (users.size() == 0 || users == null) {
                    Toast.makeText(ChooseContactActivity.this, R.string.please_check_contant, Toast.LENGTH_SHORT).show();
                    return;
                }
                showMessageFarWordDialog(users, forwordType, localUrl);
                break;
        }
    }

    private void showMessageFarWordDialog(final ArrayList<User> users, final String forwordType, final String localUrl) {
        AlertDialog.Builder buidler = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.item_dialog_gridview, null);
        TextView tv_forward = (TextView) view.findViewById(R.id.tv_forward);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        TextView tv_ok = (TextView) view.findViewById(R.id.tv_ok);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        textView.setText(R.string.forword_always);
        if ("image".equals(forwordType) && localUrl != null) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(imagePath).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        }

        tv_forward.setText(getString(R.string.forword_people).replace("1", String.valueOf(users.size())));
        buidler.setView(view);
        final AlertDialog dialog = buidler.show();
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                for (int i = 0; i < users.size(); i++) {
                    User easeUser = users.get(i);
                    switch (forwordType) {
                        case "text":
                            HTMessage textMessage= HTMessage.createTextSendMessage(easeUser.getUsername(),localUrl);
                            sendMessage(textMessage);
                            break;
                        case "voice":
                            HTMessageVoiceBody voiceBody = (HTMessageVoiceBody) message1.getBody();
                            HTMessage voiceMSg = HTMessage.createVoiceSendMessage(easeUser.getUsername(), localUrl, voiceBody.getAudioDuration());
                            sendMessage(voiceMSg);
                            break;
                        case "image":
                            HTMessageImageBody imageBody = (HTMessageImageBody) message1.getBody();
                            HTMessage message = HTMessage.createImageSendMessage(easeUser.getUsername(), localUrl, imageBody.getSize());
                            sendMessage(message);
                            break;
                    }
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

    private void sendMessage(final HTMessage htMessage){
        htMessage.setAttributes(extJSON.toJSONString());
        htMessage.setChatType(ChatType.singleChat);
        HTClient.getInstance().chatManager().sendMessage(htMessage, new HTChatManager.HTMessageCallBack() {
            @Override
            public void onProgress() {
            }

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        htMessage.setStatus(HTMessage.Status.SUCCESS);
                        HTClient.getInstance().messageManager().saveMessage(htMessage,false);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(IMAction.ACTION_MESSAGE_FORWORD).putExtra("message",htMessage));
                    }
                });
            }

            @Override
            public void onFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        htMessage.setStatus(HTMessage.Status.FAIL);
                        HTClient.getInstance().messageManager().saveMessage(htMessage,false);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(IMAction.ACTION_MESSAGE_FORWORD).putExtra("message",htMessage));
                    }
                });
            }
        });
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        friends.clear();
        getContacts();
        if (adAdapter!=null){
            adAdapter.notifyDataSetChanged();
        }
    }


    private HTMessage getCopyMessage(String msgId) {
        for (HTMessage htMessage : msgList) {
            if (htMessage.getMsgId().equals(msgId)) {
                return htMessage;
            }
        }
        return null;
    }
    public class PinyinComparator implements Comparator<User> {

        @SuppressLint("DefaultLocale")
        @Override
        public int compare(User o1, User o2) {
            String py1 = o1.getInitialLetter();
            String py2 = o2.getInitialLetter();
            if (py1.equals(py2)) {
                return o1.getNick().compareTo(o2.getNick());
            } else {
                if ("#".equals(py1)) {
                    return 1;
                } else if ("#".equals(py2)) {
                    return -1;
                }
                return py1.compareTo(py2);
            }
        }

        private boolean isEmpty(String str) {
            return "".equals(str.trim());
        }
    }

}
