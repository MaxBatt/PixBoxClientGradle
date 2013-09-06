package de.pixbox.client.test;

import de.pixbox.client.MainActivity;
import de.pixbox.client.RegisterActivity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivityUnitTest extends ActivityUnitTestCase<RegisterActivity> {

	private int registerButtonID;
	private RegisterActivity registerActivity;
	
	private EditText editUsername;
	private Button registerButton;
	private TextView tvError;

	public RegisterActivityUnitTest() {
		super(RegisterActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Intent intent = new Intent(getInstrumentation().getTargetContext(),
				RegisterActivity.class);
		startActivity(intent, null, null);
		
		// Get reference to RegisterActivity
		registerActivity = getActivity();


		// Get references to EditText for username, RegisterButton and TextView
		// for error messages
		editUsername = (EditText) registerActivity
				.findViewById(de.pixbox.client.R.id.editUsername);
		registerButton = (Button) registerActivity
				.findViewById(de.pixbox.client.R.id.registerButton);
		tvError = (TextView) registerActivity
				.findViewById(de.pixbox.client.R.id.tvError);
	}

	
	@SmallTest
	  public void testLayout() {
	    
		// Get reference to RegisterButton
		registerButtonID = de.pixbox.client.R.id.registerButton;
		Button registerButton = (Button) registerActivity.findViewById(registerButtonID);
		
		// Test if RegisterButton exists and is initialized
	    assertNotNull("RegisterButton does not exist", registerActivity.findViewById(registerButtonID));
	    
	    // Test if RegisterButton has the right Text
	    assertEquals("Incorrect label of the Register Button", registerActivity.getResources().getString(de.pixbox.client.R.string.register_button), registerButton.getText());
	    
	    // Test if RegisterButton is disabled
	    assertEquals("RegisterButton must be disabled if EditText for username is empty", registerButton.isEnabled(), false);
	  }
	
	@SmallTest
	public void testValidation() throws Exception {

		// Clear username
		editUsername.clearComposingText();

		// Set invalid username
		// This must run on the UI-Thread
		registerActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				editUsername.setText("abc#def");
			}
		});

		// Wait until Intrumentation has the new Value
		getInstrumentation().waitForIdleSync();

		// Test if RegisterButton is still disabled
		assertEquals(
				"RegisterButton must be disabled if username contains invalid characters",
				registerButton.isEnabled(), false);

		// Test if the right error message is set
		assertEquals(
				"Error message must be set if username is invalid",
				tvError.getText(),
				registerActivity.getResources().getString(
						de.pixbox.client.R.string.err_wrong_username));
	}
	
	
	
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
		

}
