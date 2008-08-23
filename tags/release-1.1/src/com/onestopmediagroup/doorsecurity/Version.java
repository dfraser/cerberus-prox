package com.onestopmediagroup.doorsecurity;


import java.util.Properties;

public class Version {

    /**
     * The version number of Cerberus-Prox in this classloader context.
     */
    public static final String VERSION;
    static {
        try {
            Properties props = new Properties();
            props.load(Version.class.getResourceAsStream("version.properties"));
            VERSION = props.getProperty("cerberus-prox.version");
        } catch (Exception e) {
            throw new RuntimeException("Failed to read version from classpath resource", e);
        }
    }
}
