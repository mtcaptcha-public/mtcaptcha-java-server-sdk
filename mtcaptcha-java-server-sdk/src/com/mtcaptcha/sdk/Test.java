/*
 *  Apache 2.0 License
 *  MTCaptcha
 */
package com.mtcaptcha.sdk;

/**
 *
 * @author batou@mtcaptcha.com
 */
public class Test
{
	
	public static void main(String[] args)
	{
		String privatekey		= "<replace this with your privatekey>";
		String token1			= "<replace this some verified token>";
		String token2			= "<replace this verified token>";		
		
		MTCaptchaUtil mtcaptcha		= new MTCaptchaUtil(privatekey);
		
		MTCaptchaTokenInfo info1	= mtcaptcha.getTokenInfo(token1);
		MTCaptchaTokenInfo info2	= mtcaptcha.getTokenInfo(token2);
		
		boolean token1IsSuccess		= info1.success;
		boolean token2IsSuccess		= info2.success;
		
		System.out.println(info1);
		System.out.println(info2);	
		
		mtcaptcha.close();
	}
	
}
