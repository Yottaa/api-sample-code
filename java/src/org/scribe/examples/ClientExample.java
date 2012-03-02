package org.scribe.examples;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scribe.exceptions.OAuthException;
import org.scribe.model.*;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

public class ClientExample
{		
	// Replace these with your own ClientId and ClientSecret
	private static final String ClientId = "4f274a0dc66d4806cb000000";
	private static final String ClientSecret = "caf1f05da20c69bdd4bd56419bcfb41065cae5959da641fd4820c978ae2dc000";
	private static final String CALLBACK_URL = "http://localhost:8080/callback"; 
	
	private static final String AUTHORIZE_URL = "https://api-dev.yottaa.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s";	
	private static final String AccessTokenEndpoint = "https://api-dev.yottaa.com/oauth/access_token";

	public static final String PROTECTED_RESOURCE_URL1 = "https://api-dev.yottaa.com/users/email";
	public static final String PROTECTED_RESOURCE_URL2 = "https://api-dev.yottaa.com/sites";

	public static void main(String[] args)
	{
		// Generate the URL to which we will direct users
		String authorizationUrl = getAuthorizationUrl();
		System.out.println("Paste this url in your browser and authorize the application:");
		System.out.println(authorizationUrl);

		// Wait for the authorization code
		System.out.println("Paste the authorization code here");
		System.out.print(">>");
		Scanner in = new Scanner(System.in);
		String auth_code = in.nextLine();
		System.out.println();

		// Exchange the authorization code for an access token and a refresh token
		System.out.println("Exchange the authorization code for an access token and a refresh token:");
		String[] tokens1 = getToken_from_code(auth_code);
		String accessToken = tokens1[0];
		String refreshToken = tokens1[1];
		System.out.println("access_token=" + accessToken);
		System.out.println("refresh_token=" + refreshToken);    
		System.out.println();

		// Now let's go and ask for a protected resource
		System.out.println("Now we're going to access a protected resource...");
		String ss1 = getResource(accessToken, PROTECTED_RESOURCE_URL1);
		System.out.println(ss1);
		System.out.println();

		// If access token expires, use refresh token to get the new access token and refresh token
		System.out.println("If accesss token expires, use refresh token to get the new access token and refresh token:");
		String[] tokens2 = getToken_from_refresh(refreshToken);
		System.out.println("new access_token=" + tokens2[0]);
		System.out.println("new refresh_token=" + tokens2[1]);    
		System.out.println();

		// Now let's visit a protected resource using new access token
		System.out.println("Now we're going to access a protected resource using new access token...");
		String ss2 = getResource(tokens2[0], PROTECTED_RESOURCE_URL2);
		System.out.println(ss2);
		System.out.println();    
	}
	
	public static String getAuthorizationUrl() {
		return String.format(AUTHORIZE_URL, ClientId, OAuthEncoder.encode(CALLBACK_URL));
	}

	public static String[] getToken_from_code(String auth_code)
	{
		OAuthRequest request = new OAuthRequest(Verb.POST, AccessTokenEndpoint);

		request.addBodyParameter(OAuthConstants.CLIENT_ID, ClientId);
		request.addBodyParameter(OAuthConstants.CLIENT_SECRET, ClientSecret);
		request.addBodyParameter(OAuthConstants.CODE, auth_code);
		request.addBodyParameter(OAuthConstants.REDIRECT_URI, CALLBACK_URL);
		request.addBodyParameter("grant_type", "authorization_code");    

		Response response = request.send();
		String body = response.getBody();
		String[] str = new String[2];
		str[0] = extract_Token("access_token", body);	 
		str[1] = extract_Token("refresh_token", body);
		return str;
	}

	public static String[] getToken_from_refresh(String refreshToken)
	{
		OAuthRequest request = new OAuthRequest(Verb.POST, AccessTokenEndpoint);

		request.addBodyParameter(OAuthConstants.CLIENT_ID, ClientId);
		request.addBodyParameter(OAuthConstants.CLIENT_SECRET, ClientSecret);
		request.addBodyParameter("refresh_token", refreshToken);
		request.addBodyParameter(OAuthConstants.REDIRECT_URI, CALLBACK_URL);
		request.addBodyParameter("grant_type", "refresh_token");    

		Response response = request.send();
		String body = response.getBody();
		String[] str = new String[2];
		str[0] = extract_Token("access_token", body);	 
		str[1] = extract_Token("refresh_token", body);
		return str;
	}

	private static String extract_Token(String name, String response)
	{  
		Preconditions.checkEmptyString(name, "name String is incorrect!");	
		Preconditions.checkEmptyString(response, "Response body is incorrect. Can't extract a token from an empty string");

		//{"access_token" : ""}
		String REGEX = "\"%s\"\\s*:\\s*\"([^\"]+)\"";
		REGEX = String.format(REGEX, name);

		Matcher matcher = Pattern.compile(REGEX).matcher(response);
		if (matcher.find())
		{
			String token = OAuthEncoder.decode(matcher.group(1));
			return token;
		} 
		else
		{
			throw new OAuthException("Response body is incorrect. Can't extract a token from this: '" + response + "'", null);
		}

	}

	public static String getResource(String accessToken, String protectedResourceUrl) 
	{
		OAuthRequest request = new OAuthRequest(Verb.GET, protectedResourceUrl);
		request.addHeader("Authorization", "OAUTH "+accessToken);
		Response response = request.send();
		String ss = "status code is \"" + response.getCode() + "\" and resource content is:\n";
		ss += response.getBody();
		return ss;
	}

}
