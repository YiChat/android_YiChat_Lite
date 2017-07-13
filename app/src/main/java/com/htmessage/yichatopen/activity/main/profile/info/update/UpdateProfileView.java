package com.htmessage.yichatopen.activity.main.profile.info.update;

import com.htmessage.yichatopen.activity.BaseView;

/**
 * 项目名称：HTOpen
 * 类描述：UpdateProfileView 描述:
 * 创建人：songlijie
 * 创建时间：2017/7/7 13:41
 * 邮箱:814326663@qq.com
 */
public interface UpdateProfileView extends BaseView<UpdateProfilePrestener>{
    String getDefultString();
    int getType();
    String getInputString();
    void onUpdateSuccess(String msg);
    void onUpdateFailed(String msg);
}
