package com.antigravity.cryptowallet.ui.browser

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import org.json.JSONObject

class Web3Bridge(
    private val webView: WebView,
    private val address: String,
    private val chainIdProvider: () -> Long,
    private val onActionRequest: (Web3Request) -> Unit
) {
    private val gson = Gson()

    data class Web3Request(
        val id: Int,
        val method: String,
        val params: String
    )

    fun getInjectionJs(): String {
        val chainId = chainIdProvider()
        return """
            (function() {
                const address = '$address';
                const chainId = '0x${chainId.toString(16)}';
                
                window.ethereum = {
                    isAntigravity: true,
                    isMetaMask: true,
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
                
                window.dispatchEvent(new Event('ethereum#initialized'));
            })();
        """.trimIndent()
    }

    @JavascriptInterface
    fun postMessage(json: String) {
        val obj = JSONObject(json)
        val method = obj.getString("method")
        val id = obj.getInt("id")
        val params = obj.optString("params", "[]")
        
        webView.post {
            when (method) {
                "wallet_switchEthereumChain" -> {
                     onActionRequest(Web3Request(id, method, params))
                }
                "eth_requestAccounts", "eth_accounts" -> {
                    sendResponse(id, "[\"$address\"]")
                }
                "eth_chainId" -> {
                    sendResponse(id, "\"0x${chainIdProvider().toString(16)}\"")
                }
                "net_version" -> {
                    sendResponse(id, "\"${chainIdProvider()}\"")
                }
                "eth_sendTransaction", "personal_sign", "eth_sign", "eth_signTypedData_v4" -> {
                    onActionRequest(Web3Request(id, method, params))
                }
                else -> {
                    // Try to respond null/error for unknowns to avoid hang
                    sendError(id, "Method $method not supported")
                }
            }
        }
    }

    fun sendResponse(id: Int, resultJson: String) {
        val js = "javascript:window.onRpcResponse($id, $resultJson, null)"
        webView.evaluateJavascript(js, null)
    }

    fun sendError(id: Int, message: String) {
        val errorJson = "{\"message\": \"$message\", \"code\": 4001}"
        val js = "javascript:window.onRpcResponse($id, null, $errorJson)"
        webView.evaluateJavascript(js, null)
    }
}
