package com.htmessage.yichatopen.activity.main;

import com.htmessage.yichatopen.activity.BaseView;
import com.htmessage.yichatopen.activity.main.contacts.FragmentContacts;
import com.htmessage.yichatopen.activity.main.conversation.ConversationFragment;

/**
 * Created by huangfangyi on 2017/6/25.
 * qq 84543217
 */

public interface MainView extends BaseView<MainPrestener>,ConversationFragment.NewMeesageListener,FragmentContacts.ContactsListener {

    void showConflicDialog();

     void showUpdateDialog( String message,String url,String isForce);

}
