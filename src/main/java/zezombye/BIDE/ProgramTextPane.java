package zezombye.BIDE;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.CaretStyle;

public class ProgramTextPane extends RSyntaxTextArea {
	
	public static DefaultCompletionProvider cp;
	
	public ProgramTextPane(int type) {
		super();
		if (type == BIDE.TYPE_CAPT || type == BIDE.TYPE_PICT) {
			BIDE.error("This should not happen");
			try {
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
        } else if (type == BIDE.TYPE_PROG || type == BIDE.TYPE_OPCODE || type == BIDE.TYPE_CHARLIST){
        	this.setFont(BIDE.progFont);
        } else {
        	this.setFont(BIDE.dispFont);
        }
		this.setBackground(Color.WHITE);
		this.setForeground(Color.BLACK);
		this.setCaretStyle(OVERWRITE_MODE, CaretStyle.UNDERLINE_STYLE);
		this.setTabSize(4);
		
		
		this.setLineWrap(true);
		this.setBackground(new Color(Integer.parseInt(BIDE.options.getProperty("bgColor"), 16)));
		this.setForeground(new Color(Integer.parseInt(BIDE.options.getProperty("textColor"), 16)));
		this.setCurrentLineHighlightColor(new Color(Integer.parseInt(BIDE.options.getProperty("hlColor"), 16)));
		this.setSyntaxEditingStyle(SYNTAX_STYLE_JAVA);
		
		//Set colors
		SyntaxScheme ss = (SyntaxScheme)this.getSyntaxScheme().clone();

		final Color keywordColor = new Color(Integer.parseInt(BIDE.options.getProperty("keywordColor"), 16));
		final Color operatorColor = new Color(Integer.parseInt(BIDE.options.getProperty("operatorColor"), 16));
		final Color variableColor = new Color(Integer.parseInt(BIDE.options.getProperty("variableColor"), 16));
		//final Color borderColor = new Color(Integer.parseInt(BIDE.options.getProperty("borderColor"), 16));
		final Color strColor = new Color(Integer.parseInt(BIDE.options.getProperty("strColor"), 16));
		final Color entityColor = new Color(Integer.parseInt(BIDE.options.getProperty("entityColor"), 16));
		final Color commentColor = new Color(Integer.parseInt(BIDE.options.getProperty("commentColor"), 16));
		final Color preprocessorColor = new Color(Integer.parseInt(BIDE.options.getProperty("preprocessorColor"), 16));
		
		ss.setStyle(Token.RESERVED_WORD, new Style(keywordColor));
		ss.setStyle(Token.OPERATOR, new Style(operatorColor));
		ss.setStyle(Token.VARIABLE, new Style(variableColor));
		ss.setStyle(Token.MARKUP_ENTITY_REFERENCE, new Style(entityColor));
		ss.setStyle(Token.RESERVED_WORD_2, new Style(operatorColor));
		ss.setStyle(Token.LITERAL_STRING_DOUBLE_QUOTE, new Style(strColor));
		ss.setStyle(Token.ERROR_STRING_DOUBLE, new Style(strColor));
		ss.setStyle(Token.LITERAL_CHAR, new Style(commentColor));
		ss.setStyle(Token.ERROR_CHAR, new Style(commentColor));
		ss.setStyle(Token.PREPROCESSOR, new Style(preprocessorColor));
		ss.setStyle(Token.FUNCTION, new Style(new Color(Integer.parseInt(BIDE.options.getProperty("textColor"), 16))));
		//ss.setStyle(Token.DATA_TYPE, new Style(borderColor));
		
		
		
		this.setSyntaxScheme(ss);
		
		if (BIDE.options.getProperty("autocomplete").equals("true")) {
			AutoCompletion ac = new AutoCompletion(cp);
			ac.setAutoActivationEnabled(true);
			ac.setAutoCompleteSingleChoices(false);
			ac.setAutoActivationDelay(0);
			ac.setShowDescWindow(true);
			ac.setChoicesWindowSize(300, 206);
			//to be the same height as the choices window, we must remove 60 px for some reason
			ac.setDescriptionWindowSize(500, 206);
		    ac.install(this);
		}
	}
	
	public static void initAutoComplete() {
	    DefaultCompletionProvider provider = new DefaultCompletionProvider() {
	    	@Override protected boolean isValidChar(char ch) {
	    		return ch >= '!' && ch <= '~';
	    	}
	    };
	    CompletionCellRenderer ccr = new CompletionCellRenderer();
	    provider.setListCellRenderer(ccr);
	    //If you change this string make sure to change the one in org.fife.ui.autocomplete.AbstractCompletionProvider.getCompletionsImpl() !
	    provider.setAutoActivationRules(false, "&ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
	    ArrayList<Opcode> opcodes2 = new ArrayList<>(BIDE.opcodes);
	    opcodes2.sort(Comparator.comparing(o -> o.hex));

		for (Opcode opcode : opcodes2) {
			if (opcode.text.length() > 1 && opcode.text.matches("([ -~])+")) {
				String txt = opcode.text.replaceAll("^ +", "");
				String summary = generateSummary(opcode);

				//Add opcodes with unicode
				if (opcode.unicode != null && BIDE.options.getProperty("allowUnicode").equals("true")) {
					//Add unicode representation of character in description
					if (opcode.unicode.length() == 1) {
						provider.addCompletion(
								new ShorthandCompletion(
										provider, txt, opcode.unicode,
										opcode.description, summary
								)
						);
					} else {
						provider.addCompletion(new ShorthandCompletion(provider, txt, opcode.unicode, opcode.description, summary));
					}

				} else {
					provider.addCompletion(new BasicCompletion(provider, txt, opcode.description, summary));
				}
			}
		}
		cp = provider;
	    for (int i = 0; i < BIDE.macros.size(); i++) {
	    	if (BIDE.macros.get(i).text.length() > 1 && BIDE.macros.get(i).text.matches("[\\w(), ]+")) {
	    		addMacroToCompletions(BIDE.macros.get(i));
	    	}
	    	
	    }
	    
	    cp.addCompletion(new BasicCompletion(
				cp, "#nocheck", "2",
				"Tells BIDE to not throw an error if there is a non-existing opcode. Use this to write plain text into programs (such as formulas)."
		));
	    cp.addCompletion(new BasicCompletion(
				cp, "#yescheck", "2",
				"Tells BIDE to check again for non-existing opcodes. See #nocheck."
		));
	}
	
	public static void addMacroToCompletions(Macro macro) {
		cp.addCompletion(new BasicCompletion(
				cp, macro.text, "2", generateHtmlSection(
						"Resolves to:", convertToHtml("[code]"+macro.replacement+"[/code]"))));
	}
	
	public static String generateSummary(Opcode o) {
		
		String syntax = (o.syntax == null ? "" : generateHtmlSection("Syntax : ", convertToHtml("[code]"+o.syntax+"[/code]")));
		String example = (o.example == null ? "" : generateHtmlSection("Example : ", convertToHtml(o.example)));
		String desc = (o.description == null ? "" : generateHtmlSection("Description : ", convertToHtml(o.description)));
		String compatibility = (o.compat == null ? "" : "<b>Compatibility : </b>" + convertToHtml(o.compat)+ "<br>");
		
		String result = syntax + desc + example + compatibility;
		
		//Hide opcodes without documentation
		//if (result.equals("")) return null;
		
		if (o.unicode != null && o.unicode.length() == 1) {
			result += "<b>Unicode : </b>U+"+Integer.toHexString(o.unicode.codePointAt(0))+"<br>";
		}
		
		result += "<b>Hex value : </b>0x"+o.hex;
		
		return result;
	}
		
	public static String generateHtmlSection(String title, String content) {
		return "<b>"+title+"</b><br>"+content+"<br><br>";
	}
	

	public static String relativeImgPath = BIDE.class.getResource("/doc/").toString();
	public static String convertToHtml(String str) {
		
		
		return str
				.replaceAll("&slash;", "")
				.replaceAll("&mult;", "")
				.replaceAll("&", "&amp;")
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;")
				.replaceAll("\\[i]", "<em>")
				.replaceAll("\\[/i]", "</em>")
				.replaceAll("\\[b]", "<b>")
				.replaceAll("\\[/b]", "</b>")
				.replaceAll("\\[code]", "<font face='DejaVu Avec Casio' size='12px'><span style='background-color:rgb(240,240,240);'>")
				.replaceAll("\\[/code]", "</span></font>")
				.replaceAll("(\\[img])([\\w/.]+)", "<img src='"+relativeImgPath+"$2")
				.replaceAll("\\[/img]", "'/>")
				.replaceAll(" {2}", "&nbsp;&nbsp;")
				.replaceAll("\\t", "&#09;")
				.replaceAll("\n", "<br>");
	}
}
