package com.example.dm.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;

public class SendDataToServer implements Runnable {

    private RequestQueue queue;
    private StringBuilder stringBuilderURI;
    private String strPacket;
    private Packet packet;
    Context context;

    public SendDataToServer(String strPacket, RequestQueue queue, StringBuilder stringBuilderURI, Packet packet, Context context) {
        this.strPacket = strPacket;
        this.queue = queue;
        this.stringBuilderURI = stringBuilderURI;
        this.packet = packet;
        this.context = context;
    }

    @Override
    public void run() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, stringBuilderURI.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equalsIgnoreCase("200")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            packet.setCommitStatus("1");
                            PacketRoomDatabase.getInstance(context)
                                    .packetDAO()
                                    .updatePacket(packet);
                        }
                    }).start();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return strPacket == null ? null : strPacket.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    Log.i(Constants.TAG_LOG, "Error :: " + e);
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        queue.add(stringRequest);
    }
}