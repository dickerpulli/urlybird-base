package suncertify.gui;

import javax.swing.JButton;

import org.fest.swing.core.GenericTypeMatcher;

class ButtonTextMatcher extends GenericTypeMatcher<JButton> {

	private String text;

	public ButtonTextMatcher(String text) {
		super(JButton.class);
		this.text = text;
	}

	@Override
	protected boolean isMatching(JButton button) {
		return text.equals(button.getText());
	}
}