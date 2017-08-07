package dk.magenta.datafordeler.core;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;

public class LarsURLClassLoader extends URLClassLoader {
    public LarsURLClassLoader(ClassLoader parent) {
        super(getUrls(), parent);
    }

    public LarsURLClassLoader() {
        super(getUrls());
    }

    public LarsURLClassLoader(ClassLoader parent, URLStreamHandlerFactory factory) {
        super(getUrls(), parent, factory);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("LARS "+name);
        return super.findClass(name);
    }

    private static URL[] getUrls() {
        return getPluginJars(getPluginFolder()).toArray(new URL[0]);
    }

    private static File getPluginFolder() {
        return new File("/home/lars/Projekt/datafordeler/plugins/jar/");
    }

    private static List<URL> getPluginJars(File folder) {
        ArrayList<URL> jars = new ArrayList<>();
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        if (files != null) {
            for (File file : files) {
                try {
                    jars.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return jars;
    }

}
