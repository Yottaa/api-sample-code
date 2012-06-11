<?php

$base_url = $_GET['api_environment'];
$api_key = $_GET['api_key'];
$partner_id = $_GET['partner_id'];
$account_id = $_GET['account_id'];
$site_id = $_GET['site_id'];

$header = array("YOTTAA-API-KEY: $api_key");

// Build array of HTTP POST variables:
$post_vars = array(
    "account_id" => $account_id,
);

// Build request URL:
$fetch_url = $base_url . "/partners/$partner_id/sites/$site_id/optimizer/pause";

// Initialize cURL instance with request URL:
$ch = curl_init($fetch_url);

// Set options (HTTP PUT)
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "PUT");
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
        

curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($post_vars));
curl_setopt($ch, CURLOPT_HTTPHEADER, $header);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);


// Fetch JSON response from Yottaa Partner API
$response = curl_exec($ch);
//(no response?)
echo $response;

// Close connection
curl_close($ch);

?>
