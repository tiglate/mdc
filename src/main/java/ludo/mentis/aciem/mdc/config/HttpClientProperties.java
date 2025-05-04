package ludo.mentis.aciem.mdc.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "file-downloader.http-client")
public class HttpClientProperties {

    /**
     * Connection timeout in seconds.
     */
    @Min(1)
    private int connectTimeoutSeconds = 20;

    /**
     * Default request timeout in minutes for memory downloads.
     */
    @Min(1)
    private int requestTimeoutMinutes = 5;

    /**
     * Request timeout in minutes specifically for downloading to a file (can be longer).
     */
    @Min(1)
    private int fileRequestTimeoutMinutes = 30;

    private SslProperties ssl;

    private ProxyProperties proxy;

    public HttpClientProperties() {
        ssl = new SslProperties();
        proxy = new ProxyProperties();
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int value) {
        this.connectTimeoutSeconds = value;
    }

    public int getRequestTimeoutMinutes() {
        return requestTimeoutMinutes;
    }

    public void setRequestTimeoutMinutes(int value) {
        this.requestTimeoutMinutes = value;
    }

    public int getFileRequestTimeoutMinutes() {
        return fileRequestTimeoutMinutes;
    }

    public void setFileRequestTimeoutMinutes(int value) {
        this.fileRequestTimeoutMinutes = value;
    }

    public ProxyProperties getProxy() {
        return proxy;
    }

    public SslProperties getSsl() {
        return ssl;
    }

    public void setSsl(SslProperties ssl) {
        this.ssl = ssl;
    }

    public void setProxy(ProxyProperties proxy) {
        this.proxy = proxy;
    }

    public static class ProxyProperties {
        private boolean enabled = false;
        private String host;
        private int port;

        public void validate() {
            if (enabled && (host == null || host.isBlank() || port <= 0)) {
                throw new IllegalArgumentException("Proxy host and port must be set when proxy is enabled.");
            }
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class SslProperties {
        /**
         * Path to the custom CA certificate file (e.g., PEM/CRT/CER format).
         * If provided, HttpClient will be configured to trust this CA, necessary for SSL interception.
         * If blank or null, the default Java truststore will be used.
         */
        private String customCaCertificatePath;

        public String getCustomCaCertificatePath() {
            return customCaCertificatePath;
        }

        public void setCustomCaCertificatePath(String value) {
            this.customCaCertificatePath = value;
        }
    }
}