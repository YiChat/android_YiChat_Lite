package com.htmessage.yichatopen.activity.login;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;

import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.BaseActivity;
import com.htmessage.yichatopen.runtimepermissions.PermissionsManager;
import com.htmessage.yichatopen.runtimepermissions.PermissionsResultAction;

/**
 * Login screen
 */
public class LoginActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        hideBackView();
        setTitle(R.string.login_by_mobile);
        requestPermissions();
        LoginFragment loginFragment =
                (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (loginFragment == null) {
            // Create the fragment
            loginFragment =new  LoginFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.contentFrame, loginFragment);
            transaction.commit();
        }

        LoginPresenter presenter=new LoginPresenter(loginFragment);

    }







    @TargetApi(23)
    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
            }

            @Override
            public void onDenied(String permission) {
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }


}
