package com.prosodie.recovery.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.binary.Base64;

import com.prosodie.recovery.ws.exception.WSException;


/*   
KEEP-ALIVE  
  
When the application finishes reading the response body or when the application calls close() on the InputStream returned  
by URLConnection.getInputStream(), the JDK's HTTP protocol handler will try to clean up the connection  
and if successful, put the connection into a connection cache for reuse by future HTTP requests. 
  
http.maxConnections=<int> default: 5 
Indicates the maximum number of connections per destination to be kept alive at any given time 
  
  
When the application encounters a HTTP 400 or 500 response,  
it may ignore the IOException and then may issue another HTTP request.  
In this case, the underlying TCP connection won't be Kept-Alive because the response body  
is still there to be consumed, so the socket connection is not cleared, therefore not available for reuse.  
What the application needs to do is call HttpURLConnection.getErrorStream()  
after catching the IOException , read the response body, then close the stream 
  
http://download-llnw.oracle.com/javase/1.5.0/docs/guide/net/http-keepalive.html 
  
*/



public class WS
{

		
	private String user;
	private String password;
	private String endpoint;
	
	private int connectTimeout;
	private int readTimeout;

	public WS(String endpoint)
	{
		this(null, null, endpoint);
	}
	
	public WS(String user, String password, String endpoint)
	{
	
		this.user = user;
		this.password = password;
		this.endpoint = endpoint;
		
		
		if (endpoint.startsWith("https"))
		{
			HttpsURLConnection.getDefaultHostnameVerifier();
			
			HostnameVerifier hv = new HostnameVerifier()
		    {
		        public boolean verify(String urlHostName, SSLSession session)
		        {
		       
		            return true;
		        }
		    };
			
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[]
			{

			new X509TrustManager() 
			{
				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{}
			} };

			// Install the all-trusting trust manager
			try
			{
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			}
			catch (Exception e)
			{}
			
			
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		}
		
		
		/*
		Properties systemProperties = System.getProperties();
		systemProperties.setProperty("https.proxyHost","localhost");
		systemProperties.setProperty("https.proxyPort","9000");
		*/
			

	}

	public int getConnectTimeout()
	{
		return connectTimeout;
	}

	public int getReadTimeout()
	{
		return readTimeout;
	}

	public void setConnectTimeout(int connectTimeout)
	{
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(int readTimeout)
	{
		this.readTimeout = readTimeout;
	}

	public WSResponse send(String payload) 
	throws Exception
	{

		BufferedReader br = null;
		HttpURLConnection connection = null;
		
		try
		{
		
			
			if (payload== null)
			{
			
				throw new WSException();
			}
			
			
			
			URL url = new URL(endpoint);
			

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			
			connection.setConnectTimeout((this.connectTimeout > 0)?this.connectTimeout:5000);
			connection.setReadTimeout((this.readTimeout > 0)?this.readTimeout:10000);
			
		

			
			if ( this.user != null && this.password != null)
			{
				String authString = user + ":" + password;
				byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
				String authStringEnc = new String(authEncBytes);
		
				connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			}
					
			
			if ( payload != null)
			{
				String type = this.content_type(payload);
				
				
				if ( type != null)
				{
					connection.setRequestProperty("Content-Type", type);
					connection.setRequestProperty("Accept", type);
				}
				
				
				OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
				osw.write(payload);
				osw.flush();
				osw.close();
			}


			// 400 | 500 ---> IOException
			int code = connection.getResponseCode();
			String data = null;

			
			if (code == 200)
			{
				
				// UTF-8 | ISO-8859-1
				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				
				StringBuilder sb = new StringBuilder();
				
				String line;
				while((line = br.readLine()) != null)
				{
				    sb.append(line);
				}
				
				
				data = sb.toString();
				
								
			}
			
			
			return new WSResponse(code, data);
		
	

		}
		catch (MalformedURLException e)
		{
			
			throw new WSException();
		}
		finally
		{
			// Calling the close() methods on the InputStream or OutputStream of
			// an HttpURLConnection after a request may free network resources associated with this
			// instance but has no effect on any shared persistent connection.

			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					
				}

			}
			// Calling the disconnect() method of HttpURLConnection may close
			// the underlying socket if a persistent connection is otherwise idle at that time
			
			if (connection != null)
			{
				try
				{
					connection.disconnect();
				}
				catch (Exception e)
				{
					
				}

			}

		}

	
		
	}
	
	/**
	 * Obtiene el tipo de contenido del fichero
	 * 
	 * 
	 */
	private String content_type(String payload)
	{
		if (payload.startsWith("{") && payload.endsWith("}"))
			return "application/json";
		else 
			return null;
	}


}
