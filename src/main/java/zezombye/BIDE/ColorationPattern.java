package zezombye.BIDE;
import java.awt.Color;
import java.util.regex.Pattern;

public class ColorationPattern {
	public Pattern pattern;
	public Color color;
	public boolean isBold;
	
	public ColorationPattern(String regex, Color color, boolean isBold) {
		this.pattern = Pattern.compile(regex);
		this.color = color;
		this.isBold = isBold;
	}
	
	public ColorationPattern(String[] words, boolean needWordBoundaries, Color color, boolean isBold) {
		StringBuilder regex = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			if (needWordBoundaries) 
				regex.append("(?<=\\W)");
			else 
				regex.append("\\Q");
			regex.append(words[i]);
			if (needWordBoundaries)
				regex.append("(?=\\W)");
			else
				regex.append("\\E");
			if (i < words.length - 1) {
				regex.append("|");
			}
		}
		this.pattern = Pattern.compile(regex.toString());
		this.color = color;
		this.isBold = isBold;
	}
	
}
