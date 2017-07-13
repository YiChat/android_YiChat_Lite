package com.htmessage.yichatopen.activity.login;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.htmessage.sdk.client.HTClient;
import com.htmessage.yichatopen.HTApp;
import com.htmessage.yichatopen.HTConstant;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.main.MainActivity;
import com.htmessage.yichatopen.domain.User;
import com.htmessage.yichatopen.manager.ContactsManager;
import com.htmessage.yichatopen.utils.CommonUtils;
import com.htmessage.yichatopen.utils.OkHttpUtils;
import com.htmessage.yichatopen.utils.Param;
import com.htmessage.yichatopen.utils.UpdateLastLoginTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangfangyi on 2017/6/21.
 * qq 84543217
 */

public class LoginPresenter implements LoginContract.Presenter {
   private LoginContract.View loginView;
    public LoginPresenter(LoginContract.View loginView){
        this.loginView=loginView;
        this.loginView.setPresenter(this);

    }

    @Override
    public void requestServer(String username, String password) {
        loginView.showDialog();
        List<Param> params = new ArrayList<>();
        params.add(new Param("usertel", username));
        params.add(new Param("password", password));
        new OkHttpUtils(loginView.getBaseActivity() ).post(params, HTConstant.URL_LOGIN, new OkHttpUtils.HttpCallBack() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int code = jsonObject.getInteger("code");
                switch(code){
                    case 1:
                        JSONObject userJson = jsonObject.getJSONObject("user");
                        loginIm(userJson);
                        break;
                    case -1:
                        loginView.cancelDialog();
                        loginView.showToast(R.string.Account_does_not_exist);

                        break;
                    case -2:
                        loginView.cancelDialog();
                        loginView.showToast(R.string.Incorrect_password);

                        break;
                    case -3:
                        loginView.cancelDialog();
                        loginView.showToast(R.string.Account_has_been_disabled);

                        break;
                    default:
                        loginView.cancelDialog();
                        loginView.showToast(R.string.Server_busy);

                        break;
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                loginView.cancelDialog();
            }
        });
    }

    @Override
    public void chooseCuntry(Context context, TextView tvCountryName, TextView tvCountryCode) {
            CommonUtils.showPup(context,tvCountryName,tvCountryCode);

    }





    private void loginIm(final JSONObject userJson) {
        HTClient.getInstance().login(userJson.getString(HTConstant.JSON_KEY_HXID), userJson.getString(HTConstant.JSON_KEY_PASSWORD), new HTClient.HTCallBack() {
            @Override
            public void onSuccess() {
                 if (userJson == null) {
                    return;
                }
                JSONArray friends = userJson.getJSONArray("friend");
                if (userJson.containsKey("friend")) {
                    userJson.remove("friend");

                }
                HTApp.getInstance().setUserJson(userJson);
                Map<String, User> userlist = new HashMap<String, User>();
                if (friends != null) {
                    for (int i = 0; i < friends.size(); i++) {
                        JSONObject friend = friends.getJSONObject(i);
                        User user = CommonUtils.Json2User(friend);
                        userlist.put(user.getUsername(), user);
                    }
                    List<User> users = new ArrayList<User>(userlist.values());
                    ContactsManager.getInstance().saveContactList(users);
                }
                //上传最近登录时间
                UpdateLastLoginTimeUtils.sendLocalTimeToService(loginView.getBaseContext());
               loginView.getBaseActivity(). runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginView.cancelDialog();
                        loginView.showToast(R.string.login_success);

                        Intent intent = new Intent(loginView.getBaseContext(),
                                MainActivity.class);
                        loginView.getBaseActivity().startActivity(intent);
                        loginView.getBaseActivity().finish();
                    }
                });
            }

            @Override
            public void onError() {
                loginView.getBaseActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginView.cancelDialog();
                        loginView.showToast(R.string.Login_failed);

                    }
                });
            }
        });

    }


    @Override
    public void start() {

    }
}
