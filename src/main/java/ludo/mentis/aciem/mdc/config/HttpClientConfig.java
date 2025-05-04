package ludo.mentis.aciem.mdc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;

@Configuration
public class HttpClientConfig {

    private static final Logger log = LoggerFactory.getLogger(HttpClientConfig.class);

    @Bean
    public HttpClient httpClient(HttpClientProperties properties) {
        log.info("Configuring custom HttpClient bean...");
        properties.getProxy().validate();
        try {
            var builder = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()));
            configureProxy(builder, properties.getProxy());
            configureSsl(builder, properties.getSsl());
            var client = builder.build();
            log.info("Custom HttpClient bean configured successfully.");
            return client;
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | KeyManagementException e) {
            // Fail fast during startup if configuration is invalid
            throw new IllegalStateException("Failed to configure HttpClient", e);
        }
    }

    private void configureProxy(HttpClient.Builder builder, HttpClientProperties.ProxyProperties proxyProps) {
        if (proxyProps.isEnabled()) {
            log.info("Configuring HttpClient with proxy: {}:{}", proxyProps.getHost(), proxyProps.getPort());
            builder.proxy(ProxySelector.of(new InetSocketAddress(proxyProps.getHost(), proxyProps.getPort())));
        } else {
            log.info("Configuring HttpClient with default system proxy selector.");
            // Use system default proxy settings (reads http.proxyHost/Port etc.)
            builder.proxy(ProxySelector.getDefault());
        }
    }

    private void configureSsl(HttpClient.Builder builder, HttpClientProperties.SslProperties sslProps)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, KeyManagementException {
        SSLContext sslContext;
        String caCertPath = sslProps.getCustomCaCertificatePath();
        if (StringUtils.hasText(caCertPath)) {
            log.info("Configuring HttpClient with custom SSL Context using CA: {}", caCertPath);
            sslContext = createCustomSslContext(caCertPath);
        } else {
            log.info("Configuring HttpClient with default SSL Context.");
            // Use default SSL context - WARNING: May fail with SSL interception
            sslContext = SSLContext.getDefault();
            log.warn("Using default SSLContext. This might not work correctly behind corporate firewalls performing " +
                    "SSL interception unless the intercepting CA certificate is in the default Java truststore.");
        }
        builder.sslContext(sslContext);
    }

    /**
     * Creates an SSLContext that trusts certificates signed by the CA specified by a path.
     * Moved from a static method to the instance method of the configuration class.
     */
    private SSLContext createCustomSslContext(String caCertificatePath)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException {
        var certificateFactory = CertificateFactory.getInstance("X.509");
        var caCertPath = Path.of(caCertificatePath);
        if (!Files.isReadable(caCertPath)) {
            throw new IOException("Custom CA certificate file not found or not readable: " + caCertPath);
        }

        X509Certificate caCert;
        try (var caInput = Files.newInputStream(caCertPath)) {
            caCert = (X509Certificate) certificateFactory.generateCertificate(caInput);
            if (caCert == null) {
                throw new CertificateException("Failed to load certificate from file: " + caCertPath + ". Is it a valid X.509 certificate?");
            }
        }
        log.debug("Successfully loaded custom CA certificate: {}", caCert.getSubjectX500Principal());

        // Create an in-memory KeyStore containing only the custom trusted CA certificate
        var customTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        customTrustStore.load(null, null); // Initialize empty
        customTrustStore.setCertificateEntry("customCaCert", caCert);
        log.debug("Created in-memory trust store with custom CA.");

        // Create a TrustManagerFactory that uses the custom KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(customTrustStore);
        log.debug("Initialized TrustManagerFactory with custom trust store.");

        // Create an SSLContext initialized with the custom TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(null, tmf.getTrustManagers(), null);
        log.debug("Initialized SSLContext with custom TrustManagers.");

        return sslContext;
    }
}