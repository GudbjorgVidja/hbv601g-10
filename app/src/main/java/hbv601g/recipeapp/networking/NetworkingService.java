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

    private String baseURL;
    private JsonElement jsonElement;
    public NetworkingService() {
        baseURL = "http://10.0.2.2:8080/"; // http://localhost:8080/
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

        // Til að bíða eftir response
        CountDownLatch latch = new CountDownLatch(1);

        final JsonElement[] element = new JsonElement[1];
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response)
                    throws IOException {
                String ret = response.body().string();
                Log.d("API", "code:" + response.code() + " og body:" + ret);

                jsonElement = JsonParser.parseString(ret);
                Log.d("API", "JsonArray:" +  jsonElement.isJsonArray() + " JsonObject:"+jsonElement.isJsonObject());
                latch.countDown();
            }

            public void onFailure(Call call, IOException e) {
                Log.d("API", "onFailure");
                call.cancel(); // ?
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.d("API", "Latch catch");
            throw new RuntimeException(e);
        }

        Log.d("API", "jsonElement null: " + jsonElement.isJsonNull() );

        return jsonElement;
    }

    public JsonElement postRequest(String reqURL, String data){
        return null;
    }
    public JsonElement patchRequest(String reqURL, String data){
        return null;
    }
    public JsonElement deleteRequest(String reqURL){ // engin delete m data?
        return null;
    }
}
