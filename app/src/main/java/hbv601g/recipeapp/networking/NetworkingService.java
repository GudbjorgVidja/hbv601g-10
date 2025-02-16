package hbv601g.recipeapp.networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.*;

public class NetworkingService extends Service {

    private final String baseURL;
    private JsonElement jsonElement;
    private int code;
    public NetworkingService() {
        baseURL = "https://hbv501g-26.onrender.com/";
    }

    // Required, dunno why
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // cite your sources:
    // https://www.baeldung.com/guide-to-okhttp
    public JsonElement getRequest(String reqURL) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(baseURL+reqURL)
                .build();

        // Wait for response
        CountDownLatch latch = new CountDownLatch(1);

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response)
                    throws IOException {

                String ret = response.body().string();
                code = response.code();

                jsonElement = JsonParser.parseString(ret);
                latch.countDown();
            }


            public void onFailure(Call call, IOException e) {
                Log.d("API", "onFailure");
                call.cancel(); // ?
                latch.countDown(); // gott eða nah?
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.d("API", "Latch catch");
            throw new RuntimeException(e);
        }


        // See about implementation
        if(code != 200) return null;

        return jsonElement;
    }

    public JsonElement postRequest(String reqURL, String data){
        return null;
    }
    public JsonElement patchRequest(String reqURL, String data){
        return null;
    }
    public JsonElement deleteRequest(String reqURL){
        return null;
    }
}
