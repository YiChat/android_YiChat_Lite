package com.htmessage.yichatopen.activity.addfriends.newfriend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.addfriends.add.next.AddFriendsNextActivity;
import com.htmessage.yichatopen.activity.BaseActivity;

/**
 * 申请与通知
 */
public class NewFriendsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        setTitle(R.string.new_friend);
        NewFriendFragment newFriendFragment =
                (NewFriendFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (newFriendFragment == null) {
            // Create the fragment
            newFriendFragment =new  NewFriendFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.contentFrame, newFriendFragment);
            transaction.commit();
        }
        final NewFriendPrestener presenter=new NewFriendPrestener(newFriendFragment);
        showRightView(R.drawable.add_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewFriendsActivity.this,
                        AddFriendsNextActivity.class));
            }
        });
    }

    public void back(View v) {
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
        super.onBackPressed();
    }
}
