package de.pixbox.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpResponseHandler;

import de.pixbox.client.helpers.GalleryListAdapter;
import de.pixbox.client.helpers.Image;
import de.pixbox.client.helpers.RestClient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Shows a list of all uploaded images and their upload date. </br>
 * On click on an image the image is opened in the Android galler (or other, if available). </br>
 * On long click on an image the user can send the image to other applications, copy the images public URL to clipboard and delete it from the PixBox.</br>
 *  
 * @author Max Batt
 * 
 */
public class PixBoxActivity extends ListActivity implements
		OnItemClickListener {

	// ArrayList with all images
	private ArrayList<Image> imageList;
	// Adapter for Listview
	private GalleryListAdapter adapter;
	// position in list of the clicked image
	private int actPosition;
	// SD Card Folder for storing images
	private static String imageFolder = Environment
			.getExternalStorageDirectory().toString() + "/PixBox/pictures";
	// ProgressDialog
	private ProgressDialog pd;

	
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		//Remove TitleBar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);


		// Show ProgressDialog until Gallery is loaded
		pd = ProgressDialog
				.show(this,
						getResources().getString(R.string.wait),
						getResources().getString(
								R.string.loading_online_gallery), true);

		// Fetch Username and ID from intent extras
		// Extras
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}

		String userID = extras.getString("userID");
		// String username = extras.getString("username");

		// Create a PixBox images folder, if it not exists
		createImageFolder(imageFolder);

		// Downlaod JSON list with all image URLS and infos from server with
		// AsyncHTTPClient
		RestClient.get("pictures/?u=" + userID, null,
				new AsyncHttpResponseHandler() {

					// If request is successful
					@Override
					public void onSuccess(String response) {

						// Log.d("pixbox", response);

						// Convert JSON-Array in ArrayList
						Gson gson = new Gson();
						JsonParser parser = new JsonParser();
						JsonArray Jarray = parser.parse(response)
								.getAsJsonArray();

						imageList = new ArrayList<Image>();

						for (JsonElement obj : Jarray) {
							Image image = gson.fromJson(obj, Image.class);
							imageList.add(image);
						}

						// If there are images in the ArrayList
						if (imageList.size() > 0) {

							// Create ListAdapter
							adapter = new GalleryListAdapter(
									PixBoxActivity.this, imageList);
							setListAdapter(adapter);

							// Fill ListView
							ListView listView = getListView();

							// Dismiss ProgressDialog, when Gallery is loaded
							pd.dismiss();

							// OnItemClickListener for Click on an item in list
							listView.setOnItemClickListener(PixBoxActivity.this);

						} else {
							// If there are no images in the list, show error
							// message
							showDialog(getResources().getString(
									R.string.no_images));

						}

					}

					// If request was not succesful, show error
					@Override
					public void onFailure(Throwable error, String content) {
						showDialog(getResources().getString(
								R.string.err_connect));
						Log.d("Pixbox", "HTTP error: " + error.getMessage());
					}
				});

		registerForContextMenu(this.getListView());
	}

	
	
	/*
	 * (non-Javadoc) onItemClickListener for ListView. on Click the image is
	 * opened in gallery
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		// List-position of the clicked image
		actPosition = position;

		// Check if the file is already on SD Card. If not, download.

		String imagePath = imageFolder + "/"
		+ imageList.get(position).getFilename();

		if (!fileIsOnSDCard(imageList.get(position).getFilename())){
			// Start AsyncTask downloading image
			new DownloadFileFromURL().execute(imageList.get(position).getUrl());
			openImageInGallery( imageFolder + "/" + imageList.get(position).getFilename());
			System.out.println("Downloaded the file!");
		} else {
			openImageInGallery(imagePath);
			System.out.println("Opened The file from SD Card");
		}

	}

	
	
	/*
	 * (non-Javadoc) Open context menu on long click on a gallery item Options:
	 * Send image, Copy public URL, Delete Image
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == this.getListView().getId()) {
			menu.setHeaderTitle(getResources().getString(
					R.string.context_menu_title));
			menu.add(0, v.getId(), 0,
					getResources().getString(R.string.send_image));
			menu.add(0, v.getId(), 1,
					getResources().getString(R.string.copy_url));
			menu.add(0, v.getId(), 2, getResources().getString(R.string.delete));

			// System.out.println(image.getFilename());
		}
	}

	
	
	/*
	 * (non-Javadoc) Actions if an option in the context menu is clicked
	 */
	@SuppressLint("NewApi")
	public boolean onContextItemSelected(MenuItem item) {
		// Get infos about selected item
		AdapterView.AdapterContextMenuInfo selectedItem = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		// Get the image of the clicked menu item
		Image image = (Image) getListView().getItemAtPosition(
				selectedItem.position);

		// If send is clicked
		if (item.getTitle() == getResources().getString(R.string.send_image)) {
			
			// Check, if file is already on SD Card
			if(!fileIsOnSDCard(image.getFilename())){
				// If not download via AsyncTask
				new DownloadFileFromURL().execute(image.getUrl());
			}
			
			// Generating Intent, so that all Applications which can work with images get the intent
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("image/jpg");
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imageFolder + "/" + image.getFilename())));
			//Start Intent
			startActivity(i);
		}

		// If 'Delete' is clicked
		if (item.getTitle() == getResources().getString(R.string.delete)) {

			// Asynctask for deleting image
			DeleteImageFromGallery task = new DeleteImageFromGallery();
			task.execute(image);

			// Remove deleted item from list
			adapter.remove(adapter.getItem(selectedItem.position));

			// If there are no images in the list, show error
			// message
			if (adapter.isEmpty()) {
				showDialog(getResources().getString(R.string.no_images));
			}

		}

		// If user 'Copy to Clipboard' is clicked
		if (item.getTitle() == getResources().getString(R.string.copy_url)) {

			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("label", image.getUrl());
				clipboard.setPrimaryClip(clip);
			} else {
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(image.getUrl());
			}
			// display in short period of time
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.url_copied),
					Toast.LENGTH_LONG).show();

		}
		return true;
	}

	
	
		
	/**
	 * AsyncTask for Downloading image, writing it to local Storage and open in
	 * Gallery
	 * 
	 * @author Max Batt
	 * 
	 */
	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Show ProgressDialog until Gallery is loaded
			pd = ProgressDialog.show(PixBoxActivity.this, getResources()
					.getString(R.string.wait),
					getResources().getString(R.string.exporting_image), true);
		}

		/**
		 * Downloading file in background thread and writing it to SD Card
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				URLConnection conection = url.openConnection();
				conection.connect();

				// input stream to read file - with 8k buffer
				InputStream input = new BufferedInputStream(url.openStream(),
						8192);

				// Output stream to write file
				OutputStream output = new FileOutputStream(imageFolder + "/"
						+ imageList.get(actPosition).getFilename());

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					// writing data to file
					output.write(data, 0, count);
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();

			} catch (Exception e) {
				Log.d("Error: ", e.getMessage());
			}

			return null;
		}

		protected void onProgressUpdate(String... progress) {

		}

		// After background task open the image in gallery.
		@Override
		protected void onPostExecute(String file_url) {

			// Displaying downloaded image into image view
			// Reading image path from sdcard
			String imagePath = imageFolder + "/"
					+ imageList.get(actPosition).getFilename();
			Log.d("pixbox", "Downloaded file to " + imagePath);

			// Dismiss ProgressDialog
			pd.dismiss();
		}

	}

	
	
	/**
	 * AsyncTask for Deleting an image from the gallery over context menu
	 * 
	 * @author Max Batt
	 * 
	 */
	class DeleteImageFromGallery extends AsyncTask<Image, Image, String> {

		/**
		 * Before starting background thread Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Show ProgressDialog until Gallery is loaded
			pd = ProgressDialog.show(PixBoxActivity.this, getResources()
					.getString(R.string.wait),
					getResources().getString(R.string.exporting_image), true);
		}

		/**
		 * Deleting File from Server and from SD Card (if exists) from the
		 * Gallery List
		 * */
		@Override
		protected String doInBackground(Image... images) {
			Image image = images[0];

			// Downlaod JSON list with all image URLS and infos from server with
			// AsyncHTTPClient
			RestClient.get("pictures/delete/?p=" + image.getId(), null,
					new AsyncHttpResponseHandler() {

						// If request is successful
						@Override
						public void onSuccess(String response) {

							if (response.length() == 1) {
								Log.d("PixBox: ", "Image Deleted");
							} else {
								Log.d("PixBox: ", "Couldnt delete image: "
										+ response);
							}
						}

						// If request was not succesful, show error
						@Override
						public void onFailure(Throwable error, String content) {
							Log.d("PixBox: ",
									"Delete File HTTP Request Failed: "
											+ error.getMessage());
						}
					});

			File file = new File(imageFolder + "/" + image.getFilename());
			if (file.exists()) {
				file.delete();
			}

			return null;
		}

		protected void onProgressUpdate(String... progress) {

		}

		// After background task open the image in gallery.
		@Override
		protected void onPostExecute(String file_url) {

			pd.dismiss();
		}

	}

	
	
	// Checks if a file with the given filename is on the SD card in the pixbox folder
	public boolean fileIsOnSDCard(String filename) {
		
		File file = new File(imageFolder + "/" + filename);

		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	
	
	/**
	 * Opens an image from given Path in the (android) gallery
	 * 
	 * @param imagePath
	 */
	public void openImageInGallery(String imagePath) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + imagePath), "image/*");
		startActivity(intent);
	}

	
	
	/**
	 * Show dialog for error message with given MSG param
	 * 
	 * @param msg
	 * */
	protected void showDialog(String msg) {
		new AlertDialog.Builder(PixBoxActivity.this).setMessage(msg)

		.setPositiveButton(getResources().getString(R.string.back),
		// Click Listener
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// cloase dialog
						dialog.cancel();
						PixBoxActivity.super.onBackPressed();
					}
				}).create().show();
	}

	
	
	/**
	 * Creates a folder with given path
	 * 
	 * @param folder
	 */
	public void createImageFolder(String folder) {
		File f = new File(folder);
		if (!f.exists()) {
			f.mkdirs();
		}
	}

}
