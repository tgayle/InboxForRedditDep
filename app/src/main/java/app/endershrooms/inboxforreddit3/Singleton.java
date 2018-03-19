package app.endershrooms.inboxforreddit3;

import okhttp3.OkHttpClient;

/**
 * Created by Travis on 1/22/2018.
 */

public class Singleton {

    static Singleton thisSingleton;

    public static Singleton getInstance() {
        if (thisSingleton == null) thisSingleton = new Singleton();
        return thisSingleton;
    }

    private OkHttpClient client = new OkHttpClient();

    public OkHttpClient client() {
        return client;
    }
}
