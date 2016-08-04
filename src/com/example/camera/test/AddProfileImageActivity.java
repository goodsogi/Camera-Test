package com.example.camera.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class AddProfileImageActivity extends Activity {

	static AddProfileImageActivity instance;
	private static final int PICK_FROM_CAMERA = 0;
	private static final int PICK_FROM_ALBUM = 1;
	private static final int CROP_FROM_CAMERA = 2;

	private Uri mImageCaptureUri;
	private Bitmap photo;
	private String full_path;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		doTakeAlbumAction();

	}

	public static AddProfileImageActivity getInstance() {
		return instance;
	}

	/**
	 * 카메라 호출 하기
	 */
	public void doTakePhotoAction() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Crop된 이미지를 저장할 파일의 경로를 생성
		mImageCaptureUri = createSaveCropFile();
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
				mImageCaptureUri);
		startActivityForResult(intent, PICK_FROM_CAMERA);
	}

	/**
	 * 앨범 호출 하기
	 */
	public void doTakeAlbumAction() {
		// 앨범 호출
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
		startActivityForResult(intent, PICK_FROM_ALBUM);
	}

	/**
	 * Result Code
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case PICK_FROM_ALBUM: {

			// 이후의 처리가 카메라와 같으므로 일단 break없이 진행합니다.
			// 실제 코드에서는 좀더 합리적인 방법을 선택하시기 바랍니다.
			mImageCaptureUri = data.getData();
			File original_file = getImageFile(mImageCaptureUri);

			mImageCaptureUri = createSaveCropFile();
			File cpoy_file = new File(mImageCaptureUri.getPath());

			// SD카드에 저장된 파일을 이미지 Crop을 위해 복사한다.
			copyFile(original_file, cpoy_file);
		}

		case PICK_FROM_CAMERA: {

			// 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.
			// 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.

			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(mImageCaptureUri, "image/*");

			// Crop한 이미지를 저장할 Path
			intent.putExtra("output", mImageCaptureUri);

			// Return Data를 사용하면 번들 용량 제한으로 크기가 큰 이미지는
			// 넘겨 줄 수 없다.
			// intent.putExtra("return-data", true);
			startActivityForResult(intent, CROP_FROM_CAMERA);

			break;
		}

		case CROP_FROM_CAMERA: {

			// Crop 된 이미지를 넘겨 받습니다.

			full_path = mImageCaptureUri.getPath();
//			String photo_path = full_path.substring(4, full_path.length());
//
//			photo = BitmapFactory.decodeFile(photo_path);
			photo = BitmapFactory.decodeFile(full_path);
			

			break;
		}
		}
	}

	/**
	 * Crop된 이미지가 저장될 파일을 만든다.
	 * 
	 * @return Uri
	 */
	private Uri createSaveCropFile() {
		Uri uri;
		String url = "tmp_" + String.valueOf(System.currentTimeMillis())
				+ ".jpg";
		uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				url));
		return uri;
	}

	/**
	 * 선택된 uri의 사진 Path를 가져온다. uri 가 null 경우 마지막에 저장된 사진을 가져온다.
	 * 
	 * @param uri
	 * @return
	 */
	private File getImageFile(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		if (uri == null) {
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		}

		Cursor mCursor = getContentResolver().query(uri, projection, null,
				null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
		if (mCursor == null || mCursor.getCount() < 1) {
			return null; // no cursor or no record
		}
		int column_index = mCursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		mCursor.moveToFirst();

		String path = mCursor.getString(column_index);

		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}

		return new File(path);
	}

	/**
	 * 파일 복사
	 * 
	 * @param srcFile
	 *            : 복사할 File
	 * @param destFile
	 *            : 복사될 File
	 * @return
	 */
	public static boolean copyFile(File srcFile, File destFile) {
		boolean result = false;
		try {
			InputStream in = new FileInputStream(srcFile);
			try {
				result = copyToFile(in, destFile);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			result = false;
		}
		return result;
	}

	/**
	 * Copy data from a source stream to destFile. Return true if succeed,
	 * return false if failed.
	 */
	private static boolean copyToFile(InputStream inputStream, File destFile) {
		try {
			OutputStream out = new FileOutputStream(destFile);
			try {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) >= 0) {
					out.write(buffer, 0, bytesRead);
				}
			} finally {
				out.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 프로필 이미지 가져감 
	 * @return
	 */
	public Bitmap getProfileBitmap() {
		return photo;
	}
	
	/**
	 * 프로필 이미지 url 가져감 
	 * @return
	 */
	public String getProfileBitmapUrl() {
		return full_path;
	}
	
	/**
	 * confirm 버튼 클릭 처리
	 * 
	 * @param v
	 */
	public void confirmProcess(View v) {
		onBackPressed();

	}

	/**
	 * 뒤로가기 처리
	 * 
	 * @param v
	 */
	public void goBack(View v) {
		onBackPressed();
	}
}
	

