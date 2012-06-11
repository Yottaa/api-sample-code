<?php

$base_url = $_GET['api_environment'];
$api_key = $_GET['api_key'];
$partner_id = $_GET['partner_id'];
$email = $_GET['email'];
$site = $_GET['site'];
$plan = $_GET['plan'];
$phone = $_GET['phone'];

$header = array("YOTTAA-API-KEY: $api_key");

// Build array of HTTP POST variables:
$post_vars = array(
    "email" => $email,
    "site" => $site,
    "plan" => $plan,
    "phone" => $phone,
);

// Build request URL:
$fetch_url = $GLOBALS['base_url'] . "/partners/$partner_id/accounts";

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
//(response has email, user_id, site_id, host, yottaa_cname)
echo $response;
// Close connection
curl_close($ch);

?>
