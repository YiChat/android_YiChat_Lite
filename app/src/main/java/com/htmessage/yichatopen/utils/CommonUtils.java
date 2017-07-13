/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.htmessage.yichatopen.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.activity.country.CountryCodeUtil;
import com.htmessage.yichatopen.activity.country.CountryComparator;
import com.htmessage.yichatopen.activity.country.CountrySortAdapter;
import com.htmessage.yichatopen.activity.country.CountrySortModel;
import com.htmessage.yichatopen.activity.country.GetCountryNameSort;
import com.htmessage.yichatopen.activity.country.SideBar;
import com.htmessage.yichatopen.HTConstant;
import com.htmessage.yichatopen.domain.User;
import com.github.promeg.pinyinhelper.Pinyin;

public class CommonUtils {
	private static final String TAG = "CommonUtils";

	public static User Json2User(JSONObject userJson) {
		User user = new User(userJson.getString(HTConstant.JSON_KEY_HXID));
		user.setNick(userJson.getString(HTConstant.JSON_KEY_NICK));
		user.setAvatar(userJson.getString(HTConstant.JSON_KEY_AVATAR));
		user.setUserInfo(userJson.toJSONString());
		CommonUtils.setUserInitialLetter(user);
		return user;
	}
	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
			}
		}

		return false;
	}

	/**
	 * check if sdcard exist
	 * 
	 * @return
	 */
	public static boolean isSdcardExist() {
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			return true;
		else
			return false;
	}
	


    
    static String getString(Context context, int resId){
        return context.getResources().getString(resId);
    }
	
	/**
	 * get top activity
	 * @param context
	 * @return
	 */
	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return runningTaskInfos.get(0).topActivity.getClassName();
		else
			return "";
	}
	
	/**
     * set initial letter of according user's nickname( username if no nickname)
     * 
     * @param
     * @param user
     */
    public static void setUserInitialLetter(User user) {
        final String DefaultLetter = "#";
        String letter = DefaultLetter;
        if ( !TextUtils.isEmpty(user.getNick()) ) {
            letter = Pinyin.toPinyin(user.getNick().toCharArray()[0]);
            user.setInitialLetter(letter.toUpperCase().substring(0,1));
			if ("123456789".contains(user.getInitialLetter())) {
				user.setInitialLetter("#");
			}
            return;
        } 
        if (letter == DefaultLetter && !TextUtils.isEmpty(user.getUsername())) {
			letter = Pinyin.toPinyin(user.getUsername().toCharArray()[0]);
        }
        user.setInitialLetter(letter.substring(0,1));
		if ("123456789".contains(user.getInitialLetter())) {
			user.setInitialLetter("#");
		}
    }

	/**
	 * 获取国家代码
	 *
	 * @param context     上下文对象
	 * @param country     显示国家的textview
	 * @param countryCode 显示国家代码的textview
	 */
	public static void showPup(final Context context, final TextView country, final TextView countryCode) {
		final boolean cn = context.getResources().getConfiguration().locale.getCountry().equals("CN");
		final List<CountrySortModel> countryList = CountryCodeUtil.getCountryList(context, cn);
		//获得pup的view
		View view = LayoutInflater.from(context).inflate(R.layout.layout_pup, null, false);
		final ImageView iv_back = (ImageView) view.findViewById(R.id.iv_back);
		final TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
		tv_title.setText(R.string.country);
		final EditText country_et_search = (EditText) view.findViewById(R.id.country_et_search);
		final ImageView country_iv_cleartext = (ImageView) view.findViewById(R.id.country_iv_cleartext);
		final ListView ll_country = (ListView) view.findViewById(R.id.country_lv_list);
		final TextView country_dialog = (TextView) view.findViewById(R.id.country_dialog);
		final SideBar country_sidebar = (SideBar) view.findViewById(R.id.country_sidebar);
		country_sidebar.setTextView(country_dialog);
		final CountrySortAdapter adapter = new CountrySortAdapter(context, countryList);
		ll_country.setAdapter(adapter);
		DisplayMetrics dm = new DisplayMetrics();
		//取得窗口属性
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		//窗口的宽度
		int screenWidth = dm.widthPixels;
		//窗口高度
		int screenHeight = dm.heightPixels / 2;
		//设置window的宽高   1 window的布局 2、window的宽  3、window的高  4、window是否获取焦点
//        final PopupWindow window = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, screenHeight, true);
		final PopupWindow window = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true);
		//设置window背景色
		window.setBackgroundDrawable(new ColorDrawable(0x00000000));
		//设置可以获取焦点，否则弹出菜单中的EditText是无法获取输入的
		window.setFocusable(true);
		//设置键盘不遮盖
		window.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		//设置window动画
//        window.setAnimationStyle(R.style.custom_pup_style);
		//设置window在底部显示
		window.showAtLocation(view, Gravity.BOTTOM, 0, 0);

		country_iv_cleartext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				country_et_search.setText("");
				Collections.sort(countryList, new CountryComparator());
				adapter.updateListView(countryList);
			}
		});

		country_et_search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String searchContent = country_et_search.getText().toString();
				if (searchContent.equals("")) {
					country_iv_cleartext.setVisibility(View.INVISIBLE);
				} else {
					country_iv_cleartext.setVisibility(View.VISIBLE);
				}

				if (searchContent.length() > 0) {
					// 按照输入内容进行匹配
					ArrayList<CountrySortModel> fileterList = (ArrayList<CountrySortModel>) new GetCountryNameSort()
							.search(searchContent, countryList);

					adapter.updateListView(fileterList);
				} else {
					adapter.updateListView(countryList);
				}
				ll_country.setSelection(0);
			}
		});

		// 右侧sideBar监听
		country_sidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					ll_country.setSelection(position);
				}
			}
		});

		ll_country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				window.dismiss();
				String countryName = null;
				String countryNumber = null;
				String searchContent = country_et_search.getText().toString();
				if (searchContent.length() > 0) {
					// 按照输入内容进行匹配
					ArrayList<CountrySortModel> fileterList = (ArrayList<CountrySortModel>) new GetCountryNameSort()
							.search(searchContent, countryList);
					//获取国家名字及代码
					countryName = fileterList.get(i).countryName;
					countryNumber = fileterList.get(i).countryNumber;
				} else {
					//获取国家名字及代码
					countryName = countryList.get(i).countryName;
					countryNumber = countryList.get(i).countryNumber;
				}
				country.setText(countryName);
				countryCode.setText(countryNumber);
				Log.e(TAG, "countryName: + " + countryName + "countryNumber: " + countryNumber);
			}
		});
		iv_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				window.dismiss();
			}
		});
	}



}
