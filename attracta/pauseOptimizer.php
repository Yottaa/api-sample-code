<?php

//$base_url = 'http://localhost:5000';
$base_url = 'http://api-dev-new.yottaa.com';
$api_key = $_GET['api_key'];
$partner_id = $_GET['partner_id'];
$account_id = $_GET['account_id'];
$site_id = $_GET['site_id'];

$header = array("YOTTAA-API-KEY: $api_key");

// Build request URL:
$fetch_url = $GLOBALS['base_url'] . "/partners/$partner_id/sites/$site_id/optimizer/pause";

// Initialize cURL instance with request URL:
$ch = curl_init($fetch_url);

// Set options (HTTP POST)
curl_setopt($ch, CURLOPT_POST, TRUE);
curl_setopt($ch, CURLOPT_HTTPHEADER, $header);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);

// Fetch JSON response from Yottaa Partner API
$response = curl_exec($ch);
//(no response?)
echo $response;

// Close connection
curl_close($ch);

?>
