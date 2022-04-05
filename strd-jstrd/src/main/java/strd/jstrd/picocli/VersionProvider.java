package strd.jstrd.picocli;

import picocli.CommandLine;

import java.io.InputStream;
import java.util.Properties;

public class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        Properties properties = new Properties();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/application.properties");
        properties.load(resourceAsStream);
        String version = properties.getProperty("project.version");

        if (version.contains("SNAPSHOT")) {
            String time = properties.getProperty("git.build.time");
            String commit = properties.getProperty("git.commit.id");
            return new String[]{
                    "version: "+version,
                    "\tbuild time: "+time,
                    "\tcommit id: "+commit
            };
        } else{
            return new String[]{version};
        }



    }
}