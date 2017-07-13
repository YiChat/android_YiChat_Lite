package com.htmessage.yichatopen.activity.addfriends.add.next;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.BaseActivity;


public class AddFriendsNextActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_input);
        AddFriendNextFragment fragment = (AddFriendNextFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fragment == null){
            fragment = new AddFriendNextFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.contentFrame,fragment);
            transaction.commit();
        }
        new AddFriendNextPrestener(fragment);
    }
}