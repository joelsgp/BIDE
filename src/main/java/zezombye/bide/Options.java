package zezombye.bide;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.stream.Collectors;


public class Options {

    public static final String VERSION = "1.0";
    Properties options = new Properties();

    public void loadProperties() {

        try {
            options.load(new FileInputStream(BIDE.pathToOptions));
        } catch (FileNotFoundException e) {
            System.out.println("No options.txt file found, creating it at "+BIDE.pathToOptions);
            initProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (options.getProperty("version") == null || options.getProperty("version").compareTo(Options.VERSION) < 0) {
            System.out.println("Your option file is outdated, it has been replaced by the default file. Your options have been saved in the file options.txt.backup.");
            try {
                Files.copy(Paths.get(BIDE.pathToOptions), Paths.get(BIDE.pathToOptions+".backup"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            initProperties();
        }
    }

    public String getProperty(String prop) {
        if (options.getProperty(prop) == null) {
            BIDE.error("Could not find property "+prop);
        }
        return options.getProperty(prop);
    }

    public void setProperty(String prop, String value) {
        options.setProperty(prop, value);
    }

    public void initProperties() {
        try {
            IO.writeStrToFile(
                    new File(BIDE.pathToOptions),
                    new BufferedReader(
                            new InputStreamReader(
                                    BIDE.class.getResourceAsStream("/options.txt"))).lines().collect(Collectors.joining("\n")), true);
            loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
