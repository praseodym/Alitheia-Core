package eu.sqooss.service.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import org.osgi.framework.BundleContext;


import org.springframework.beans.factory.DisposableBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CacheServiceImpl implements CacheService, DisposableBean {

    public static final String CACHE_IMPL = "eu.sqooss.service.cache.OnDiskCache";
    
    private static List<Class<? extends CacheService>> impls;
    
    static {
        impls = new ArrayList<Class<? extends CacheService>>();
        impls.add(OnDiskCache.class);
        impls.add(InMemoryCache.class);
    }
  
    private CacheService c;
    @Autowired
    private BundleContext bc;
    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);
    
    public CacheServiceImpl() {
        String impl = System.getProperty(CACHE_IMPL);

        if (impl == null)
            impl = "eu.sqooss.service.cache.OnDiskCache";

        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(impl);
            c = (CacheService) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("Cannot load cache implementation:" + impl);
        } catch (InstantiationException e) {
            logger.error("Cannot initialize cache implementation:" + impl + " Error:" + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("Cannot initialize cache implementation:" + impl + " Error:" + e.getMessage());
        }
    }
    
    @Override
    public byte[] get(String key) {
        return c.get(key);
    }
    
    @Override
    public InputStream getStream(String key) {
        byte[] buff = c.get(key);
        
        if (buff == null)
            return null;
        
        ByteArrayInputStream bais = new ByteArrayInputStream(buff);
        return bais;
    }

    @Override
    public void set(String key, byte[] data) {
        c.set(key, data);
    }

    @Override
    public void setStream(String key, InputStream in) {
        try {
            int nRead;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];

            while ((nRead = in.read(data, 0, data.length)) != -1) {
              buffer.write(data, 0, nRead);
            }

            buffer.flush();            
            set(key, buffer.toByteArray());
            
        } catch (IOException e) {
            logger.error("Error");
        }
    }

    @Override
    public void destroy() {
        c = null;
    }
}
