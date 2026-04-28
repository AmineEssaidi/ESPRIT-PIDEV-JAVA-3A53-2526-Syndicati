package com.syndicati.services.observability;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * hCaptcha verification service.
 * Verifies tokens against hCaptcha's siteverify endpoint.
 */
public class HCaptchaService {

    private static final String VERIFY_URL = "https://hcaptcha.com/siteverify";
    private static final String LOG_TAG = "[HCaptchaService]";

    private final HCaptchaConfig config;
    private final HttpClient httpClient;

    public HCaptchaService() {
        this(new HCaptchaConfig());
    }

    public HCaptchaService(HCaptchaConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }

    public boolean isEnabled() {
        return config.isEnabled();
    }

    public String getSiteKey() {
        return config.getSiteKey();
    }

    public String buildWidgetHtml() {
        String siteKey = escapeHtml(config.getSiteKey());
        return """
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    html, body {
                        margin: 0;
                        padding: 0;
                        background: transparent;
                        overflow: hidden;
                        font-family: Arial, sans-serif;
                    }
                    .wrap {
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        gap: 10px;
                        padding: 8px 0;
                    }
                    #captcha-target {
                        min-height: 78px;
                        width: 100%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .note {
                        color: rgba(255,255,255,0.70);
                        font-size: 12px;
                        text-align: center;
                    }
                    .status {
                        color: rgba(52, 211, 153, 0.95);
                        font-size: 12px;
                        text-align: center;
                    }
                    .status.error {
                        color: rgba(248, 113, 113, 0.98);
                    }
                </style>
                <script>
                    let captchaRendered = false;

                    function onSolved(token) {
                        var el = document.getElementById('hcaptcha-response');
                        if (el) {
                            el.value = token || '';
                        }
                        var status = document.getElementById('captcha-status');
                        if (status) {
                            status.textContent = token ? 'Security verification complete.' : 'Security verification pending.';
                        }
                    }
                    function onExpired() {
                        var el = document.getElementById('hcaptcha-response');
                        if (el) {
                            el.value = '';
                        }
                        var status = document.getElementById('captcha-status');
                        if (status) {
                            status.textContent = 'Verification expired. Please solve it again.';
                            status.className = 'status error';
                        }
                    }
                    function onHcaptchaScriptLoaded() {
                        renderCaptcha();
                    }
                    function onHcaptchaScriptError() {
                        var status = document.getElementById('captcha-status');
                        if (status) {
                            status.textContent = 'hCaptcha script failed to load. Check network access or refresh.';
                            status.className = 'status error';
                        }
                    }
                    function renderCaptcha() {
                        if (captchaRendered || !window.hcaptcha || !window.hcaptcha.render) {
                            return;
                        }
                        captchaRendered = true;
                        if (window.hcaptcha) {
                            window.hcaptcha.render('captcha-target', {
                                sitekey: '__HCAPTCHA_SITE_KEY__',
                                theme: 'dark',
                                callback: onSolved,
                                'expired-callback': onExpired,
                                'error-callback': onExpired
                            });
                            var status = document.getElementById('captcha-status');
                            if (status) {
                                status.textContent = 'Verification widget loaded. Please solve it before signing in.';
                                status.className = 'status';
                            }
                        }
                    }
                    window.addEventListener('load', function() {
                        window.setTimeout(renderCaptcha, 250);
                        window.setTimeout(renderCaptcha, 1000);
                    });
                </script>
                <script src="https://js.hcaptcha.com/1/api.js?render=explicit" defer onload="onHcaptchaScriptLoaded()" onerror="onHcaptchaScriptError()"></script>
            </head>
            <body>
                <div class="wrap">
                    <div class="note">Complete the security check before signing in</div>
                    <div id="captcha-target"></div>
                    <input type="hidden" id="hcaptcha-response" name="h-captcha-response" value="">
                    <div id="captcha-status" class="status">Waiting for verification...</div>
                </div>
            </body>
            </html>
                """.replace("__HCAPTCHA_SITE_KEY__", siteKey);
    }

    public String readToken(String jsAccessExpression) {
        if (!config.isEnabled() || jsAccessExpression == null || jsAccessExpression.isBlank()) {
            return "";
        }
        try {
            return String.valueOf(jsAccessExpression);
        } catch (Exception e) {
            return "";
        }
    }

    public boolean verifyToken(String token) {
        if (!config.isEnabled()) {
            return true;
        }
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            String body = "secret=" + urlEncode(config.getSecretKey())
                    + "&response=" + urlEncode(token);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VERIFY_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println(LOG_TAG + " Verification HTTP status: " + response.statusCode());
                return false;
            }

            JSONObject json = new JSONObject(response.body());
            return json.optBoolean("success", false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            System.out.println(LOG_TAG + " Verification failed: " + e.getMessage());
            return false;
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
