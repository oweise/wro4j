/*
 * Copyright (c) 2012. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sommeri.less4j.LessCompiler.Problem;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.sass.RubySassEngine;
import ro.isdc.wro.extensions.processor.support.sass.RubySassImporter;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.WroUtil;


/**
 * A processor using the ruby sass engine:
 *
 * @author Simon van der Sluis
 * @since 1.4.6
 * @created 2/03/12
 */
@SupportedResourceType(ResourceType.CSS)
public class RubySassCssProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, Destroyable {
    
  private static final Logger LOG = LoggerFactory.getLogger(RubySassCssProcessor.class);
  public static final String ALIAS = "rubySassCss";
  private ObjectPoolHelper<RubySassEngine> enginePool;

  @Inject
  private UriLocatorFactory locatorFactory;
  
  @Inject
  private WroConfiguration wroConfiguration;
  
  public RubySassCssProcessor() {
    enginePool = new ObjectPoolHelper<RubySassEngine>(new ObjectFactory<RubySassEngine>() {
      @Override
      public RubySassEngine create() {
        return newEngine();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
      
    final String content = IOUtils.toString(reader);
    RubySassImporter.ImporterContext cx = new RubySassImporter.ImporterContext();
    try {
        cx.setLocatorFactory(locatorFactory);
        List<URI> searchPaths = new ArrayList<URI>();
        if (resource != null) {
            searchPaths.add(new URI(resource.getUri()).resolve("."));
        }
        searchPaths.add(new File(System.getProperty("user.dir")).toURI());
        cx.setSearchPaths(searchPaths);
        cx.setEncoding(wroConfiguration.getEncoding());
    }
    catch (URISyntaxException e) {
        LOG.error("Failed to resolve load paths for SASS");
        throw WroRuntimeException.wrap(e);
    }
    
    final RubySassEngine engine = enginePool.getObject();
    try {
      writer.write(engine.process(content, cx));
    } catch (final WroRuntimeException e) {
      onException(e);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
      enginePool.returnObject(engine);
    }
  }

  /**
   * Invoked when a processing exception occurs. By default propagates the runtime exception.
   */
  protected void onException(final WroRuntimeException e) {
    throw e;
  }

  /**
   * A getter used for lazy loading.
   * @deprecated use {@link #newEngine()} instead.
   */
  @Deprecated
  protected RubySassEngine getEngine() {
      return newEngine();
  }

  /**
   * @return a fresh instance of {@link RubySassEngine}
   */
  protected RubySassEngine newEngine() {
    return new RubySassEngine();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  @Override
  public void destroy() throws Exception {
    enginePool.destroy();
  }
}
