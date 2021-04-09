package com.example.dm.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class NetworkServiceHelper {

    static boolean status = false;

    public static boolean hasInternetAccess(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

    public static boolean checkServerConnection(Context context, String serverIP) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringBuilder stringBuilderURI = new StringBuilder(serverIP);
//        stringBuilderURI.append(Constants.API_CHECK_CONNECTION);
        stringBuilderURI.append(Constants.TEST_API_CHECK_CONNECTION);
//        Log.i(Constants.TAG_LOG, "URL : " + stringBuilderURI.toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, stringBuilderURI.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.i(Constants.TAG_LOG, "Response from server : " + response.toString());

                status = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(Constants.TAG_LOG, "Error : " + error.toString());
            }
        });
        stringRequest.setTag(Constants.TAG_REQUEST);
        queue.add(stringRequest);

        return status;
    }
}
