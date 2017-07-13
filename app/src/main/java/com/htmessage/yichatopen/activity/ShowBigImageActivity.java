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
package com.htmessage.yichatopen.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.htmessage.yichatopen.R;
import com.htmessage.yichatopen.widget.photoview.PhotoView;

import java.io.File;

/**
 * download and show original image
 * 
 */
public class ShowBigImageActivity extends  BaseActivity {

	private PhotoView image;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_show_big_image);
		super.onCreate(savedInstanceState);
        String localPath=this.getIntent().getStringExtra("localPath");
		Uri uri=Uri.fromFile(new File(localPath));
		image = (PhotoView) findViewById(R.id.image);
//		image.setImageURI(uri);
		Glide.with(ShowBigImageActivity.this).load(uri.getPath()).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.default_image).into(image);
		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}


	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		finish();
	}
}
