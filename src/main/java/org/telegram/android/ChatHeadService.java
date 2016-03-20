package org.telegram.android;


import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.telegram.messenger.R;
import org.telegram.ui.LoginActivity;

public class ChatHeadService extends Service {

	private WindowManager windowManager;
	LoginActivity ln;
	private ImageView chatHead;
	WindowManager.LayoutParams params;
	public static final int REQUEST_ID = 1;

	@Override
	public void onCreate() {
		super.onCreate();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		chatHead = new ImageView(this);
		chatHead.setImageResource(R.drawable.logo2);

		params= new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;
		chatHead.setClickable(true);
		chatHead.setLongClickable(true);


		//this code is for dragging the chat head
		chatHead.setOnTouchListener(new View.OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, final MotionEvent event) {
				boolean ismoved=false;
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						ismoved=false;
						initialX = params.x;
						initialY = params.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						Handler mHandler = new Handler(Looper.getMainLooper());
						final boolean finalIsmoved = ismoved;
						mHandler.postDelayed(new Runnable() {
									public void run() {
										if (!finalIsmoved
												&& event.getAction() != MotionEvent.ACTION_UP) {
											//perform LongClickOperation
											chatHead.performLongClick();
								}
							}
						}, 1000);
						return true;
					case MotionEvent.ACTION_UP:
						int action_down_time = (int) System.currentTimeMillis();
						if ((Math.abs(initialTouchX - event.getRawX()) < 5) && (Math.abs(initialTouchY - event.getRawY()) < 5)) {
							Log.e("tag", "It's a click ! ");
							chatHead.performClick();
						} else Log.e("tag", "you moved the head");
						/*if (System.currentTimeMillis() - action_down_time > 10*1000) {
							chatHead.performLongClick();
						}*/
						return true;
					case MotionEvent.ACTION_MOVE:
						params.x = initialX
								+ (int) (event.getRawX() - initialTouchX);
						params.y = initialY
								+ (int) (event.getRawY() - initialTouchY);
						windowManager.updateViewLayout(chatHead, params);
						return true;
				}
				return false;
			}
		});

		chatHead.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent i = getBaseContext().getPackageManager()
						.getLaunchIntentForPackage( getBaseContext().getPackageName() );
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);

			}
		});
		chatHead.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				onDestroy();
				windowManager.removeView(chatHead);

				android.os.Process.killProcess(android.os.Process.myPid());
				return false;
			}
		});

		windowManager.addView(chatHead, params);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if (chatHead != null) {
			windowManager.removeView(chatHead);
			chatHead.setImageBitmap(null);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	/*public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		Log.d("tag", "Permission callback called-------");
		switch (requestCode) {
			case REQUEST_ID_MULTIPLE_PERMISSIONS: {

				Map<String, Integer> perms = new HashMap<>();
				// Initialize the map with both permissions
				perms.put(Manifest.permission.SYSTEM_ALERT_WINDOW, PackageManager.PERMISSION_GRANTED);


				// Fill with actual results from user
				if (grantResults.length > 0) {
					for (int i = 0; i < permissions.length; i++)
						perms.put(permissions[i], grantResults[i]);
					// Check for both permissions
					if (perms.get(Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED
							) {
						Log.d("TAG1", "sms & location services permission granted");
						// process the normal flow
						//else any one or both the permissions are not granted
					} else {
						Log.d("TAG2", "Some permissions are not granted ask again ");
						//permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
						//show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
						if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
								|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
						{
							showDialogOK("CAMERA and GALLERY access Permission required for this app",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											switch (which) {
												case DialogInterface.BUTTON_POSITIVE:
													checkAndRequestPermissions();
													break;
												case DialogInterface.BUTTON_NEGATIVE:
													// proceed with logic by disabling the related features or quit the app.
													break;
											}
										}
									});
						}
						//permission is denied (and never ask again is  checked)
						//shouldShowRequestPermissionRationale will return false
						else {
							Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
									.show();
							//                            //proceed with logic by disabling the related features or quit the app.
						}
					}
				}
			}
		}

	}

	private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
		new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton("OK", okListener)
				.setNegativeButton("Cancel", okListener)
				.create()
				.show();
	}
	private  boolean checkAndRequestPermissions() {
		int camerapermission = ContextCompat.checkSelfPermission(this,
				Manifest.permission.CAMERA);

		int externalstoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
		int externalstoragewritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		List<String> listPermissionsNeeded = new ArrayList<>();
		if (camerapermission != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.CAMERA);
		}
		if (externalstoragePermission != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
		}

		if (externalstoragewritePermission != PackageManager.PERMISSION_GRANTED) {
			listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		if (!listPermissionsNeeded.isEmpty()) {
			ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
			return false;
		}
		return true;
	}*/

}