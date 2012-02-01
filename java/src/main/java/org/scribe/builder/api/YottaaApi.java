package org.scribe.builder.api;

import org.scribe.model.*;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;

import org.scribe.utils.*;


public class YottaaApi extends DefaultApi20
{
  private static final String AUTHORIZE_URL = "https://api-dev.yottaa.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s";
  private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s";

  @Override
  public Verb getAccessTokenVerb()
  {
    return Verb.POST;
  }
  
  @Override
  public String getAccessTokenEndpoint()
  {
    return "https://api-dev.yottaa.com/oauth/access_token";
  }

//  @Override
//  public OAuthService createService(OAuthConfig config)
//  {
//    return new YottaaOAuth20ServiceImpl(this, config);
//  }
  
  @Override
  public String getAuthorizationUrl(OAuthConfig config)
  {
    Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback. Yottaa does not support OOB");

    // Append scope if present
    if(config.hasScope())
    {
     return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope()));
    }
    else
    {
      return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
    }
  }
}
//
//class YottaaOAuth20ServiceImpl extends OAuth20ServiceImpl
//{
//	public YottaaOAuth20ServiceImpl(DefaultApi20 api, OAuthConfig config) {
//		super(api, config);
//		// TODO Auto-generated constructor stub
//	}
//	
//	@Override
//	public Token getAccessToken(Token requestToken, Verifier verifier)
//	  {
//	    OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
//	    
//	    request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
//	    request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
//	    request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
//	    request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
//	    request.addBodyParameter("grant_type", "authorization_code");    
//	    if(config.hasScope()) request.addBodyParameter(OAuthConstants.SCOPE, config.getScope());
//	    
//	    Response response = request.send();
//	    return api.getAccessTokenExtractor().extract(response.getBody());
//	  }
//
//}
