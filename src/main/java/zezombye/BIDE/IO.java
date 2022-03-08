package zezombye.BIDE;

// Taken from B2C

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


/**
 * ONLY EVER USE CasioStrings WHEN HANDLING CASIO ENCODING!
 * The reason is that Strings use UTF-16, and invalid encodings are replaced by '?'
 * Try to create the string with byte[]{0xAA, 0xAC, 0xBD, 0xAF, 0x90, 0x88, 0x9A, 0x8D}.
 * You'll see that some characters are replaced by '?'.
 */


public class IO {
	public static void writeToFile(File file, List<Byte> content, boolean deleteFile) {
		try {
			if (deleteFile) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
			// Convert to byte[]
			byte[] result = new byte[content.size()];
			for(int i = 0; i < content.size(); i++) {
			    result[i] = content.get(i);
			}
			out.write(result);
			out.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	// Only use this for non-casio strings (ascii text)!
	public static void writeStrToFile(File file, String content, boolean deleteFile) throws IOException {
		if (deleteFile) {
			file.delete();
		}
		file.getParentFile().mkdirs();
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
		writer.write(content);
		writer.close();
	}

	public static CasioString readFromFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new CasioString(encoded);
	}
}
