package com.antigravity.cryptowallet.ui.browser

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import org.json.JSONObject

class Web3Bridge(
    private val webView: WebView,
    private val address: String,
    private val chainId: Int = 1 // Mainnet default
) {
    private val gson = Gson()

    // JS to be injected
    fun getInjectionJs(): String {
        return """
            (function() {
                const address = '$address';
                const chainId = '0x${chainId.toString(16)}';
                
                window.ethereum = {
                    isAntigravity: true,
                    isMetaMask: true, // Some dApps check this
                    address: address,
                    chainId: chainId,
                    
                    request: async function(payload) {
                        return new Promise((resolve, reject) => {
                            const id = Math.floor(Math.random() * 1000000);
                            window.callbacks[id] = { resolve, reject };
                            window.androidWallet.postMessage(JSON.stringify({
                                method: payload.method,
                                params: payload.params,
                                id: id
                            }));
                        });
                    },
                    
                    enable: async function() {
                        return this.request({ method: 'eth_requestAccounts' });
                    },
                    
                    send: function(method, params) {
                        return this.request({ method, params });
                    },
                    
                    on: function(event, callback) {
                        console.log('Event listener added:', event);
                    }
                };
                
                window.callbacks = {};
                
                window.onRpcResponse = function(id, result, error) {
                    if (window.callbacks[id]) {
                        if (error) window.callbacks[id].reject(error);
                        else window.callbacks[id].resolve(result);
                        delete window.callbacks[id];
                    }
                };
                
                // Dispatch event that provider is ready
                window.dispatchEvent(new Event('ethereum#initialized'));
            })();
        """.trimIndent()
    }

    @JavascriptInterface
    fun postMessage(json: String) {
        val obj = JSONObject(json)
        val method = obj.getString("method")
        val id = obj.getInt("id")
        
        webView.post {
            when (method) {
                "eth_requestAccounts", "eth_accounts" -> {
                    sendResponse(id, "[\"$address\"]")
                }
                "eth_chainId" -> {
                    sendResponse(id, "\"0x${chainId.toString(16)}\"")
                }
                "net_version" -> {
                    sendResponse(id, "\"$chainId\"")
                }
                else -> {
                    // For now, return null for unsupported methods
                    sendResponse(id, "null")
                }
            }
        }
    }

    private fun sendResponse(id: Int, resultJson: String) {
        val js = "javascript:window.onRpcResponse($id, $resultJson, null)"
        webView.evaluateJavascript(js, null)
    }
}
