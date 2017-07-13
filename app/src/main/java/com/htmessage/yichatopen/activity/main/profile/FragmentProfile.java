package com.htmessage.yichatopen.activity.main.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.htmessage.yichatopen.HTApp;
import com.htmessage.yichatopen.HTConstant;
import com.htmessage.yichatopen.IMAction;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.main.qrcode.QrCodeActivity;
import com.htmessage.yichatopen.activity.main.profile.info.profile.ProfileActivity;
import com.htmessage.yichatopen.activity.SettingsActivity;

public class FragmentProfile extends Fragment implements View.OnClickListener {
    private InfoChangedListener listener;
    private ImageView ivAvatar;
    private TextView tvNick;
    private TextView tvFxid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getData();
        setListener();
        initView(HTApp.getInstance().getUserJson());
    }

    private void getData() {
        IntentFilter intent = new IntentFilter(IMAction.ACTION_UPDATE_INFO);
        listener = new InfoChangedListener();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(listener, intent);

    }

    private void initView(JSONObject jsonObject) {

        ivAvatar = (ImageView) getView().findViewById(R.id.iv_avatar);
        tvNick = (TextView) getView().findViewById(R.id.tv_name);

        tvFxid = (TextView) getView().findViewById(R.id.tv_fxid);
        String avatarUrl = jsonObject.getString(HTConstant.JSON_KEY_AVATAR);
        Glide.with(getActivity()).load(avatarUrl).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.default_avatar).into(ivAvatar);
        tvNick.setText(jsonObject.getString(HTConstant.JSON_KEY_NICK));
        String fxid = jsonObject.getString(HTConstant.JSON_KEY_FXID);
        if (!TextUtils.isEmpty(fxid)) {
            tvFxid.setText(getString(R.string.app_id) + fxid);
        } else {
            tvFxid.setText(getString(R.string.app_id) + getString(R.string.not_set));
        }
    }

    private void setListener() {
        getView().findViewById(R.id.re_myinfo).setOnClickListener(this);
        getView().findViewById(R.id.re_setting).setOnClickListener(this);
        getView().findViewById(R.id.re_xiangce).setOnClickListener(this);
        getView().findViewById(R.id.rl_qrcode).setOnClickListener(this);

        getView().findViewById(R.id.re_pro_test).setOnClickListener(this);
        getView().findViewById(R.id.re_github).setOnClickListener(this);
        getView().findViewById(R.id.re_oschina).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_myinfo:
                startActivity(new Intent(getActivity(), ProfileActivity.class));
                break;

            case R.id.re_setting:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;

            case R.id.re_xiangce:
                break;
            case R.id.rl_qrcode: //我的二维码
                startActivity(new Intent(getActivity(), QrCodeActivity.class));
                break;
            case R.id.re_pro_test://pro版本
                toStartBrowser(HTConstant.YICHATPROURL);
                break;
            case R.id.re_github:
                toStartBrowser(HTConstant.GITHUBURL);
                break;
            case R.id.re_oschina:
                toStartBrowser(HTConstant.OSCHINAURL);
                break;

        }
    }

    private void toStartBrowser(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        startActivity(intent);
    }


    private class InfoChangedListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (IMAction.ACTION_UPDATE_INFO.equals(intent.getAction())) {
                String type = intent.getStringExtra(HTConstant.KEY_CHANGE_TYPE);
                if (HTConstant.JSON_KEY_AVATAR.equals(type)) {
                    String avatar = intent.getStringExtra(HTConstant.JSON_KEY_AVATAR);
                    Glide.with(getActivity()).load(avatar).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.default_avatar).into(ivAvatar);
                } else if (HTConstant.JSON_KEY_FXID.equals(type)) {
                    String fxid = intent.getStringExtra(HTConstant.JSON_KEY_FXID);
                    tvFxid.setText(getString(R.string.app_id) + fxid);
                } else if (HTConstant.JSON_KEY_NICK.equals(type)) {
                    String nick = intent.getStringExtra(HTConstant.JSON_KEY_NICK);
                    tvNick.setText(nick);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(listener);
        }
    }
}
