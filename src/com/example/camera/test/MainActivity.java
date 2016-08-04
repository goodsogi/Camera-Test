package com.example.camera.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * 뒤로 두 번 눌러야 표시됨
 * @author samsung
 *
 */
public class MainActivity extends Activity {

	private static final int FROM_ADD_PROFILE_IMAGE_ACTIVITY = 0;
	private static final int SCALED_WIDTH = 800;
	private static final int SCALED_HEIGHT = 800;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = new Intent(MainActivity.this,
				AddProfileImageActivity.class);
		startActivityForResult(intent, FROM_ADD_PROFILE_IMAGE_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

	
			ImageView profileImage = (ImageView) findViewById(R.id.imageview);
			Bitmap bitmap = AddProfileImageActivity.getInstance()
					.getProfileBitmap();
			
			// 서버 업로드용 이미지를 800*800으로 스케일링합니다.

			Bitmap scaledImageForServer = Bitmap.createScaledBitmap(bitmap,
					SCALED_WIDTH, SCALED_HEIGHT, true);
			// 스케일된 비트맵을 파일로 저장합니다.

			File sdcard = Environment.getExternalStorageDirectory();
			String filePath = sdcard.getAbsolutePath() + "/test.png";
			try {
				FileOutputStream out = new FileOutputStream(filePath);
				scaledImageForServer.compress(Bitmap.CompressFormat.PNG, 90,
						out);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Bitmap bitmapInSdcard = BitmapFactory.decodeFile(filePath);
			
			profileImage.setImageBitmap(bitmapInSdcard);
		}
	}

