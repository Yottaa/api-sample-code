package org.scribe.model;

/**
 * The representation of an OAuth HttpRequest.
 * 
 * Adds OAuth-related functionality to the {@link Request}  
 * 
 */
public class OAuthRequest extends Request
{
//  private static final String OAUTH_PREFIX = "oauth_";
//  private Map<String, String> oauthParameters;

  /**
   * Default constructor.
   * 
   * @param verb Http verb/method
   * @param url resource URL
   */
  public OAuthRequest(Verb verb, String url)
  {
    super(verb, url);
//    this.oauthParameters = new HashMap<String, String>();
  }

  @Override
  public String toString()
  {
    return String.format("@OAuthRequest(%s %s)", getVerb(), getUrl());
  }
}
