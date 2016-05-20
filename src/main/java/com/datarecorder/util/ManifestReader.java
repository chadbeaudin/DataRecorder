package com.datarecorder.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ManifestReader {

    private String ciBuildVersion;
    private String ciBuildId;
    private String ciBuildJobName;
    private String ciBuildCmRevision;

    public ManifestReader(final Class clazz) {
        try {
            final File file = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
            final JarFile jarFile = new JarFile(file);
            final Manifest manifest = jarFile.getManifest();
            final Attributes attribs = manifest.getMainAttributes();
            ciBuildVersion = attribs.getValue("CI-Build-Version");
            ciBuildId = attribs.getValue("CI-Build-ID");
            ciBuildJobName = attribs.getValue("CI-Build-Job-Name");
            ciBuildCmRevision = attribs.getValue("CI-Build-CM-Revision");
            System.out.println("ciBuildVersion: " + ciBuildVersion);
            System.out.println("ciBuildId: " + ciBuildId);
            System.out.println("ciBuildJobName: " + ciBuildJobName);
            System.out.println("ciBuildCmRevision: " + ciBuildCmRevision);

        } catch (final URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     * @return the ciBuildVersion
     */
    public String getCiBuildVersion() {
        return ciBuildVersion;
    }

    /**
     * @return the ciBuildId
     */
    public String getCiBuildId() {
        return ciBuildId;
    }

    /**
     * @return the ciBuildJobName
     */
    public String getCiBuildJobName() {
        return ciBuildJobName;
    }

    /**
     * @return the ciBuildCmRevision
     */
    public String getCiBuildCmRevision() {
        return ciBuildCmRevision;
    }

}
