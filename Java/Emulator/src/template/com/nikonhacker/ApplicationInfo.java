/* The real source is in source/template and is preprocessed during the build to produce a version in ant-gen */
package com.nikonhacker;

public class ApplicationInfo {
    public static String getName() {
        return "@APPNAME@";
    }
    public static String getVersion() {
        return "@APPVERSION@";
    }
    public static String getBuildTime() {
        return "@BUILDTIME@";
    }
}

