package com.htmessage.yichatopen.activity.chat.weight.loadmore;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Field;

public class DensityUtil {

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}


	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	public static float getScreenDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}

	public static int getStatusBarHeight(Context ctx) {
		Class<?> c = null;

		Object obj = null;

		Field field = null;

		int x = 0, sbar = 0;

		try {

			c = Class.forName("com.android.internal.R$dimen");

			obj = c.newInstance();

			field = c.getField("status_bar_height");

			x = Integer.parseInt(field.get(obj).toString());

			sbar = ctx.getResources().getDimensionPixelSize(x);

		} catch (Exception e1) {

			e1.printStackTrace();

		}

		return sbar;
	}
	
	public static int getScreenHeightWithoutTitlebar(Context ctx) {
		int[] screenWidthAndHeight = getScreenWidthAndHeight(ctx);
		return screenWidthAndHeight[1] -getStatusBarHeight(ctx)- dip2px(ctx, 48);
	}

	public static int[] getScreenWidthAndHeight(Context ctx) {
		WindowManager mWm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);

		DisplayMetrics dm = new DisplayMetrics();
		// 获取屏幕信息
		mWm.getDefaultDisplay().getMetrics(dm);

		int screenWidth = dm.widthPixels;

		int screenHeigh = dm.heightPixels;

		return new int[] { screenWidth, screenHeigh };
	}
	
	public static void addOnSoftKeyBoardVisibleListener(final Activity activity, final ScrollView scrollView) {
		final View decorView = activity.getWindow().getDecorView();
		decorView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Rect rect = new Rect();
						decorView.getWindowVisibleDisplayFrame(rect);
						int displayHight = rect.bottom - rect.top;
						int hight = decorView.getHeight();
						boolean visible = (double) displayHight / hight < 0.8;// 决断键盘是弹�?
						System.out.println("===监听" + visible);
						if (visible) {

							Handler mHandler = new Handler();
							mHandler.postDelayed(new Runnable() {
								@Override
								public void run() {
//									scrollView
//											.fullScroll(ScrollView.FOCUS_DOWN);// ScrollView滚动到底
									scrollView.scrollTo(0, DensityUtil.dip2px(activity, 167));
								}
							}, 50);
						}
					}
				});
	}
 	public static String getEllipsisedText(TextView textView) {
	    try {
			String text = textView.getText().toString();
			int lines = textView.getLineCount();
			int width = textView.getWidth();
			int len = text.length();
			
			Log.d("Test", "text-->" + text + "; lines-->" + lines + "; width-->" + width + ";len-->" + len);
			TextUtils.TruncateAt where = TextUtils.TruncateAt.END;
			TextPaint paint = textView.getPaint();

			StringBuffer result = new StringBuffer();

			int spos = 0, cnt, tmp, hasLines = 0;

			while(hasLines < lines - 1) {
			    cnt = paint.breakText(text, spos, len, true, width, null);
			    if(cnt >= len - spos) {
			        result.append(text.substring(spos));
			        break;
			    }

			    tmp = text.lastIndexOf('\n', spos + cnt - 1);

			    if(tmp >= 0 && tmp < spos + cnt) {
			        result.append(text.substring(spos, tmp + 1));
			        spos += tmp + 1;
			    }
			    else {
			        tmp = text.lastIndexOf(' ', spos + cnt - 1);
			        if(tmp >= spos) {
			            result.append(text.substring(spos, tmp + 1));
			            spos += tmp + 1;
			        }
			        else {
			            result.append(text.substring(spos, cnt));
			            spos += cnt;
			        }
			    }

			    hasLines++;
			}

			if(spos < len) {
			    result.append(TextUtils.ellipsize(text.subSequence(spos, len), paint, (float) width, where));
			}

			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    return null;
	}
}
