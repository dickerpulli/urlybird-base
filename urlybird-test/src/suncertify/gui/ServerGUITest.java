package suncertify.gui;

import java.util.regex.Pattern;

import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerGUITest {

	private FrameFixture window;

	@Before
	public void before() {
		ServerFrame frame = GuiActionRunner.execute(new GuiQuery<ServerFrame>() {
			@Override
			protected ServerFrame executeInEDT() {
				return new ServerFrame(new GuiController());
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
		window.textBox("dbTextField").requireEditable();
		window.textBox("portTextField").requireEditable();
		assertServerStopped();
	}

	@Test
	public void testDBLocationFailure() throws InterruptedException {
		window.textBox("dbTextField").setText("xx");
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Start");
		window.button(textMatcher).click();
		Thread.sleep(5000);
		window.label("statusLabel").requireText(Pattern.compile(".*not.*"));
		assertServerStopped();
	}

	@Test
	public void testPortFailure() throws InterruptedException {
		window.textBox("portTextField").setText("xx");
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Start");
		window.button(textMatcher).click();
		Thread.sleep(5000);
		window.label("statusLabel").requireText(Pattern.compile("Illegal port number.*"));
		assertServerStopped();
	}

	@Test
	public void testBrowseButton() {
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Browse...");
		window.button(textMatcher).click();
		window.fileChooser().requireVisible();
	}

	@Test
	public void testStartAndStopServer() throws InterruptedException {
		window.textBox("dbTextField").setText("test/db-1x1.db");
		window.textBox("portTextField").setText("88");
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Start");
		window.button(textMatcher).click();
		Thread.sleep(5000);
		window.label("statusLabel").requireText(Pattern.compile(".*started.*"));
		assertServerStarted();
		textMatcher = new ButtonTextMatcher("Stop");
		window.button(textMatcher).click();
		Thread.sleep(5000);
		assertServerStopped();
	}

	@Test
	public void testConfiguration() {
		window.textBox("dbTextField").setText("xyz");
		window.textBox("portTextField").setText("123");
		window.cleanUp();
		window.show();
		window.textBox("dbTextField").requireText("xyz");
		window.textBox("portTextField").requireText("123");
		window.textBox("dbTextField").setText("abc");
		window.textBox("portTextField").setText("987");
		window.cleanUp();
		window.show();
		window.textBox("dbTextField").requireText("abc");
		window.textBox("portTextField").requireText("987");
	}

	private void assertServerStopped() {
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Start");
		window.button(textMatcher).requireEnabled();
		textMatcher = new ButtonTextMatcher("Stop");
		window.button(textMatcher).requireDisabled();
		window.requireVisible();
	}

	private void assertServerStarted() {
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Start");
		window.button(textMatcher).requireDisabled();
		textMatcher = new ButtonTextMatcher("Stop");
		window.button(textMatcher).requireEnabled();
		window.requireVisible();
	}

}
