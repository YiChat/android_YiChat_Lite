package com.htmessage.yichatopen.activity.main.conversation;

import com.htmessage.sdk.model.HTConversation;
import com.htmessage.yichatopen.activity.BaseView;

/**
 * Created by huangfangyi on 2017/6/27.
 * qq 84543217
 */

public interface ConversationView extends BaseView<ConversationPresenter>{

    void showItemDialog(HTConversation htConversation);
    void refresh();
}
