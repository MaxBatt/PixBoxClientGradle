package de.pixbox.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import de.pixbox.client.helpers.ConnectionCheck;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * The main screen of the PixBox app.</br>
 * There are two icons: "Get image" and "Your PixBox".</br>
 * By clicking on "Get image" users can take a photo with their camera or pick an image from the Android gallery (or other galleries if available).</br>
 * If an image is picked, it is shown in an imageView and the user can upload it to her PixBox (online storage) by clicking on "Upload".</br>
 * By Clicking on "Your PixBox" the user gets to a list which shows all of the uploaded images.</br>
 * 
 * @author Max Batt
 */
public class MainActivity extends Activity {

	public static final String PREFS = "PIXBOX_PREFS";
	private static final String TAG = "Pixbox: ";
	private final String BASE_URL = "http://www.maxbatt.de/picbox";
	private String userID;
	private String username;
	private TextView tvUserWelcome;
	private TextView tvError;
	private ImageView imgView;
	private Button takePictureBtn;
	private Button galleryBtn;
	private Button uploadImgBtn;
	private static Bitmap bitmap;
	private ProgressDialog pd;
	private TextView uploadLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove TitleBar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		
		setContentView(R.layout.main_copy);
		
		
		tvUserWelcome = (TextView) findViewById(R.id.tvUserWelcome);
		tvError = (TextView) findViewById(R.id.tvError);
		imgView = (ImageView) findViewById(R.id.imgView);
		takePictureBtn = (Button) findViewById(R.id.takePictureBtn);
		galleryBtn = (Button) findViewById(R.id.galleryBtn);
		uploadImgBtn = (Button) findViewById(R.id.uploadImgBtn);
		uploadLabel = (TextView) findViewById(R.id.uploadLabel);
		
		
		
		// Hide uplad image button

		hideView(uploadImgBtn);
		hideView(uploadLabel);
		
		// Read Sared Prefs
		SharedPreferences settings = getSharedPreferences(PREFS, 0);

		// Delete Userdata in preferences if necessary
		// SharedPreferences.Editor editor = settings.edit();
		// editor.remove("userid");
		// editor.remove("username");
		// editor.commit();

		// Get Userdata from Shared Prefs
		userID = settings.getString("userid", null);
		username = settings.getString("username", null);

		// System.out.println("id: " + userID);
		// System.out.println("name: " + username);

		// If user not created yet, go to RegisterActivity
		if (userID == null || username == null) {
			Intent i = new Intent(this, RegisterActivity.class);
			startActivity(i);
		}
		// If user already exists
		else {
			// Set Welcome Message
			tvUserWelcome.setText(getString(R.string.user_welcome_text,
					username));
		}

		// Check InternetConnection. If offline, hide all Buttons and show
		// Errormessage
		ConnectionCheck cc = new ConnectionCheck(getApplicationContext());
		Boolean isOnline = cc.checkConnection();

		if (!isOnline) {
			hideView(takePictureBtn);
			hideView(galleryBtn);
			tvError.setText(getString(R.string.err_no_internet));
		}

	}

	
	/**
	 * Show image always in thumbnail size
	/* This must be in onWindowFocusChanged to be able to measure actual view
	 */
	
	/*
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus == true) {
			
			View parentView = findViewById(R.id.mainContent);
			
			int height = parentView.getHeight();
			int width = parentView.getWidth();

			
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) imgView
					.getLayoutParams();
			int percentHeight = (int) Math.round(height * .3);
			int percentWidth = (int) Math.round(width * .5);
			lp.height = percentHeight;
			lp.width = percentWidth;
			imgView.setLayoutParams(lp);
			
		}
	}
	*/
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	@Override
	protected void onPause() {
        super.onPause();
        //Delete messages
        tvError.setText("");
    }


	
	/**
	 * Runs if the "Take picture" Button is clicked Opens a dialog, with which
	 * you can choose if you want to take a photo with your camera or to choose
	 * a picture from the Gallery
	 * 
	 * @param v
	 */
	public void onPictureBtnClicked(View v) {

		// Options for choosing-dialog
		final CharSequence[] options = {
				getResources().getString(R.string.picture_cam),
				getResources().getString(R.string.picture_gallery),
				getResources().getString(R.string.cancel) };

		// Create Dialog with given options
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(getResources().getString(R.string.add_photo_dialog));
		builder.setItems(options, new DialogInterface.OnClickListener() {

			// OnClick on dialog options
			@Override
			public void onClick(DialogInterface dialog, int item) {

				// Option: Take photo with camera
				if (options[item].equals(getResources().getString(
						R.string.picture_cam))) {
					// Camera Intent
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					// Store the image-file as temp.jpg on SD-Card
					File f = new File(android.os.Environment
							.getExternalStorageDirectory(), "temp.jpg");
					// Put image-file to intent
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					// Start activity
					startActivityForResult(intent, 1);

				}

				// Option: Choose image from Gallery
				else if (options[item].equals(getResources().getString(
						R.string.picture_gallery)))

				{
					// Gallery Intent
					Intent intent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

					startActivityForResult(intent, 2);
				}

				// Option: cancel
				else if (options[item].equals(getResources().getString(
						R.string.cancel))) {
					// Dismiss dialog
					dialog.dismiss();
				}

			}
		});

		// Show Dialog
		builder.show();
	}

	
	
	/**
	 * Is executed after a picture has been taken by camera or gallery
	 * Shows the result image in an imageView and displays an upload button
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		// If image could be resolved
		if (resultCode == RESULT_OK) {

			// Case: You took a photo via camera
			if (requestCode == 1) {

				// get SD-Card-Folder
				File f = new File(Environment.getExternalStorageDirectory()
						.toString());

				System.out.println(f.getAbsolutePath());

				// Find taken photo in folder
				for (File temp : f.listFiles()) {
					if (temp.getName().equals("temp.jpg")) {
						// f is now the temporary image
						f = temp;
						break;
					}
				}

				try {
					// Create Bitmap out of temp.jpg
					bitmap = createBitmap(Uri.fromFile(f));
					// Delete temp.jpg
					f.delete();
				} catch (Exception e) {

					e.printStackTrace();

				}

				// Case: You choose an image from the Gallery
			} else if (requestCode == 2) {
				// Get URI to selected image
				Uri selectedImage = data.getData();
				bitmap = createBitmap(selectedImage);
			}

			// Set ImageView with created Bitmap
			imgView.setImageBitmap(bitmap);
			showView(imgView);

			// Make Upload Image Button visible
			showView(uploadImgBtn);
			showView(uploadLabel);
		}

	}

	
	
	/**
	 * Is executed if the Upload Image Button is clicked
	 * @param v
	 */
	public void onUploadImgBtnClicked(View v) {
		hideView(uploadImgBtn);
		hideView(uploadLabel);
		hideView(imgView);
		pd = ProgressDialog
		.show(this,
				getResources().getString(R.string.wait),
				getResources().getString(
						R.string.uploading_image), true);
		uploadImage();
	}
	
	
	
	/**
	 * Is executed if the Gallery Button is clicked
	 * @param v
	 */
	public void onGalleryBtnClicked(View v) {
		
		Intent i = new Intent(this, PixBoxActivity.class);
		i.putExtra("userID", userID);
		i.putExtra("username", username); 
		startActivity(i);
	}

	
	
	
	
	
	
	/**
	 * Create a bitmap out of an Image URI
	 * @param selectedImage
	 * @return
	 */
	public Bitmap createBitmap(Uri selectedImage) {
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 5;
		AssetFileDescriptor fileDescriptor = null;
		try {
			fileDescriptor = this.getContentResolver().openAssetFileDescriptor(
					selectedImage, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				bm = BitmapFactory.decodeFileDescriptor(
						fileDescriptor.getFileDescriptor(), null, options);
				fileDescriptor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bm;
	}

	
	
	/**
	 * Upload selected image to the server
	 * @return
	 */
	private boolean uploadImage() {
		
		// Base64 Encoding the image
		String img64 = encodeTobase64(bitmap);
		// Log.d(TAG, img64);

		// PArams for HTTP-Request
		RequestParams params = new RequestParams();
		params.put("userid", userID);
		params.put("username", username);
		params.put("img64", img64);

		// Create HTTP Client
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(BASE_URL + "/pictures/upload/", params,
				new AsyncHttpResponseHandler() {
					// If Request was successfull
					@Override
					public void onSuccess(String response) {
						pd.dismiss();
						if (response.length() != 1) {
							Log.d(TAG, "HTTP Request nicht erfolgreich. Antwort: " + response);
							// Error Message
							tvError.setText((getResources().getString(
									R.string.err_upload)));
						}
						else{
							Log.d(TAG, "HTTP Request erfolgreich. Antwort: " + response);
							
							tvError.setText(getResources().getString(
									R.string.upload_complete));
						}
					}
					
					// If Request failed
					@Override
					public void onFailure(Throwable error, String content) {
						// Still show ImageView and Upload Button for coming uploads 
						showView(imgView);
						showView(uploadImgBtn);
						showView(uploadLabel);
						// Hide ProgrssBAr
						pd.dismiss();
						
						// Show Error message
						tvError.setText(getResources().getString(
								R.string.err_upload));
						Log.d(TAG, "HTTP-Request nicht erfolgreich. Fehler: " + error.toString());

					}
				});
		return true;
	}

	
	
	/**
	 * Encodes Bitmap to Base64 String
	 * @param image
	 * @return
	 */
	public static String encodeTobase64(Bitmap image) {
		Bitmap immagex = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

		Log.e("LOOK", imageEncoded);
		return imageEncoded;
	}

	
	
	/**
	 * Decodes Base64 String to Bitmap
	 * @param input
	 * @return
	 */
	public static Bitmap decodeBase64(String input) {
		byte[] decodedByte = Base64.decode(input, 0);
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

	
	
	/**
	 * Makes a view visible
	 * @param v
	 */
	public void showView(View v){
		v.setVisibility(View.VISIBLE);
	}
	
	
	
	/**
	 * Makes a view unvisible
	 * @param v
	 */
	public void hideView(View v){
		v.setVisibility(View.GONE);
	}
}
