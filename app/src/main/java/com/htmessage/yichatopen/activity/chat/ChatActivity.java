package com.htmessage.yichatopen.activity.chat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.htmessage.sdk.utils.MessageUtils;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.BaseActivity;
import com.htmessage.yichatopen.manager.MyNotification;
import com.htmessage.yichatopen.domain.User;
import com.htmessage.yichatopen.manager.ContactsManager;
import com.htmessage.yichatopen.activity.chat.activity.ChatSettingActivity;
import com.htmessage.yichatopen.activity.main.MainActivity;
import java.util.List;

/**
 *
 */
public class ChatActivity extends BaseActivity {
    public static ChatActivity activityInstance;
    private ChatFragment chatFragment;
    public String toChatUsername;
    public int chatType;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_base);
        activityInstance = this;
        toChatUsername = getIntent().getExtras().getString("userId");
        chatType = getIntent().getExtras().getInt("chatType", MessageUtils.CHAT_SINGLE);
        if (chatType == MessageUtils.CHAT_SINGLE) {
            User user = ContactsManager.getInstance().getContactList().get(toChatUsername);
            setTitle(user.getNick());
            showRightView(R.drawable.icon_setting_single, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ChatActivity.this, ChatSettingActivity.class).putExtra("userId", toChatUsername));
                }
            });

        }
        chatFragment = new ChatFragment();
        chatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, chatFragment).commit();
        MyNotification.getInstance().cancel(Integer.parseInt(toChatUsername));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityInstance = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {

        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        chatFragment.onBackPressed();
        if (isSingleActivity(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }


    public boolean isSingleActivity(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List list = activityManager.getRunningTasks(1);
        return ((ActivityManager.RunningTaskInfo) list.get(0)).numRunning == 1;
    }

    public String getToChatUsername() {
        return toChatUsername;
    }
}
