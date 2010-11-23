/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SimpleProcessorsFactory;
import ro.isdc.wro.util.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;

/**
 * AbstractWroTest.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public abstract class AbstractWroTest {
  /**
   * UriLocator Factory.
   */
  private final UriLocatorFactory uriLocatorFactory = createDefaultUriLocatorFactory();

  private UriLocatorFactory createDefaultUriLocatorFactory() {
    return new SimpleUriLocatorFactory().addUriLocator(new ServletContextUriLocator()).addUriLocator(
        new ClasspathUriLocator()).addUriLocator(new UrlUriLocator());
  }
  /**
   * @return the injector
   */
  protected final void initProcessor(final ResourcePreProcessor processor) {
    final Injector injector = new Injector(uriLocatorFactory, new SimpleProcessorsFactory().addPreProcessor(processor));
    injector.inject(processor);
  }

  /**
   * @return the injector
   */
  protected final void initProcessor(final ResourcePostProcessor processor) {
    final Injector injector = new Injector(uriLocatorFactory, new SimpleProcessorsFactory().addPostProcessor(processor));
    injector.inject(processor);
  }

  /**
   * Compare contents of two resources (files) by performing some sort of
   * processing on input resource.
   *
   * @param inputResourceUri
   *          uri of the resource to process.
   * @param expectedContentResourceUri
   *          uri of the resource to compare with processed content.
   * @param processor
   *          a closure used to process somehow the input content.
   */
  protected void compareProcessedResourceContents(
      final String inputResourceUri, final String expectedContentResourceUri,
      final ResourceProcessor processor) throws IOException {
    final Reader resultReader = getReaderFromUri(inputResourceUri);
    final Reader expectedReader = getReaderFromUri(expectedContentResourceUri);
    WroTestUtils.compare(resultReader, expectedReader,
        processor);
  }

  private Reader getReaderFromUri(final String uri) throws IOException {
    // wrap reader with bufferedReader for top efficiency
    return new BufferedReader(new InputStreamReader(uriLocatorFactory.locate(uri)));
  }

  public InputStream getInputStream(final String uri) throws IOException {
    return uriLocatorFactory.locate(uri);
  }
}
