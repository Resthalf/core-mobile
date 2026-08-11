package com.resthalflab.resthalfapp.feature.listing.domain

/**
 * SANDBOX-ONLY payment simulation config.
 *
 * ⚠️ Computing the Midtrans webhook signature on-device requires the server key, which must NEVER
 * ship in a production app. This exists only to fake a successful payment while real Midtrans Snap
 * isn't wired. When Snap is ready: drop the simulation, load the real Snap redirect URL in the
 * WebView, and let Midtrans call /payment/webhook server-to-server.
 */
object PaymentConfig {
    /** Paste your Midtrans **sandbox** server key here for local testing. */
    const val SANDBOX_SERVER_KEY: String = "test-secret"

    /** Placeholder page shown in the WebView until the real Snap redirect URL is available. */
    const val SANDBOX_PAYMENT_URL: String = "https://simulator.sandbox.midtrans.com/v2/qris/index"
}
