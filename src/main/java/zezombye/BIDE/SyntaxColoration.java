package zezombye.BIDE;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;


public class SyntaxColoration extends AbstractTokenMaker {
	@Override
	public Token getTokenList(Segment text, int startTokenType, int startOffset) {
		   resetTokenList();

		   char[] array = text.array;
		   int offset = text.offset;
		   int count = text.count;
		   int end = offset + count;

		   int newStartOffset = startOffset - offset;

		   int currentTokenStart = offset;
		   int currentTokenType = startTokenType;

		   for (int i=offset; i<end; i++) {

		      char c = array[i];

		      switch (currentTokenType) {

		         case Token.NULL:

		            currentTokenStart = i;   // Starting a new token here.

					 switch (c) {
						 case ' ', '\t' -> currentTokenType = Token.WHITESPACE;
						 case '"' -> currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
						 case '#' -> currentTokenType = Token.COMMENT_EOL;
						 default -> {
							 if (RSyntaxUtilities.isDigit(c)) {
								 currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
								 break;
							 } else if (RSyntaxUtilities.isLetter(c) || c == '/' || c == '_') {
								 currentTokenType = Token.IDENTIFIER;
								 break;
							 }

							 // Anything not currently handled - mark as an identifier
							 currentTokenType = Token.IDENTIFIER;
						 }
					 }

					 break;

		         case Token.WHITESPACE:

		            switch (c) {

		               case ' ':
		               case '\t':
		                  break;   // Still whitespace.

		               case '"':
		                  addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
		                  break;

		               case '#':
		                  addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.COMMENT_EOL;
		                  break;

		               default:   // Add the whitespace token and start anew.

		                  addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;

		                  if (RSyntaxUtilities.isDigit(c)) {
		                     currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
		                     break;
		                  }
		                  else if (RSyntaxUtilities.isLetter(c) || c=='/' || c=='_') {
		                     currentTokenType = Token.IDENTIFIER;
		                     break;
		                  }

		                  // Anything not currently handled - mark as identifier
		                  currentTokenType = Token.IDENTIFIER;

		            } // End of switch (c).

		            break;

		         default: // Should never happen
		         case Token.IDENTIFIER:

		            switch (c) {

		               case ' ':
		               case '\t':
		                  addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.WHITESPACE;
		                  break;

		               case '"':
		                  addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
		                  currentTokenStart = i;
		                  currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
		                  break;

		               default:
		                  if (RSyntaxUtilities.isLetterOrDigit(c) || c=='/' || c=='_') {
		                     break;   // Still an identifier of some type.
		                  }
		                  // Otherwise, we're still an identifier (?).

		            } // End of switch (c).

		            break;

		         case Token.LITERAL_NUMBER_DECIMAL_INT:

					 switch (c) {
						 case ' ', '\t' -> {
							 addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
							 currentTokenStart = i;
							 currentTokenType = Token.WHITESPACE;
						 }
						 case '"' -> {
							 addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
							 currentTokenStart = i;
							 currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
						 }
						 default -> {
							 if (RSyntaxUtilities.isDigit(c)) {
								 break;   // Still a literal number.
							 }

							 // Otherwise, remember this was a number and start over.
							 addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
							 i--;
							 currentTokenType = Token.NULL;
						 }
					 }

					 break;

		         case Token.COMMENT_EOL:
		            i = end - 1;
		            addToken(text, currentTokenStart,i, currentTokenType, newStartOffset+currentTokenStart);
		            // We need to set token type to null so at the bottom we don't add one more token.
		            currentTokenType = Token.NULL;
		            break;

		         case Token.LITERAL_STRING_DOUBLE_QUOTE:
		            if (c=='"') {
		               addToken(text, currentTokenStart,i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset+currentTokenStart);
		               currentTokenType = Token.NULL;
		            }
		            break;

		      }

		   }

		switch (currentTokenType) {
			// Remember what token type to begin the next line with.
			case Token.LITERAL_STRING_DOUBLE_QUOTE -> addToken(
					text, currentTokenStart, end - 1, currentTokenType,
					newStartOffset + currentTokenStart
			);

			// Do nothing if everything was okay.
			case Token.NULL -> addNullToken();

			// All other token types don't continue to the next line...
			default -> {
				addToken(
						text, currentTokenStart, end - 1, currentTokenType,
						newStartOffset + currentTokenStart
				);
				addNullToken();
			}
		}

		   // Return the first token in our linked list.
		   return firstToken;

	}

	@Override
	public TokenMap getWordsToHighlight() {
		   TokenMap tokenMap = new TokenMap();
		   
		   tokenMap.put("case",  Token.RESERVED_WORD);
		   tokenMap.put("for",   Token.RESERVED_WORD);
		   tokenMap.put("if",    Token.RESERVED_WORD);
		   tokenMap.put("while", Token.RESERVED_WORD);
		  
		   tokenMap.put("printf", Token.FUNCTION);
		   tokenMap.put("scanf",  Token.FUNCTION);
		   tokenMap.put("fopen",  Token.FUNCTION);
		   
		   return tokenMap;
	}
	
}
