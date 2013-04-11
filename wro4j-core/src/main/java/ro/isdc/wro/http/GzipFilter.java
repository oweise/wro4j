package ro.isdc.wro.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.http.support.HttpHeader;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * A filter responsible for gzipping all content served through this filter.
 *
 * @author Alex Objelean
 * @created 11 Apr 2013
 * @since 1.6.4
 */
public class GzipFilter
    implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(GzipFilter.class);

  /**
   * {@inheritDoc}
   */
  public void init(final FilterConfig filterConfig)
      throws ServletException {
  }

  /**
   * {@inheritDoc}
   */
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    final ServletResponseWrapper responseWrapper = decorateResponse(request, response);
    chain.doFilter(req, responseWrapper);
    responseWrapper.flushBuffer();
    IOUtils.closeQuietly(response.getOutputStream());
    //chain.doFilter(request, response);
  }

  /**
   * Decorates the provided {@link HttpServletResponse} which handles gzip if it is allowed.
   */
  private HttpServletResponseWrapper decorateResponse(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(response);
    if (isGzipAllowed(request)) {
      LOG.debug("setting gzip header");
      response.setHeader(HttpHeader.CONTENT_ENCODING.toString(), "gzip");
      // Create a gzip stream
      final GZIPOutputStream gzout = new GZIPOutputStream(new ByteArrayOutputStream());
      // Handle the request

      wrappedResponse = new HttpServletResponseWrapper(wrappedResponse) {
        @Override
        public ServletOutputStream getOutputStream()
            throws IOException {
          return new DelegatingServletOutputStream(gzout);
        }
      };
    }
    return wrappedResponse;
  }

  /**
   * Checks if the request supports gzip and is not a include request (these cannot be gzipped)
   */
  private boolean isGzipAllowed(final HttpServletRequest request) {
    return !DispatcherStreamLocator.isIncludedRequest(request) && WroUtil.isGzipSupported(request);
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
  }

}
