/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
