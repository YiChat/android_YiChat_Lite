package com.htmessage.yichatopen.activity.chat;

import com.htmessage.yichatopen.activity.BasePresenter;
import com.htmessage.yichatopen.activity.BaseView;

/**
 * Created by dell on 2017/7/1.
 */

public interface ChatContract {

    interface  View extends BaseView<Presenter>{
       //长按消息体出现的dialog
       void showItemDialog();
        //标题栏
        void showTitle(String title);
        //隐藏输入法
        void hideKeyboard();
        //显示输入法
        void showKeyboard();
        //显示录音UI
        void showSpeakerMode();
        //显示输入文字状态
        void showInputMode();
        //显示更多消息类型
        void showExtendMenuItem();
        //显示表情UI
        void showEmojiView();
    }

    interface Presenter extends BasePresenter{


    }
}
