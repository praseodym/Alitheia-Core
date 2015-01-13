package eu.sqooss.core;

import org.osgi.framework.BundleContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Spring Configuration
 */
@Configuration
@Lazy
@ComponentScan("eu.sqooss")
public class SpringApplication {
    public static class BundleContextHolder {
        private BundleContext bundleContext;

        public void setBundleContext(BundleContext bundleContext) {
            assert this.bundleContext == null;
            this.bundleContext = bundleContext;
        }

        public BundleContext getBundleContext() {
            assert bundleContext != null;
            return bundleContext;
        }
    }

    @Bean
    BundleContextHolder bundleContextHolder() {
        return new BundleContextHolder();
    }

    @Bean
    BundleContext bundleContext() {
        return bundleContextHolder().getBundleContext();
    }
}
