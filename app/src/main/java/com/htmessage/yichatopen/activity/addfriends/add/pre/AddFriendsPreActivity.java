package com.htmessage.yichatopen.activity.addfriends.add.pre;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.htmessage.yichatopen.HTApp;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.BaseActivity;
import com.htmessage.yichatopen.activity.main.qrcode.QrCodeActivity;
import com.htmessage.yichatopen.activity.ScanCaptureActivity;
import com.htmessage.yichatopen.HTConstant;
import com.htmessage.yichatopen.activity.addfriends.add.next.AddFriendsNextActivity;

public class AddFriendsPreActivity extends BaseActivity implements OnClickListener{
    private TextView tv_search,tv_fxid;
    private RelativeLayout rl_leida,rl_jianqun,rl_sacn,rl_lianxiren,rl_ggh;
    private ImageView iv_scode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriends_pre);
        initUI();
        setOnClick();
    }

    private void initUI() {
        tv_search = (TextView) findViewById(R.id.tv_search);
        tv_fxid = (TextView) findViewById(R.id.tv_fxid);
        rl_leida = (RelativeLayout) findViewById(R.id.rl_leida);
        rl_jianqun = (RelativeLayout) findViewById(R.id.rl_jianqun);
        rl_sacn = (RelativeLayout) findViewById(R.id.rl_sacn);
        rl_lianxiren = (RelativeLayout) findViewById(R.id.rl_lianxiren);
        rl_ggh = (RelativeLayout) findViewById(R.id.rl_ggh);
        iv_scode = (ImageView) findViewById(R.id.iv_scode);

        JSONObject userJson = HTApp.getInstance().getUserJson();
        if (userJson!=null){
            String fxid = userJson.getString(HTConstant.JSON_KEY_FXID);
            if (!TextUtils.isEmpty(fxid)){
                tv_fxid.setText(getString(R.string.me_yichat_number)+fxid);
            }else{
                tv_fxid.setText(getString(R.string.me_yichat_number)+getString(R.string.not_set));
            }
        }
    }
    private void setOnClick() {
        tv_search.setOnClickListener(this);
        rl_leida.setOnClickListener(this);
        rl_jianqun.setOnClickListener(this);
        rl_sacn.setOnClickListener(this);
        rl_lianxiren.setOnClickListener(this);
        rl_ggh.setOnClickListener(this);
        iv_scode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.tv_search:
                startActivity(new Intent(AddFriendsPreActivity.this,AddFriendsNextActivity.class));
                break;
            case R.id.rl_leida:
//                startActivity(new Intent(AddFriendsPreActivity.this,RadarActivity.class));
                break;
            case R.id.rl_jianqun:

                break;
            case R.id.rl_sacn:
                startActivity(new Intent(AddFriendsPreActivity.this,ScanCaptureActivity.class));
                break;
            case R.id.rl_lianxiren:

                break;
            case R.id.rl_ggh:

                break;
            case R.id.iv_scode:
                startActivity(new Intent(AddFriendsPreActivity.this,QrCodeActivity.class));
                break;
        }
    }
}
