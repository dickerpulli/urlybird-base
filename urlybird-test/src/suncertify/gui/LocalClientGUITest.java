package suncertify.gui;

import java.util.regex.Pattern;

import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LocalClientGUITest {

	private FrameFixture window;

	@Before
	public void before() {
		ClientFrame frame = GuiActionRunner.execute(new GuiQuery<ClientFrame>() {
			@Override
			protected ClientFrame executeInEDT() {
				return new ClientFrame(new GuiController(), ConnectionMode.LOCAL);
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
		window.textBox("dbLocationTextField").requireEditable();
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Connect");
		window.button(textMatcher).requireEnabled();
		window.requireVisible();
	}

	@Test
	public void testDBLocationFailure() throws InterruptedException {
		window.textBox("dbLocationTextField").setText("xx");
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Connect");
		window.button(textMatcher).click();
		Thread.sleep(5000);
		window.label("statusLabel").requireText(Pattern.compile(".*File not found.*"));
		window.requireVisible();
	}

	@Test
	public void testBrowseButton() {
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Browse...");
		window.button(textMatcher).click();
		window.fileChooser().requireVisible();
	}

	@Test
	public void testStartClient() {
		window.textBox("dbLocationTextField").setText("test/db-1x1.db");
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Connect");
		window.button(textMatcher).click();
		window.requireNotVisible();
	}

	@Test
	public void testConfiguration() {
		window.textBox("dbLocationTextField").setText("xyz");
		window.cleanUp();
		window.show();
		window.textBox("dbLocationTextField").requireText("xyz");
		window.textBox("dbLocationTextField").setText("abc");
		window.cleanUp();
		window.show();
		window.textBox("dbLocationTextField").requireText("abc");
	}

}
