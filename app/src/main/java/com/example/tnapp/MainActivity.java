package com.example.tnapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.tnapp.databinding.ActivityMainBindingImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private CurrentNews currentNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBindingImpl binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);

        TextView gNews = findViewById(R.id.gnews);

        gNews.setMovementMethod(LinkMovementMethod.getInstance());


        String apiKey = "9544faf78f1ab9fbab68a3fab1cc8203";
        String gnewsURL = "https://gnews.io/api/v4/top-headlines?topic=technology&lang=en&token=" + apiKey;

        if (isNetworkAvailable()) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(gnewsURL)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {

                            currentNews = getCurrentDetails(jsonData);

                            CurrentNews displayNews = new CurrentNews(
                                    currentNews.getTitle(),
                                    currentNews.getDescription(),
                                    currentNews.getContent(),
                                    currentNews.getDate(),
                                    currentNews.getImage(),
                                    currentNews.getUrl()
                            );

                            binding.setNews(displayNews);

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Exception caught: ", e);
                    }
                }
            });
        }
        Log.d(TAG, "Main UI code is running!");

    }

    private CurrentNews getCurrentDetails(String jsonData) throws JSONException {
        JSONObject topNews = new JSONObject(jsonData);

        // int totalArticles = topNews.getInt("totalArticles");
        // Log.i(TAG, "From JSON: " + totalArticles);

        JSONArray articlesArray = topNews.getJSONArray("articles");
        JSONObject currently = new JSONObject();

        for (int i = 1; i < articlesArray.length(); i++) {
            currently = articlesArray.getJSONObject(i);
        }

        CurrentNews currentNews = new CurrentNews();

        currentNews.setTitle(currently.getString("title"));
        currentNews.setDescription(currently.getString("description"));
        currentNews.setContent(currently.getString("content"));
        currentNews.setUrl(currently.getString("url"));
        currentNews.setImage(currently.getString("image"));
        currentNews.setDate(currently.getString("publishedAt"));

        return currentNews;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        } else {
            Toast.makeText(this, R.string.network_unavailable_message,
                    Toast.LENGTH_SHORT).show();
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getSupportFragmentManager(), "error_dialog");
    }
}
