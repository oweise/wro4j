package ro.isdc.wro.extensions.processor.support.sass;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;


/**
 * @author Dmitry Erman
 */
public class TestRubySassEngine {
  private RubySassEngine engine;
  private RubySassImporter.ImporterContext context;
  @Inject
  private UriLocatorFactory uriLocatorFactory;


  @Before
  public void setUp() {
    engine = new RubySassEngine();
    context = new RubySassImporter.ImporterContext();
    context.setEncoding("UTF-8");
    context.setLocatorFactory(uriLocatorFactory);
    context.setSearchPaths(Collections.singletonList(new File(System.getProperty("user.dir")).toURI()));
  }


  @Test
  public void shouldReturnEmptyStringWhenNullContentIsProcessed() {
    assertEquals(StringUtils.EMPTY, engine.process(null, context));
  }


  @Test
  public void shouldReturnEmptyStringWhenEmptyContentIsProcessed() {
    assertEquals(StringUtils.EMPTY, engine.process("", context));
  }


  @Test(expected = WroRuntimeException.class)
  public void cannotProcessInvalidCss() {
    assertEquals(StringUtils.EMPTY, engine.process("invalidCss", context));
  }


  @Test
  public void shouldProcessValidCss()
    throws IOException {
    assertEquals("#element {\n  color: red; }\n", engine.process("#element {color: red;}", context));
  }

  @Test
  public void shouldProcessValidSass()
    throws IOException {
    assertEquals("#element #child {\n  color: red; }\n", engine.process("#element { #child {color: red;}}", context));
  }

  @Test
  public void shouldProcessValidNonAsciiSass()
    throws IOException {
    assertEquals("@charset \"UTF-8\";\n#element {\n  font-family: \"\uFF2D\uFF33 \uFF30\u30B4\u30B7\u30C3\u30AF\"; }\n",
        engine.process("#element {font-family: \"\uFF2D\uFF33 \uFF30\u30B4\u30B7\u30C3\u30AF\";}", context));
  }
}
