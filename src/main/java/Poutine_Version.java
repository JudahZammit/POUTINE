import picocli.CommandLine.IVersionProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Single source of the program version: the Maven project version, written into poutine.properties at build time.
 * Used for "--version" and for the session log, so the launcher, the jar and the logs cannot disagree.
 */
public class Poutine_Version implements IVersionProvider {

    private static final String UNKNOWN = "unknown (not built with Maven)";

    private static final String VERSION = read_version();


    private static String read_version() {
        try (InputStream in = Poutine_Version.class.getResourceAsStream("/poutine.properties")) {
            if (in == null) {
                return UNKNOWN;
            }
            Properties properties = new Properties();
            properties.load(in);
            String version = properties.getProperty("version");
            // an unfiltered file still contains the placeholder
            return (version == null || version.startsWith("${")) ? UNKNOWN : version;
        } catch (IOException e) {
            return UNKNOWN;
        }
    }


    public static String get() {
        return VERSION;
    }


    @Override
    public String[] getVersion() {
        return new String[]{"%nPOUTINE " + VERSION + "%n"};
    }
}
