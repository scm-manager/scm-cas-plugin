package com.cloudogu.scm.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.Stage;
import sonia.scm.plugin.Extension;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

@Extension
public class DevelopmentContextListener implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(DevelopmentContextListener.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    if (isDevelopmentStageActive()) {
      LOG.warn("disable ssl certificate validation for https connection");
      LOG.warn("do never use this in production, be sure scm-manager run in stage production");
      disableCertificateValidation();
    }
  }

  private void disableCertificateValidation() {
    TrustManager[] trustAllCerts = createTrustAllCertsManager();
    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (GeneralSecurityException ex) {
      LOG.warn("failed to disable certificate validation", ex);
    }
  }

  private TrustManager[] createTrustAllCertsManager() {
    return new TrustManager[] {
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
      }
    };
  }

  private boolean isDevelopmentStageActive() {
    return SCMContext.getContext().getStage() == Stage.DEVELOPMENT;
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // nothing to do
  }
}
