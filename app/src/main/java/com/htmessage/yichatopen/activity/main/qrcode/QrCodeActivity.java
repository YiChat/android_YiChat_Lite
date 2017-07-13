package com.htmessage.yichatopen.activity.main.qrcode;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.BaseActivity;


public class QrCodeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        setTitle(R.string.me_qrcode);
        QrCodeFragment fragment = (QrCodeFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fragment == null){
            fragment = new QrCodeFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.contentFrame,fragment);
            transaction.commit();
        }
        new QrCodePrester(fragment);
    }
}