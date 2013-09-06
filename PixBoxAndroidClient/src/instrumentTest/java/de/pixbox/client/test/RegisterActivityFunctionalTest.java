package de.pixbox.client.test;

import java.util.Random;

import de.pixbox.client.MainActivity;
import de.pixbox.client.R;
import de.pixbox.client.RegisterActivity;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivityFunctionalTest extends
		ActivityInstrumentationTestCase2<RegisterActivity> {

	private RegisterActivity registerActivity;
	private EditText editUsername;
	private Button registerButton;
	private ActivityMonitor activityMonitor;
	private TextView tvUserWelcome;

	public RegisterActivityFunctionalTest() {
		super(RegisterActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// Get reference to RegisterActivity
		registerActivity = getActivity();

		// Add ActivityMonitor to check for MainActivity
		activityMonitor = getInstrumentation().addMonitor(
				MainActivity.class.getName(), null, false);

		// Get references to EditText for username, RegisterButton and TextView
		// for error messages
		editUsername = (EditText) registerActivity
				.findViewById(de.pixbox.client.R.id.editUsername);
		registerButton = (Button) registerActivity
				.findViewById(de.pixbox.client.R.id.registerButton);

	}

	@SmallTest
	public void testStartMainActivity() throws Exception {

		// Clear username
		editUsername.clearComposingText();
		final String username = createRandomString();
		// Set valid username
		registerActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Set a random String as username
				editUsername.setText(username);
			}
		});

		// Wait until Intrumentation has the new Value
		getInstrumentation().waitForIdleSync();

		// Test if RegisterButton is now enabled
		assertEquals("RegisterButton must be enabled",
				registerButton.isEnabled(), true);

		// Click registerButton
		TouchUtils.clickView(this, registerButton);

		// Wait 2 seconds for the start of the activity
		MainActivity mainActivity = (MainActivity) activityMonitor
				.waitForActivityWithTimeout(2000);
		assertNotNull(mainActivity);

		tvUserWelcome = (TextView) mainActivity
				.findViewById(de.pixbox.client.R.id.tvUserWelcome);

		// Test if the TextView for the welcome message is on the screen
		ViewAsserts.assertOnScreen(mainActivity.getWindow().getDecorView(),
				tvUserWelcome);

		// Test if user is welcomed with username
		assertEquals(
				"Username is missing in welcome message",
				registerActivity.getResources().getString(
						de.pixbox.client.R.string.user_welcome_text, username),
				tvUserWelcome.getText().toString());

	}

	private String createRandomString() {
		char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 6; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}

}
