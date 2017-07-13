package com.htmessage.yichatopen.activity.login;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.htmessage.yichatopen.activity.BasePresenter;
import com.htmessage.yichatopen.activity.BaseView;

/**
 * Created by huangfangyi on 2017/6/21.
 * qq 84543217
 */

public interface LoginContract {

    interface View extends BaseView<Presenter> {

        void showDialog();

        void cancelDialog();

        String getUsername();

        String getPassword();

        void setButtonEnable();

        void setButtonDisabel();

        void showToast(int toastMsg);

        String getCountryName();

        String getCountryCode();

        Activity getBaseActivity();

        Context getBaseContext();


    }

    interface Presenter extends BasePresenter {

        void requestServer(String username, String password);

        void chooseCuntry(Context context, TextView tvCountryName, TextView tvCountryCode);

    }
}
