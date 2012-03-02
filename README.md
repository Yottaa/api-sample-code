# Introduction

This is Yottaa API(OAuth2 stuff) sample code project. These samples are going to show you how to use Yottaa OAuth2 service and [Yottaa Users API](https://api.yottaa.com/UserApi.html), here we have three different languages, they are:  

* ruby
* php
* java

# Usage

### Ruby

All ruby sample code is under ruby directory, it is based on [sinatra](http://www.sinatrarb.com/intro), and we use [oauth2 gem](https://github.com/intridea/oauth2) as the OAuth2 client. Firstly, you need to configure your OAuth2 settings in ruby/oauth_client_sample.yml file, and you can register an account at [Yottaa Dev Site](https://dev.yottaa.com) for testing.

Then you can start the project use:

	$ ruby oauth_client_sample.rb
	
to start the server, now you can open localhost:4567 in your browser.


### PHP

The PHP OAuth2 Client implementation uses the light PHP wrapper [PHP-OAuth2](https://github.com/adoy/PHP-OAuth2) with some modifications.

As with the other clients, you must configure your OAuth2 settings in the php/config/oauth_client_sample.yml file, and you can register for an account at the [Yottaa Dev Site](https://dev.yottaa.com) for testing.

After the settings have been changed in the YAML file, edit the environment ($ENV) in the php/oauth_client_sample.php file to specify the environment (testing, staging, development, etc.).

Open the php/oauth_client_sample.php file in your web browser (running on localhost or a server with PHP enabled) to see the sample client in action.

### Java

This java program is an example of Oauth 2.0 client of Yottaa API. 
For more information on OAuth 2.0, you can go to [Oauth-v2-10](http://tools.ietf.org/html/draft-ietf-oauth-v2-10).
The program is modified from [Scribe](https://github.com/fernandezpablo85/scribe-java), an OAuth library for Java by Pablo Fernandez.


please take a look at the following files:

	README_YOTTAA.TXT
	src/org/scribe/examples/ClientExample.java
	src/org/scribe/examples/WebApplication.java

ClientExample.java is a native OAuth client application, and WebApplication.java is a web application.


To run the programs, do the following:

(1)download source code from github.com and use Eclipse Java IDE to import the project:

	git clone git://github.com/Yottaa/api-sample-code.git

(2)register an account at dev.yottaa.com and login into dev.yottaa.com and go to YourUserName->"API Key" and then add an App there, let

	App Name=YourAppName
	URL=http://localhost:8080
	Redirect URL=http://localhost:8080/callback

After adding an App you click "Get Client Secret" to get your ClientId and ClientSecret.

(3)modify ClientExample.java to provide correct values of the following parameters(the default Yottaa API server is [api-dev](https://api-dev.yottaa.com)):

	ClientId, ClientSecret, CALLBACK_URL, AUTHORIZE_URL, AccessTokenEndpoint, PROTECTED_RESOURCE_URL1, PROTECTED_RESOURCE_URL2

(4)run ClientExample.java according to the prompting information printed in the Console. The program will ask you to visit an authorization URL in a browser.
After you grant authorization you will be redirected to the CALLBACK_URL with the "code" parameter and its value in the query part of the URL
(though the page can't be visited since this program is a native application and there is no web server running). The value of the "code" parameter 
is the authorization code that you paste to the Console to let the program go on running.

(5)run WebApplication.java with argument "8080 . true", then visit "http://localhost:8080" in a browser and click the hyperlinks step by step.

(6)Note: if you change the end user's password at dev.yottaa.com when you run ClientExample.java or WebApplication.java, the access token will expire
and if at that time you call getResource(accessToken, PROTECTED_RESOURCE_URL) you will get "401 Unauthorized" http response.

