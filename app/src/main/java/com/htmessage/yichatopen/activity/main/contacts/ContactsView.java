package com.htmessage.yichatopen.activity.main.contacts;

import com.htmessage.yichatopen.domain.User;
import com.htmessage.yichatopen.activity.BaseView;

/**
 * Created by huangfangyi on 2017/6/28.
 * qq 84543217
 */

public interface ContactsView extends BaseView<ContactsPresenter> {
    void showItemDialog(User user);
    void showSiderBar();
    void showInvitionCount(int count);
    void showContactsCount(int count);
    void refresh();



}
