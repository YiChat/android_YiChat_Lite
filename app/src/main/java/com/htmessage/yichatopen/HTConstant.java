package com.htmessage.yichatopen;

import android.os.Environment;

/**
 * Created by ustc on 2016/6/27.
 */
public class HTConstant {
//    IM服务器相关
    public static final String HOST_IM = "xxx.xxx.xxx.xxx";//示例ip: 119.125.523.153 此IP不可用
    //api服务器
    public static final String HOST_API = "http://xxx.xxx.xxx.xxx/api/";//示例ip: 119.125.523.153 此IP不可用
    //阿里云OSS信息配置
    public static final String endpoint = "oss-cn-hangzhou.aliyuncs.com";
    public static final String accessKeyId = "xxxxxxxx";
    public static final String accessKeySecret = "xxxxxxxxxxxx";
    public static final String bucket = "xxxxxxxxxx";
    public static final String baseOssUrl = "http://"+ bucket+"."+endpoint+"/";

    //登记设备id,实现单点登陆
    public static final  String DEVICE_URL_UPDATE = HOST_API+"updateDeviceId.php";
    public static final  String DEVICE_URL_GET = HOST_API+"getDeviceId.php";
    //查询更新
    public static final String URL_CHECK_UPDATE = HOST_API+"version.php";

    //应用层使用到的api接口
    public static final String URL_AVATAR= HOST_API + "upload/";
    public static final String URL_REGISTER = HOST_API + "register";//注册
    public static final String URL_LOGIN = HOST_API + "login";//登录
     public static final String URL_FriendList = HOST_API + "fetchFriends";//获取好友列表
    public static final String URL_Search_User = HOST_API + "searchUser";//查询好友
    public static final String URL_Get_UserInfo = HOST_API + "getUserInfo";//获取详情
    public static final String URL_UPDATE = HOST_API + "update";//更新
    public static final String URL_RESETPASSWORD = HOST_API + "resetPassword";//更新密码
    public static final String URL_ADD_FRIEND=HOST_API + "addFriend"; //添加好友
    public static final String URL_DELETE_FRIEND=HOST_API + "removeFriend";//删除好友
    public static final String URL_ADD_BLACKLIST=HOST_API +"addBlackList";//添加黑名单
    public static final String URL_GET_RECENTLY_PEOPLE= HOST_API + "getRecentlyUser";//获取最近上线的人
    public static final String URL_SEND_LOCAL_LOGIN_TIME= HOST_API + "updateLocalTimestamp";//获取最近上线的人


    // 缩略图处理---等高宽-请查看阿里云官方文档oss图片处理文档
    public static final String baseImgUrl_set = "?x-oss-process=image/resize,m_fill,h_300,w_300";
    //jsonobject常用key值

    public static final String JSON_KEY_NICK ="nick";
    public static final String JSON_KEY_HXID ="userId";
    public static final String JSON_KEY_FXID ="fxid";
    public static final String JSON_KEY_SEX ="sex";
    public static final String JSON_KEY_AVATAR ="avatar";
    public static final String JSON_KEY_CITY ="city";
    public static final String JSON_KEY_PASSWORD ="password";
    public static final String JSON_KEY_PROVINCE ="province";
    public static final String JSON_KEY_TEL ="tel";
    public static final String JSON_KEY_SIGN ="sign";
    public static final String JSON_KEY_ROLE ="role";

    public static final String JSON_KEY_SESSION ="session";
    //添加好友的原因
    public static final String CMD_ADD_REASON="ADD_REASON";

    //进入用户详情页传递json字符串
    public static final String KEY_USER_INFO="userInfo";
    //修改用户资料的广播
    public static final String KEY_CHANGE_TYPE="type";
    //开源地址
    public static final String GITHUBURL = "https://github.com/YiChat";
    public static final String OSCHINAURL = "http://git.oschina.net/zhangfeng_tech";
    //Pro版本体验
    public static final String YICHATPROURL = "https://www.pgyer.com/yichat_android";
    public static final String DIR_AVATAR = Environment.getExternalStorageDirectory().toString()+"/yiChat/";

}
