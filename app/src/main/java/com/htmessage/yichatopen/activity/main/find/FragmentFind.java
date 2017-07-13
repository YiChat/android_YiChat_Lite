package com.htmessage.yichatopen.activity.main.find;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.ScanCaptureActivity;
import com.htmessage.yichatopen.activity.main.find.recentlypeople.PeopleRecentlyActivity;


public class FragmentFind extends Fragment implements View.OnClickListener {
    private RelativeLayout rl_friends, re_qrcode, rl_near;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null
                && savedInstanceState.getBoolean("isConflict", false))
            return;
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        rl_friends.setOnClickListener(this);
        re_qrcode.setOnClickListener(this);
         rl_near.setOnClickListener(this);

    }

    private void initData() {

    }

    private void initView() {
        rl_friends = (RelativeLayout) getView().findViewById(R.id.rl_friends);
        re_qrcode = (RelativeLayout) getView().findViewById(R.id.re_qrcode);

        rl_near = (RelativeLayout) getView().findViewById(R.id.rl_near);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_friends://志工圈
                break;
            case R.id.re_qrcode: //扫一扫
                startActivity(new Intent(getActivity(), ScanCaptureActivity.class));
                break;

            case R.id.rl_near://最近在线
                startActivity(new Intent(getActivity(), PeopleRecentlyActivity.class));
                 break;
        }
    }
}
