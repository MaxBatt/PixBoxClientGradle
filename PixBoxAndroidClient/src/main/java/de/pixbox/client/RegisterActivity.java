package de.pixbox.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import de.pixbox.client.helpers.RestClient;
import de.pixbox.client.helpers.User;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * User can register here by typing in a Username.</br>
 * Is only called once after the first start of PixBox.</br>
 * Webservice is called to create User in DB, Userdata is made permanently available in shared preferences.</br>
 * 
 * @author Max Batt 
 */
public class RegisterActivity extends Activity {

	private EditText et;
	private Button btn;
	private TextView tv;
	private ProgressBar pb;
	public static final String PREFS = "PIXBOX_PREFS";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove TitleBar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.register_activity);

		et = (EditText) findViewById(R.id.editUsername);
		btn = (Button) findViewById(R.id.okBtn);
		tv = (TextView) findViewById(R.id.tvUserResult);
		pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.GONE);

		btn.setEnabled(false);

		// Listener for any changes in the EditTest for Username
		// checks if username only contains letters and numbers
		// checks if username is empty

		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			/**
			 * @params s 
			 * Validates the Textfield for username everytime the user types a character.
			 * Checks if the textfield is empty and that the username only consists of letters and numbers.
			 */
			@Override
			public void afterTextChanged(Editable s) {

				// No username is typed in
				if (usernameIsEmpty(et)) {
					btn.setEnabled(false);
					tv.setText(getResources().getString(
							R.string.err_no_username));
				} else {
					String username = et.getText().toString();

					// username got invalid chars
					if (!username.matches("^[a-zA-Z0-9]+$")) {
						btn.setEnabled(false);
						tv.setText(getResources().getString(
								R.string.err_wrong_username));
					} else {
						btn.setEnabled(true);
						tv.setText("");
					}

				}

			}
		});

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	/**
	 * Called if "Create User" Button is clicked
	 * Creates an AsyncHTTPClient
	 * Sends a HHTP-Request to create a new user with the typed in username
	 * Reports an error, if user already exits
	 * If user can be created, the user information is passed back as a JSON-String
	 * JSON-String is converted into a user object. 
	 * User-data is stored in a Shared pref file to make it permanently available
	 * @param v
	 */
	public void onBtnClicked(View v) {
		
//		Show ProgressBar
		pb.setVisibility(View.VISIBLE);

		try {

			String user = URLEncoder.encode(et.getText().toString(), "UTF-8");
			
//			Send HTTP request for creating a user with given username 
			RestClient.get("user/new/?u=" + user, null,
					new AsyncHttpResponseHandler() {
				
//						If request is successful
						@Override
						public void onSuccess(String response) {
							
//							Hide Progressbar
							pb.setVisibility(View.GONE);

							try {
//								Convert JSON-String to object
								Gson gson = new Gson();
								User user = gson.fromJson(response, User.class);
								//System.out.println(user.getUsername());

								// Save Userdata permanently in SharedPreferences
								SharedPreferences settings = getSharedPreferences(
										PREFS, 0);
								SharedPreferences.Editor editor = settings
										.edit();
								settings.edit();
								editor.putString("userid", user.getId());
								settings.edit();
								editor.putString("username", user.getUsername());
								editor.commit();
								
								Intent i = new Intent(RegisterActivity.this, MainActivity.class);
								startActivity(i);
								finish();

//							If response is not a valid JSON string, it is an error message. Show it!
							} catch (Exception e) {
								System.out.println(e);
								tv.setText(getResources().getString(
										R.string.err_general)
										+ response);
							}
						}
						
//						If request was not succesful, show error
						@Override
						public void onFailure(Throwable error, String content) {
							// Do something with the response
							pb.setVisibility(View.GONE);
							tv.setText("An error occured: " + content);
						}
					});

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	
	/**
	 * Checks if the TextField for username is empty
	 * @param etText
	 * @return
	 */
	private boolean usernameIsEmpty(EditText etText) {
		if (etText.getText().toString().trim().length() > 0) {
			return false;
		} else {
			return true;
		}
	}

}
