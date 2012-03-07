<?php
session_start();
// OAuth2 PHP Sample Client
// Choose environment (ENV):
// (options: development, staging, production)
$ENV = 'development';

define('APP_PATH', dirname(__FILE__));
require_once "spyc.php";
require('Client.php');

// Load YAML file:
$all_config = Spyc::YAMLLoad($APP_PATH . 'config/oauth_client_sample.yml');
$ClientSampleConfig = $all_config[$ENV];

// Load settings from YAML file:
$client_id = $ClientSampleConfig['client_id'];
$client_secret = $ClientSampleConfig['client_secret'];

$yottaa_api_url = $ClientSampleConfig['app_url'];

$client_url = $ClientSampleConfig['client_url'];
$server_url = $ClientSampleConfig['server_url'];
$redirect_uri = $client_url . '/oauth_client_sample.php';

$authorize_url = $server_url . '/oauth/authorize';
$token_url = $server_url . '/oauth_client_sample/get_token';

// The client instance - see "Client.php"
$client = new OAuth2\Client($client_id, $client_secret);

if (!isset($_GET['code']) && !isset($_SESSION['access_token']))
{
	// On first load, get authentication from Yottaa.
	$auth_url = $client->getAuthenticationUrl($authorize_url, $redirect_uri);
	header('Location: ' . $auth_url);
	die('Redirect');
}
else
{
	// If we don't have a token set as a session variable, then the page load
	// comes after requesting the authorization grant. Fetch the access token
	// in this scenario.
	if (!isset($_SESSION['access_token']))
	{
		// Once we've received the authentication grant, fetch the access token.
		$authorization_code = $_GET['code'];

		$params = array('code' => $authorization_code, 'redirect_uri' => $redirect_uri);
		$response = $client->getAccessToken($server_url . '/oauth/access_token', 'authorization_code', $params);
		$access_token = $response['result']['access_token'];
		$refresh_token = $response['result']['refresh_token'];

		// Store the access token and refresh token as a session variable.
		$_SESSION['access_token'] = $access_token;
		$_SESSION['refresh_token'] = $refresh_token;
	}
	// Otherwise, if the token is already stored as a session variable, we can
	// go directly to requesting any protected resource.
	else
	{
		$access_token = $_SESSION['access_token'];
		$refresh_token = $_SESSION['refresh_token'];
	}
	// Set the access token - it will be used when fetching the protected resources.
	$client->setAccessToken($access_token);

	// Build headers for the cURL request (access token required for authorization).
	$http_headers = array('Authorization' => "OAuth $access_token");
	
	// This client example fetches the user's email.

	$URL_email = $server_url . '/users/email';
	$URL_sites = $server_url . '/sites';
	$URL_benchmarks = $benchmarks . '/benchmarks';
	$URL_1 = $server_url . '/users/email';
	$URL_2 = $server_url . '/users/email';

	// Fetch the user's email and site list.
	$response = $client->fetch($URL_email, array(), 'GET', $http_headers);
		
	// If the request failed, restart the authorization process:
	// (Otherwise, continue with resource fetching.)
	echo var_dump($response);

	if ($response['code'] == 401)
	{
		// Reauthorize using refresh token if the access token has been expired
		$params = array('refresh_token' => $_SESSION['refresh_token'], 'redirect_uri' => $redirect_uri);
		$response = $client->getAccessToken($server_url . '/oauth/access_token', 'refresh_token', $params);

		// After reauthorization is complete, refetch protected resource (email)
		$response = $client->fetch($URL_email, array(), 'GET', $http_headers);

		echo "<h2>Access token has expired, used refresh token.</h2>";
	}
	else
	{
		echo "<h2>Valid access token, no need to use refresh token.</h2>";
	}
	$user_email = $response['result'];
	
	$response = $client->fetch($URL_sites, array(), 'GET', $http_headers);
	$user_sites = $response['result']['sites'];

	if (isset($authorization_code))
	{
		echo "<b>Authorization Code:</b> $authorization_code <br /><br />";
	}
	else
	{
		echo "<b>Authorization Code:</b> <i>(using token stored in session)</i><br /><br />";
	}
	echo "<b>Access Token:</b> $access_token <br /><br />";
	echo "<b>Refresh Token:</b> $refresh_token <br /><br />";
	echo "<b>User email:</b> $user_email <br /><br />";
	echo "<b>User sites:</b> <ul>";
	
	foreach ($user_sites as $site_info)
	{
		echo "<li>";
		$id = $site_info['id'];
		$portal_url = $site_info['portal_url'];
		$host = $site_info['host'];
		$api_url = $site_info['api_url'];
		echo "<b><i>$host</i></b>";
		echo "<ul>";
		echo "<li><b>Site ID:</b> $id </li>";
		echo "<li><b><a href=$portal_url >Go to Portal</a></b> ($portal_url) </li>";
		echo "<li><b><a href=$api_url >Go to API</a></b> ($api_url) </li>";
		
		// Now, pull the performance data for the site.
		$URL_perf = "$server_url/sites/$id/key_pages/monitoring";
		$response = $client->fetch($URL_perf, array(), 'GET', $http_headers);
		$perf_data = $response['result']['values'];
	
		$yottaa_score = $perf_data['yottaa_score'];
		$asset_count = $perf_data['asset_count'];
		$asset_size = $perf_data['asset_size'];
		$time_to_display = $perf_data['time_to_display'];
		$time_to_title = $perf_data['time_to_title'];
		$first_paint = $perf_data['first_paint'];
		$time_to_interact = $perf_data['time_to_interact'];
		
		echo "<li><b>Performance Benchmarks:</b>";
		echo "<ul>";
		echo "<li><b>Yottaa Score:</b> $yottaa_score</li>";
		echo "<li><b>Asset Count:</b> $asset_count</li>";
		echo "<li><b>Asset Size:</b> $asset_size bytes</li>";
		echo "<li><b>Time to Display:</b> $time_to_display ms</li>";
		echo "<li><b>Time to Title:</b> $time_to_title ms</li>";
		echo "<li><b>First Paint:</b> $first_paint ms</li>";
		echo "<li><b>Time to Interact:</b> $time_to_interact ms</li>";
		echo "</ul>";

		echo "</ul>";
		echo "</li>";
		echo "<br /><br />";

	}
	echo "</ul>";
}

?>
