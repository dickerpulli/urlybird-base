package suncertify.gui;

import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientGUITest {

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
	public void testStartClient() {
		window.textBox("dbLocationTextField").setText("test/db-1x1.db");
		ButtonTextMatcher textMatcher = new ButtonTextMatcher("Connect");
		window.button(textMatcher).click();
		window.requireNotVisible();
	}

}
