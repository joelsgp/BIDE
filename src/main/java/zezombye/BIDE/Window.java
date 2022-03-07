package zezombye.BIDE;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

public class Window extends JFrame implements SearchListener {
	public ReplaceDialog replaceDialog;

	public Window() {
		super();
		
		replaceDialog = new ReplaceDialog(this, this);
		replaceDialog.setTitle("Find/Replace");
	}
	
	@Override
	public String getSelectedText() {
		try {
			return ((ProgScrollPane)BIDE.ui.jtp.getSelectedComponent()).textPane.getSelectedText();
		} catch (NullPointerException | ClassCastException e) {
			return null;
		}
	}

	@Override
	public void searchEvent(SearchEvent e) {

		SearchEvent.Type type = e.getType();
		SearchContext context = e.getSearchContext();
		SearchResult result = null;
		
		if (!(BIDE.ui.jtp.getSelectedComponent() instanceof ProgScrollPane)) {
			return;
		}
		
		ProgramTextPane textPane = ((ProgScrollPane)BIDE.ui.jtp.getSelectedComponent()).textPane;
		textPane.getCaret().getMark();
		switch (type) { // Prevent FindBugs warning later
			case MARK_ALL -> result = SearchEngine.markAll(textPane, context);
			case FIND -> {
				result = SearchEngine.find(textPane, context);
				if (!result.wasFound()) {
					UIManager.getLookAndFeel().provideErrorFeedback(textPane);
				}
			}
			case REPLACE -> {
				result = SearchEngine.replace(textPane, context);
				if (!result.wasFound()) {
					UIManager.getLookAndFeel().provideErrorFeedback(textPane);
				}
			}
			case REPLACE_ALL -> {
				result = SearchEngine.replaceAll(textPane, context);
				JOptionPane.showMessageDialog(null, result.getCount() +
						" occurrences replaced.");
			}
		}

		String text = "";
		if (result.wasFound()) {
			text = "Text found";
		}
		else if (type==SearchEvent.Type.MARK_ALL) {
			text = "";
		}
		else {
			text = "Text not found";
		}
	}
}
