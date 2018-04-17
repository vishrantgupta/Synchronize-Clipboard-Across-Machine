/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vishrant.cbsync.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HTTPUtilities {

    static SSLContext context;
    // always verify the host - dont check for certificate
    private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static String sendPostJsonRequest(String post_url, String json) {
        return sendPostRequest(post_url, json, 10000, 300000);
    }

    public static String sendGETRequest(String urlString) {
        return sendGETRequest(urlString, 10000, 300000);
    }

    /**
     *
     */
    public static String sendPostRequest(String urlString, String json, int timeout, int readTimeout) {
        String response = null;
        try {

            // SSLContext context = getHttpContext();
            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = null;

            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                urlConnection = https;
//				urlConnection = setUpHttpsConnection(urlString);
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }

            // urlConnection.setSSLSocketFactory(context.getSocketFactory());
            try {
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);

                urlConnection.setRequestProperty("content-type", "application/json");
                urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                urlConnection.setRequestProperty("Accept-Encoding", "gzip");
                urlConnection.setRequestProperty("Accept", "*/*");

                urlConnection.setRequestMethod("POST");
                // urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                // urlConnection.setRequestProperty("Accept-Encoding", "gzip");
                urlConnection.setConnectTimeout(timeout);
                urlConnection.setReadTimeout(readTimeout);

                // Send post request
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.write(json.getBytes("UTF-8"));
                wr.flush();
                wr.close();

                // InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                InputStream inputStream = urlConnection.getInputStream();

                // check if we got a compressed resonse back
                if ("gzip".equalsIgnoreCase(urlConnection.getContentEncoding())) {
                    response = uncompressInputStream(inputStream);
                } else {
                    response = convertStreamToString(inputStream);
                }
            } finally {
                urlConnection.disconnect();
            }

        } catch (Exception ex) {
            System.out.println("Failed to establish SSL connection to server: " + ex.toString());
        }

        return response;
    }

    public static String sendGETRequest(String urlString, int timeout, int readTimeout) {
        String response = null;
        try {

            // SSLContext context = getHttpContext();
            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = null;

            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                urlConnection = https;
//				urlConnection = setUpHttpsConnection(urlString);
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }

            try {

                // add reuqest header
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                urlConnection.setRequestProperty("Accept-Encoding", "gzip");
                urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.setConnectTimeout(timeout);
                urlConnection.setReadTimeout(readTimeout);

                InputStream inputStream = urlConnection.getInputStream();

                // check if we got a compressed resonse back
                if ("gzip".equalsIgnoreCase(urlConnection.getContentEncoding())) {
                    response = uncompressInputStream(inputStream);
                } else {
                    response = convertStreamToString(inputStream);
                }
            } finally {
                urlConnection.disconnect();
            }

        } catch (Exception ex) {
            System.out.println("Failed to establish SSL connection to server: " + ex.toString());
        }

        return response;
    }

    private static SSLContext getHttpContext() throws Exception {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // My CRT file that I put in the assets folder
        // I got this file by following these steps:
        // * Go to https://littlesvr.ca using Firefox
        // * Click the padlock/More/Security/View Certificate/Details/Export
        // * Saved the file as littlesvr.crt (type X.509 Certificate (PEM))
        FileInputStream caInput = new FileInputStream("social_group_com.crt");
//		InputStream caInput = new BufferedInputStream(Constants.assetManager.open("social_group_com.crt"));
        Certificate ca = cf.generateCertificate(caInput);
        System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        return context;
    }

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String convertStreamToString(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String bufferedStrChunk = null;

            while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
                stringBuilder.append(bufferedStrChunk);
            }

            return stringBuilder.toString();
        } finally {
            inputStreamReader.close();
            bufferedReader.close();
        }
    }

    private static String uncompressInputStream(InputStream inputStream) throws IOException {
        StringBuilder value = new StringBuilder();

        GZIPInputStream gzipIn = null;
        InputStreamReader inputReader = null;
        BufferedReader reader = null;

        try {
            gzipIn = new GZIPInputStream(inputStream);
            inputReader = new InputStreamReader(gzipIn, "UTF-8");
            reader = new BufferedReader(inputReader);

            String line = "";
            while ((line = reader.readLine()) != null) {
                value.append(line).append("\n");
            }
        } finally {
            try {
                if (gzipIn != null) {
                    gzipIn.close();
                }

                if (inputReader != null) {
                    inputReader.close();
                }

                if (reader != null) {
                    reader.close();
                }

            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        return value.toString();
    }

//	public static String sendPostJsonZippedRequest(String urlString, String json) {
//		try {
//			URL url = new URL(urlString);
//			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//			conn.setReadTimeout(15000);
//			conn.setConnectTimeout(3000);
//			conn.setRequestMethod("POST");
//			conn.setUseCaches(false);
//			conn.setDoInput(true);
//			conn.setDoOutput(true);
//
//			conn.setRequestMethod("POST");
//			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
//			conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
//
//			byte[] fooGzippedBytes = zipJson(json);
//
//			conn.setRequestProperty("Connection", "Keep-Alive");
//			conn.addRequestProperty("Content-length", fooGzippedBytes.length + "");
//			// conn.addRequestProperty("json-data",
//			// reqEntity.getContentType().getValue());
//
//			conn.connect();
//			OutputStream os = conn.getOutputStream();
//			os.write(fooGzippedBytes);
//			os.close();
//
//			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//				return readStream(conn.getInputStream());
//			}
//
//		} catch (Exception e) {
//			System.out.println("multipart post error " + e + "(" + urlString + ")");
//		}
//		return null;
//	}
    private static byte[] zipJson(String json) throws IOException, UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(baos);
        gzos.write(json.getBytes("UTF-8"));
        byte[] fooGzippedBytes = baos.toByteArray();
        return fooGzippedBytes;
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public static HttpsURLConnection setUpHttpsConnection(String urlString) {
        HttpsURLConnection urlConnection = null;
        try {

            if (context == null) {
                context = getSSLContext();
            }

            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url = new URL(urlString);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());

        } catch (Exception ex) {
            System.out.println("Failed to establish SSL connection to server: " + ex.toString());
            return null;
        }
        return urlConnection;
    }

    private static SSLContext getSSLContext() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException,
            KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        FileInputStream caInput = new FileInputStream("social_group_com.crt");
//		InputStream caInput = new BufferedInputStream(Constants.assetManager.open("star_zoyride_com_bundled.crt"));
        X509Certificate ca;
        String alias = null;
        try {
            ca = (X509Certificate) cf.generateCertificate(caInput);
            alias = ca.getSubjectX500Principal().getName();
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        keyStore.setCertificateEntry(alias, ca);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
        kmf.init(keyStore, null /*clientCertPassword.toCharArray()*/);

        // Create a TrustManager that trusts the CAs in our KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return context;
    }

    public static String sendCompressedPostFile(String urlServer, String json) {

        if (json == null) {
            System.out.println("JSON was null." + new NullPointerException("JSON was null."));
            return null;
        }

        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String response = null;

        try {
            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs &amp; Outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Set HTTP method to POST.
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            connection.setConnectTimeout(6000);
            connection.setReadTimeout(300000);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + "json-data" + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

//		 
            outputStream.write(compress(json));

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                response = readStream(connection.getInputStream());
            }
            System.out.println(connection.getResponseMessage());

            ///fileInputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            //Exception handling
            ex.printStackTrace();
        }
        return response;
    }

    public static byte[] compress(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(text.getBytes("UTF-8"));
            out.close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return baos.toByteArray();
    }

    private static String decompress(byte[] bytes) {
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return new String(baos.toByteArray(), "UTF-8");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
