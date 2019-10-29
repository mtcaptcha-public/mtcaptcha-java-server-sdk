/*
 *  Apache 2.0 License
 *  MTCaptcha
 */
package com.mtcaptcha.sdk;


import com.google.gson.Gson;
import java.util.Map;

/**
 *
 * @author batou@mtcaptcha.com
 * 
 * An POJO for the JSON response on CheckToken
 * For full detail of TokenInfo see
 * <a href="https://www.mtcaptcha.com/dev-guide-validate-token">MTCaptcha Dev Guide - Check Verified Token</a>
 * 
 * JSON response is like below:
 * 
 * {
 *   "success": true,
 *   "tokeninfo": {
 *     "v": "1.0",
 *     "code": 301,
 *     "codeDesc": "valid-test:captcha-solved-via-testkey",
 *     "tokID": "6b5a87ab80369d660e3fe2ceb8eb84ac",
 *     "timestampSec": 1572371631,
 *     "timestampISO": "2019-10-29T17:53:51Z",
 *     "hostname": "some.exmaple.com",
 *     "isDevHost": false,
 *     "action": "login",
 *     "ip": "1.1.1.1"
 *   }
 * }
 * 
 */
public class MTCaptchaTokenInfo implements java.io.Serializable
{
	
	protected MTCaptchaTokenInfo(){}

	public boolean				success			= false;	// default FALSE
	public String[]				fail_codes		= null;
	public Map<String, String>	tokeninfo		= null;

	
	public Integer    	_httpResponseCode		= null;
	public String 		_httpResponseSatusMsg	= null;
	public String 		_httpResponseString		= null;  	
	public Exception 	_unexepctedException	= null;

	
	protected static Gson			GSON		= null;
	protected static final Object	GSONLock	= new Object();
	
	private static Gson getGSON()
	{
		if(GSON == null)
		{
			synchronized(GSONLock)
			{
				if(GSON == null)
					GSON = new Gson();
			}
		}
		
		return GSON;
	}
	
	
	public String toString()
	{
		Gson gson = getGSON();
		return gson.toJson(this);
	}
	
}
