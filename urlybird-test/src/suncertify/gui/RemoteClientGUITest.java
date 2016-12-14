package suncertify.gui;

import static org.junit.Assert.fail;

import java.util.regex.Pattern;

import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RemoteClientGUITest {

	private FrameFixture window;

	@Before
	public void before() {
		ClientFrame frame = GuiActionRunner.execute(new GuiQuery<ClientFrame>() {
			@Override
			protected ClientFrame executeInEDT() {
				return new ClientFrame(new GuiController(), ConnectionMode.REMOTE);
			}
		});
		window = new FrameFixture(frame);
		window.show();
	}

	@After
	public void after() {
		window.cleanUp();
	}

	@Test
	public void testStartGUI() {
		window.textBox("hostnameTextField").requireEditable();
		window.textBox("portTextField").requireEditable();
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Connect");
		window.button(textMatcher).requireEnabled();
		window.requireVisible();
	}

	@Test
	public void testHostnameFailure() throws InterruptedException {
		window.textBox("hostnameTextField").setText("xx");
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Connect");
		window.button(textMatcher).click();
		Thread.sleep(5000);
		window.label("statusLabel").requireText(Pattern.compile(".*No remote database.*"));
		window.requireVisible();
	}

	@Test
	public void testPortFailure() throws InterruptedException {
		window.textBox("portTextField").setText("xx");
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Connect");
		window.button(textMatcher).click();
		Thread.sleep(1000);
		window.label("statusLabel").requireText(Pattern.compile(".*Illegal port number.*"));
		window.requireVisible();
	}

	@Test
	public void testNoBrowseButton() {
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Browse...");
		try {
			window.button(textMatcher);
			fail("Exception expected here");
		} catch (ComponentLookupException e) {
			// yipiie
		}
	}

	@Test
	public void testStartClient() {
		// // Server server = new SocketServer(88, "test/db-1x1.db");
		// Server server = new RMIServer(88, "test/db-1x1.db");
		// server.start();
		// window.textBox("hostnameTextField").setText("localhost");
		// window.textBox("portTextField").setText("88");
		// ButtonTextMatcher textMatcher = new ButtonTextMatcher("Connect");
		// window.button(textMatcher).click();
		// window.requireNotVisible();
		// window.close();
		// server.stop();
	}

	@Test
	public void testConfiguration() {
		window.textBox("hostnameTextField").setText("xyz");
		window.textBox("portTextField").setText("123");
		window.cleanUp();
		window.show();
		window.textBox("hostnameTextField").requireText("xyz");
		window.textBox("portTextField").requireText("123");
		window.textBox("hostnameTextField").setText("abc");
		window.textBox("portTextField").setText("987");
		window.cleanUp();
		window.show();
		window.textBox("hostnameTextField").requireText("abc");
		window.textBox("portTextField").requireText("987");
	}

}
