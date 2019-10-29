/*
 *  Apache 2.0 License
 *  MTCaptcha
 */
package com.mtcaptcha.sdk;

import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import java.io.Closeable;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 *
 * Java SDK for MTCaptcha, to facilitate Server side validation of captcha tokens.
 * 
 * This object is multi-thread safe and reusable. 
 * This uses connection pooling and threads via OKHttpClient, so
 * only one instance shoud be created per sitekey/privatekey. 
 * 
 * 
 * @see <a href="https://www.mtcaptcha.com">MTCaptcha</a>
 * @see <a href="https://www.mtcaptcha.com/dev-guide-validate-token">MTCaptcha Dev Guide - Check VerifiedToken</a>
 * 
 * @author batou@mtcaptcha.com
 */
public class MTCaptchaUtil implements Closeable
{

	private String			mtcaptchaPrivateKey = null;
	private OkHttpClient	httpclient = null;
	private Gson			gson = null;
	
	private String checkTokenURL1 = "https://service.mtcaptcha.com/mtcv1/api/checktoken.json";
	
	//private String checkTokenURL2 = "https://service2.mtcaptcha.com/mtcv1/api/checktoken.json";	

	public static int HTTP_CONNECT_TIMEOUT_MS 	= 10000;
	public static int HTTP_READ_TIMEOUT_MS		= 3000;
	
	private static final Integer INT200			= new Integer(200);
	
	/**
	 * Constructor.
	 * 
	 * @param privateKey - Private key of the site, required.
	 */
	public MTCaptchaUtil(String privateKey) 
	{
		if (privateKey == null || privateKey.isEmpty()) {
			throw new IllegalArgumentException("Private Key cannot be null.");
		}
		this.mtcaptchaPrivateKey = privateKey;
		httpclient = new OkHttpClient().newBuilder().connectTimeout(HTTP_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS).readTimeout(HTTP_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS).build();
		gson   = new Gson();
	}


	/**
	 * Checks if the MTCaptcha verifiedtoken is valid
	 * 
	 * @param verificationToken The verifiedtoken string 
	 * 
	 * @return true if valid, false if not. 
	 * Note, will also return false on error like bad privatekey, or server server connectivity issues
	 */
	public boolean isTokenValid(String verificationToken) {
		MTCaptchaTokenInfo info = getTokenInfo(verificationToken);
		return info.success;
	}

	/**
	 * Checks if the MTCaptcha verifiedtoken is valid and return the full tokeninfo as an object
	 * 
     * @param verificationToken The verifiedtoken string 
	 * 
	 * @return Always returns a MTCaptchaTokenInfo object.
	 *		MTCaptchaTokenInfo.success will indicate success of check
	 *		MTCaptchaTokenInfo_unexepctedException will hold any error during check such as connectivity issue. 
	 *
	 */
	public MTCaptchaTokenInfo getTokenInfo(String verificationToken) 
	{
		return validateTokenViaHTTP(verificationToken);	
	}


	protected MTCaptchaTokenInfo validateTokenViaHTTP(String verificationToken) {
		
		if (verificationToken == null || verificationToken.isEmpty()) {
			throw new IllegalArgumentException("verificationToken cannot be null or empty");
		}	
			
		Request			httpRequest	= null;
		Response		httpResponse = null;
		ResponseBody	httpRespBody = null;
		
		int				httpResponseCode = 0;
		String			httpResponseStatusMsg = null;
		String			httpResponseString = null;
		Exception		unexpectedErr = null;
		
		
		CALL_API: 
		try {
			
			HttpUrl.Builder urlBuilder = HttpUrl.parse(checkTokenURL1).newBuilder();
			urlBuilder.addEncodedQueryParameter("privatekey", this.mtcaptchaPrivateKey);
			urlBuilder.addEncodedQueryParameter("token", verificationToken);
			
			
			String url		= urlBuilder.build().toString();
				
			httpRequest		= new Request.Builder().url(url).build();
			
			httpResponse	= httpclient.newCall(httpRequest).execute();

			
			httpResponseCode		= httpResponse.code();
			httpResponseStatusMsg	= httpResponse.message();
			httpRespBody			= httpResponse.body();
			
			if (httpRespBody != null) {
				httpResponseString = httpRespBody.string();
			}		
			
		} catch (Exception exception) {
			unexpectedErr = exception;
		} finally {
			closeQuietly(httpRespBody);
			closeQuietly(httpResponse);
		}
		
		MTCaptchaTokenInfo result = null;		
		
		
		DECODEJSON:
		try{
			if(unexpectedErr != null)
				break DECODEJSON;
			if(httpResponseCode != 200)
				break DECODEJSON;
			if(httpRespBody == null)
				throw new Exception("Unexpected, No HTTP body found");
			
			result = gson.fromJson(httpResponseString, MTCaptchaTokenInfo.class);
		
		}catch(Exception exception)
		{
			if(result != null)
				result.success = false;
			
			unexpectedErr = exception;
		}
		
		if(result == null)
			result = new MTCaptchaTokenInfo();

		result._unexepctedException		= unexpectedErr;		
		result._httpResponseCode		= httpResponseCode;
		result._httpResponseSatusMsg	= httpResponseStatusMsg;
		result._httpResponseString		= httpResponseString;
		
		return result;

	}
	
	private void closeQuietly(Closeable c)
	{
		if(c != null)
		{
			try{
				c.close();
			}catch(Exception e)
			{}
		
		}
	}

	/**
	 * Aggressively Closes threads and clears resources in OKHTTPClient. 
	 * This is not needed as the client closes threads on idle, but provided
	 * here for more aggressive cleanup.
	 */
	@Override
	public synchronized void close()
	{
		if(this.httpclient != null)
		{
			this.httpclient.connectionPool().evictAll();
			this.httpclient.dispatcher().executorService().shutdown();
			closeQuietly(this.httpclient.cache());
		}
	}

	
}
