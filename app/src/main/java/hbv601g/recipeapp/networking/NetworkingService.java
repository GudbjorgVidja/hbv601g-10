package hbv601g.recipeapp.networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

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
    public JsonElement[] getRequest(String reqURL) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(baseURL+reqURL)
                .build();

        final JsonElement[] element = new JsonElement[1];
        Call call = client.newCall(request);

        // enqueue = async
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response)
                    throws IOException {
                String ret = response.body().string();
                Log.d("API", "code:" + response.code() + " og body:" + ret);

                jsonElement = JsonParser.parseString(ret);
                element[0] = JsonParser.parseString(ret);
                Log.d("API", "JsonArray:" +  element[0].isJsonArray() + " JsonObject:"+element[0].isJsonObject());
            }

            public void onFailure(Call call, IOException e) {
                Log.d("API", "onFailure");
                //fail();
            }
        });


        Log.d("API", "method return");
        return element;
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
