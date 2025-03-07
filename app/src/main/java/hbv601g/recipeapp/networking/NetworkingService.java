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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.*;

public class NetworkingService extends Service {

    private final String baseURL;
    private JsonElement jsonElement;
    private int responseCode;
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
                responseCode = response.code();

                jsonElement = JsonParser.parseString(ret);
                latch.countDown();
            }

            public void onFailure(Call call, IOException e) {
                Log.d("API", "onFailure");
                call.cancel();
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.d("API", "Latch catch");
            throw new RuntimeException(e);
        }

        // See about implementation
        if(responseCode != 200) return null;

        return jsonElement;
    }


    /**
     * //TODO: Add the data to the formBody (see how that is done)
     * Makes a Post Request to the external API
     * @param reqURL a string containing the URL for the API call
     * @param data a string containing the data to add to the requestbody of the call
     * @return a JsonElement containing the result of the post request
     * @throws IOException signals that something went wrong with the post request
     */
    public JsonElement postRequest(String reqURL, String data) throws IOException{
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder().build();

        Request request = new Request.Builder().url(baseURL+reqURL).post(formBody).build();

        // Wait for response
        CountDownLatch latch = new CountDownLatch(1);

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                String ret = response.body().string();
                responseCode = response.code();
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
        if(responseCode != 200 || jsonElement.isJsonNull()) return null;

        return jsonElement;
    }



    public JsonElement patchRequest(String reqURL, String data){
        return null;
    }
    public JsonElement deleteRequest(String reqURL){
        return null;
    }
}
