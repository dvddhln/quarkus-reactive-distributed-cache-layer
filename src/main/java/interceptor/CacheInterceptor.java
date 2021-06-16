package interceptor;

import io.smallrye.mutiny.Uni;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.exceptions.TransportException;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.jboss.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Interceptor
@Priority(1000)
@Cached(cacheName = "")
public class CacheInterceptor {


    @Inject
    RemoteCacheManager remoteCacheManager;

    @Inject
    Logger log;

    @ConfigProperty(name = "cache")
    String cacheConfiguration;

    @AroundInvoke
    <T> Object checkCache(InvocationContext context) throws Exception {
        Cached cachedAnnotation = context.getMethod().getAnnotation(Cached.class);
        String cacheName = cachedAnnotation.cacheName();
        String key = String.valueOf(context.getParameters()[0]);

        if (!cacheExists(cacheName)) {
            remoteCacheManager.administration().createCache(cacheName,
                    new XMLStringConfiguration(cacheConfiguration.replace("<name-override>", cacheName))
            );
        }

        return Uni.createFrom().completionStage(getValue(cacheName, key))
                .call(cachedValue -> {
                    if (cachedValue != null) {
                        return Uni.createFrom().item(cachedValue);
                    } else {
                        Uni<T> uniResponse = getMethodResponse(context);
                        return uniResponse.invoke(data -> putValue(cacheName, key, data));
                    }
                });

    }

    @SneakyThrows
    private <T> Uni<T> getMethodResponse(InvocationContext context) {
        return (Uni<T>) context.proceed();
    }

    private void putValue(String cacheName, String key, Object value) {
        log.debugf("Setting cached value for key: %s with value: %s", key, value);
        remoteCacheManager.getCache(cacheName).putAsync(key, value, 60, TimeUnit.SECONDS);
    }

    private CompletableFuture getValue(String cacheName, Object key) {
        log.debugf("Check cache for key: %s", key);
        return remoteCacheManager.getCache(cacheName).getAsync(key);
    }

    private boolean cacheExists(String cacheName) {
        try {
            if (remoteCacheManager.getCache(cacheName) == null) {
                log.warnf("Cache %s not present.", cacheName);
                return false;
            }
            return true;
        } catch (TransportException ex) {
            log.errorf("Error while trying to connect to the remote cache: %s", ex.getCause());
            return false;
        }
    }
}