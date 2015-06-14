package ro.isdc.wro.extensions.processor.support.sass;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

public class RubySassImporter {
    
    public static class ImporterContext {
        
        List<URI> searchPaths;
        public List<URI> getSearchPaths() {
            return searchPaths;
        }
        public void setSearchPaths(List<URI> searchPaths) {
            this.searchPaths = searchPaths;
        }
        public UriLocatorFactory getLocatorFactory() {
            return locatorFactory;
        }
        public void setLocatorFactory(UriLocatorFactory locatorFactory) {
            this.locatorFactory = locatorFactory;
        }
        public String getEncoding() {
            return encoding;
        }
        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }
        UriLocatorFactory locatorFactory;
        String encoding;
        
    }
    
    class TargetResource {
        public TargetResource(URI uri, String code) {
            super();
            this.uri = uri;
            this.code = code;
        }
        public String getCode() {
            return code;
        }
        private URI uri;
        String code;
        public URI getUri() {
            return uri;
        }
        
    }
    
    public RubySassImporter(String path) {
    }
    
    public Object find(String uri, RubyHash options) throws IOException, URISyntaxException {
        
        Ruby runtime = org.jruby.Ruby.getGlobalRuntime();
        ImporterContext context =  (ImporterContext) options.get(org.jruby.RubySymbol.newSymbol(runtime, "filesystem_importer_context"));
        if (context == null) {
            throw new IllegalStateException("No importer context");
        }
        
        TargetResource foundResource = findResource(context, uri + ".scss");
        if (foundResource == null && uri.indexOf("/") == -1) {
            foundResource = findResource(context, "_" + uri + ".scss");
        }
        
        if (foundResource != null) {
            IRubyObject rubyClass = JavaEmbedUtils.newRuntimeAdapter().eval(runtime, "Sass::Engine");
            @SuppressWarnings("unchecked")
            Map<Object,Object> targetOptions = new HashMap<Object,Object>(options);
            targetOptions.put(org.jruby.RubySymbol.newSymbol(runtime, "filename"), foundResource.getUri().toString());
            Object[] parameters = {foundResource.getCode(), targetOptions};
            return JavaEmbedUtils.invokeMethod(runtime, rubyClass, "new", parameters, IRubyObject.class);
        }
        else {
            return null;
        }
        
    }

    private TargetResource findResource(ImporterContext cx, String uri) throws URISyntaxException {

        for (URI searchURI : cx.getSearchPaths()) {
            URI targetURI = searchURI.resolve(uri);
            try {
                InputStream data = cx.getLocatorFactory().locate(targetURI.toString());
                return new TargetResource(targetURI, IOUtils.toString(data, cx.getEncoding()));
            }
            catch (IOException e) {
            }
        }
        
        return null;
    
    }

    public Date mtime(String uri,RubyHash options) {
        
        Ruby runtime = org.jruby.Ruby.getGlobalRuntime();
//        WGA wga = (WGA) options.get(org.jruby.RubySymbol.newSymbol(runtime, "wga"));
//        Design design = (Design) options.get(org.jruby.RubySymbol.newSymbol(runtime, "wgaDesign"));
//        PostProcessResult result = (PostProcessResult) options.get(org.jruby.RubySymbol.newSymbol(runtime, "wgaResult"));

        return new Date();
    }
    
    public Object key(String uri, RubyHash options) throws IOException, URISyntaxException {
        Ruby runtime = org.jruby.Ruby.getGlobalRuntime();
        ImporterContext context =  (ImporterContext) options.get(org.jruby.RubySymbol.newSymbol(runtime, "filesystem_importer_context"));
        if (context == null) {
            throw new IllegalStateException("No importer context");
        }
        
        TargetResource foundResource = findResource(context, uri);
        if (foundResource == null) {
            return null;
        }
        
        String uriStr = foundResource.getUri().toString();
        int lastSlash = uriStr.lastIndexOf("/");
        String path;
        String fileName;
        if (lastSlash != -1) {
            path = uriStr.substring(0, lastSlash);
            fileName = uriStr.substring(lastSlash + 1);
        }
        else {
            path = "";
            fileName = uriStr;
        }
            
        List<IRubyObject> rvList = new ArrayList<IRubyObject>();
        rvList.add(JavaEmbedUtils.javaToRuby(runtime, path));
        rvList.add(JavaEmbedUtils.javaToRuby(runtime, fileName));
        return runtime.newArray(rvList);

    }
    
    public Object find_relative(String uri, String base, RubyHash options) throws IOException, URISyntaxException {
        
        URI baseUri = URI.create(base);
        URI uriUri = baseUri.resolve(baseUri.toString());
        return find(uriUri.toString(), options);
        
    }

}
