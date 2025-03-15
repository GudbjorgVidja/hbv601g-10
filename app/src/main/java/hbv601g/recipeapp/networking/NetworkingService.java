package hbv601g.recipeapp.networking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.*;

/**
 * A service class which makes requests to the external API.
 * cite your sources:
 * <a href="https://www.baeldung.com/guide-to-okhttp">...</a>
 */
public class NetworkingService extends Service {
    private final String mBaseURL;
    private JsonElement mJsonElement;
    private int mResponseCode;

    public NetworkingService() {
        mBaseURL = "https://hbv501g-26.onrender.com/";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Makes a get request to the API
     * @param reqURL - the path to use, which is appended to the base url
     * @return - a json element with the result of the call
     * @throws IOException signals that something went wrong with the request
     */
    public JsonElement getRequest(String reqURL) throws IOException {
        Request request = new Request.Builder()
                .url(mBaseURL +reqURL)
                .build();

        return callAPI(request);
    }


    /**
     * Makes a Post Request to the external API
     * @param reqURL a string containing the URL for the API call
     * @param data a string containing the data to add to the request body of the call
     * @return a JsonElement containing the result of the post request
     * @throws IOException signals that something went wrong with the post request
     */
    public JsonElement postRequest(String reqURL, String data) throws IOException{
        RequestBody requestBody = RequestBody.create(data == null ? "" : data,
                MediaType.parse("application/json"));
        Request request = new Request.Builder().url(mBaseURL +reqURL).post(requestBody).build();
        return callAPI(request);
    }


    /**
     * Makes a patch request using the given url and data, calls the API and interprets the result
     * into a JsonElement to return
     * @param reqURL - the url of the request
     * @param data - data to include in the request body
     * @return a JsonElement with the result of the call
     * @throws IOException indicates that something went wrong with the patch request
     */
    public JsonElement patchRequest(String reqURL, String data) throws  IOException{
        RequestBody requestBody = RequestBody.create(data == null ? "" : data,
                MediaType.parse("application/json"));
        Request request = new Request.Builder().url(mBaseURL +reqURL).patch(requestBody).build();
        return callAPI(request);
    }

    /**
     * Makes a delete request using the given url, calls the API and interprets the result into
     * a json element
     * @param reqURL - the url to the endpoint that is being called
     * @return - a json element with the result of the call
     * @throws IOException if the call fails for some reason
     */
    public JsonElement deleteRequest(String reqURL)throws IOException{
        Request request = new Request.Builder().url(mBaseURL +reqURL).delete().build();
        mJsonElement = callAPI(request);

        if(mResponseCode != 200) return null;

        return JsonParser.parseString("true");
    }

    /**
     * Makes a put request to the API and returns the result of the call
     * @param reqURL endpoint url
     * @param data data for the request body
     * @return JsonElement with the result of the call
     * @throws IOException if the calls fails
     */
    public JsonElement putRequest(String reqURL, String data) throws IOException {
        RequestBody requestBody = RequestBody.create(data == null ? "" : data,
                MediaType.parse("application/json"));
        Request request = new Request.Builder().url(mBaseURL + reqURL).put(requestBody).build();
        mJsonElement = callAPI(request);
        Log.d("API", "Response code from put request: " + mResponseCode);
        return mJsonElement;
    }


    /**
     * Method that calls the API, using the given request
     * @param request - the request to be used
     * @return a JsonElement with the response
     */
    private JsonElement callAPI(Request request){
        OkHttpClient client = new OkHttpClient();

        // Wait for response
        CountDownLatch latch = new CountDownLatch(1);

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {

                assert response.body() != null;
                String ret = response.body().string();
                mResponseCode = response.code();

                mJsonElement = JsonParser.parseString(ret);
                latch.countDown();
            }

            public void onFailure(@NonNull Call call, @NonNull IOException e) {
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
        if(mResponseCode != 200 || mJsonElement == null || mJsonElement.isJsonNull()) return null;

        return mJsonElement;
    }
}
