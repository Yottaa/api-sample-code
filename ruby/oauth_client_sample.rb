require 'rubygems'
require 'sinatra'
require 'oauth2'

ClientSampleConfig = YAML.load_file('oauth_client_sample.yml')[ENV['RACK_ENV'] || 'development']

before do
  client_id       = ClientSampleConfig['client_id']
  client_secret   = ClientSampleConfig['client_secret']
  
  @yottaa_api_url = ClientSampleConfig['app_url']
  @redirect_uri   = 'http://localhost:4567/callback'
  @client         = OAuth2::Client.new(client_id, client_secret, :token_url => '/oauth/access_token', :site => @yottaa_api_url)
end

get '/' do
  @authorize_url = @client.auth_code.authorize_url(:redirect_uri => @redirect_uri, :response_type => 'code') # response_type alse can be: token
  erb :'oauth_client_sample/client'
end

get '/oauth_client_sample/callback' do
  session[:code] = params[:code] if params[:code]

  erb :'oauth_client_sample/callback'
end

get '/oauth_client_sample/get_token' do
  # If end user grant access, you will get the access_token, and you need to save the access_token reference with current end user,
  # next time you can directly use this token to access the user's data.
  
  begin
    @token = @client.auth_code.get_token(session[:code], :redirect_uri => @redirect_uri)
    @token.options[:header_format] = "OAuth %s"
  rescue OAuth2::Error => e
    e.message
  else
    session[:access_token] = @token.token # You need to save the code into DB reference with current user.
    erb :'oauth_client_sample/get_token'
  end
end

get '/oauth_client_sample/private_api_test' do
  # Remember to make sure the header must be.
  #   Authorization: OAuth tokennnnn...
  #
  token = OAuth2::AccessToken.new @client, session[:access_token], :header_format => "OAuth %s"
  resp = token.get('/users/email')
  resp.body
end
