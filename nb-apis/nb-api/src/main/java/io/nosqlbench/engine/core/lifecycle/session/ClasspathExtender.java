package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.system.NBEnvironment;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClasspathExtender {
  private final Set<URL> jarUrls = new HashSet<>();

  public ClasspathExtender() {}

  public void extend() {

    final List<String> libpaths =
        NBEnvironment.INSTANCE.interpolateEach(":", '$' + NBEnvironment.NBLIBS);
    Set<URL> urlsToAdd = new HashSet<>();

    for (final String libpaths_entry : libpaths) {
      final Path libpath = Path.of(libpaths_entry);
      if (Files.isDirectory(libpath))
        urlsToAdd = addLibDir(urlsToAdd, libpath);
      else if (Files.isRegularFile(libpath) && libpath.toString().toLowerCase().endsWith(".zip"))
        urlsToAdd = addZipDir(urlsToAdd, libpath);
      else if (Files.isRegularFile(libpath) && libpath.toString().toLowerCase().endsWith(".jar"))
        urlsToAdd = addJarFile(urlsToAdd, libpath);
    }
    extendClassLoader(urlsToAdd);

  }

  private void extendClassLoader(final Set<URL> urls) {
    final Set<URL> newUrls = new HashSet<>();
    if (!jarUrls.containsAll(urls)) {
      for (final URL url : urls)
        if (!jarUrls.contains(url)) {
          newUrls.add(url);
          jarUrls.add(url);
        }
      final URL[] newUrlAry = newUrls.toArray(new URL[]{});
      final URLClassLoader ucl =
          URLClassLoader.newInstance(newUrlAry, Thread.currentThread().getContextClassLoader());
      Thread.currentThread().setContextClassLoader(ucl);
      System.err.println("Extended class loader layering with " + newUrls);
    } else
      System.err.println("All URLs specified were already in a class loader.");
  }


  private Set<URL> addJarFile(final Set<URL> urls, final Path libpath) {
    try {
      urls.add(libpath.toUri().toURL());
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return urls;
  }

  private Set<URL> addZipDir(final Set<URL> urlsToAdd, final Path libpath) {
    return urlsToAdd;
  }

  private Set<URL> addLibDir(final Set<URL> urlsToAdd, final Path libpath) {
    final Set<URL> urls =
        NBIO.local().searchPrefixes(libpath.toString()).extensionSet(".jar").list().stream()
            .map(Content::getURL).collect(Collectors.toSet());
    urlsToAdd.addAll(urls);
    return urlsToAdd;
  }


}
