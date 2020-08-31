package edu.inha.hellocookieya.api;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestQueueSingleton {
    private static RequestQueueSingleton instance;
    private RequestQueue requestQueue;

    private RequestQueueSingleton(Context context) {
        requestQueue = getRequestQueue(context);
    }

    public static void initialize(Context context) {
        if (instance == null) {
            instance = new RequestQueueSingleton(context);
        }
    }

    public static synchronized RequestQueueSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new RequestQueueSingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public static void close() {
        instance.requestQueue = null;
        instance = null;
    }
}
