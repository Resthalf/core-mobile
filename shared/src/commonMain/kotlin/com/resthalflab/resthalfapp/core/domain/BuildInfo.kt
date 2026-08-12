package com.resthalflab.resthalfapp.core.domain

/**
 * True on debuggable/dev builds. Gates verbose HTTP logging so release builds never write request
 * bodies (which include credentials) to the platform log. Sensitive headers are redacted regardless.
 */
expect fun isDebugBuild(): Boolean
