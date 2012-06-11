<?php

$base_url = $_GET['api_environment'];
$api_key = $_GET['api_key'];
$partner_id = $_GET['partner_id'];
$account_id = $_GET['account_id'];
$host = $_GET['host'];

$header = array("YOTTAA-API-KEY: $api_key");

// Build array of HTTP POST variables:
$post_vars = array(
    "host" => $host,
);

// Build request URL:
$fetch_url = $GLOBALS['base_url'] . "/partners/$partner_id/accounts/$account_id/sites";

// Initialize cURL instance with request URL:
$ch = curl_init($fetch_url);

// Set options (HTTP POST)
curl_setopt($ch, CURLOPT_POST, TRUE);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
curl_setopt($ch, CURLOPT_POSTFIELDS, $post_vars);
curl_setopt($ch, CURLOPT_HTTPHEADER, $header);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);

// Fetch JSON response from Yottaa Partner API
$response = curl_exec($ch);
//(response has site_id and host)
echo $response;

// Close connection
curl_close($ch);

?>
