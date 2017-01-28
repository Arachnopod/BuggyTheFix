package com.soton.android.buggythefix;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by AndroidHPE on 1/28/2017.
 */

public abstract class HttpsSearch extends AsyncTask<String, Void, Object> {

    @Override
    protected Object doInBackground(String... params) {
        byte[] responseArray;

        StringBuilder s = new StringBuilder();
        for (String param : params){
            s.append(param);
            s.append('+');
        }
        s.deleteCharAt(s.length() - 1);

        String search_url = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" +
                s.toString();

        responseArray = getByteArray(search_url);
        if (responseArray == null) {
            return null;
        }


        String image_url;
        try {
            String json = new String(responseArray);
            image_url = new JSONObject(json).getJSONObject("responseData")
                    .getJSONArray("results").getJSONObject(0).getString("url");
        } catch(JSONException e) {
            return e;
        }


        if (image_url != null ) {
            responseArray = getByteArray(image_url);
            if (responseArray == null) {
                return null;
            }
        }

        return responseArray;
    }

    private byte[] getByteArray(String strUrl) {
        byte[] buff = new byte[1024];
        byte[] result = null;
        HttpsURLConnection response;
        BufferedInputStream inputStream = null;
        ByteArrayOutputStream responseArray = null;
        int length;
        //KeyPinStore keystore = KeyPinStore.getInstance();

        try {
            //The Solution to week four SSl Certificate pinning is starting from this line, please reference to the link :
            // https://developer.android.com/training/articles/security-ssl.html
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // GIAG2.crt should be in the Assets directory
            InputStream caInput = new BufferedInputStream(new FileInputStream("GIAG2.crt"));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

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


            URL url = new URL(strUrl);
            response = (HttpsURLConnection) url.openConnection();
            response.setSSLSocketFactory(context.getSocketFactory());
            response.setRequestMethod("GET");
            response.connect();
            checkResponse(response);

            inputStream = new BufferedInputStream(response.getInputStream());
            responseArray = new ByteArrayOutputStream();

            while ((length = inputStream.read(buff)) != -1) {
                if (length > 0) {
                    responseArray.write(buff, 0, length);
                }
            }
            result = responseArray.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // This is sample, so omit the exception process
                }
            }
            if (responseArray != null) {
                try {
                    responseArray.close();
                } catch (IOException e) {
                    // This is sample, so omit the exception process
                }
            }
        }
        return result;
    }

    private void checkResponse(HttpURLConnection response) throws IOException {
        int statusCode = response.getResponseCode();
        if (HttpURLConnection.HTTP_OK != statusCode) {
            throw new IOException("HttpStatus: " + statusCode);
        }
    }



}
