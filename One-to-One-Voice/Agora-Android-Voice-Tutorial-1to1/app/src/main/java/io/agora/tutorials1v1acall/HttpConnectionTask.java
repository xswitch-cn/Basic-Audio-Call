package io.agora.tutorials1v1acall;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpConnectionTask {
    private static final String TAG = "HttpConnectionTask";

    ExecutorService executor = Executors.newSingleThreadExecutor();

    final Handler handler = new Handler(Looper.getMainLooper());

    private String jsonBody;

    private String authorization;

    TaskCallback callback;

    private String mUrl;

    private String mCallData;

    public HttpConnectionTask(String jsonBody, TaskCallback callback) {
        this.jsonBody = jsonBody;
        this.callback = callback;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public void setCallData(String callData) {
        mCallData = callData;
    }

    public void execute(String url) {
        mUrl = url;
        final String[] result = new String[1];
        executor.execute(new Runnable() {
            @Override
            public void run() {
                result[0] = doInBackground(url);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute(result[0]);
                    }
                });
            }
        });
    }

    private String doInBackground(String url) {
        String response = "";
        try {
            URL lUrl;
            if (CommonConstant.CALL_HANG_UP_URL.equals(url)) {
                lUrl = new URL(url + mCallData);
            } else {
                lUrl = new URL(url);
            }
            HttpURLConnection conn = (HttpURLConnection) lUrl.openConnection();

            // 配置请求
            if (CommonConstant.CALL_HANG_UP_URL.equals(url)) {
                conn.setRequestMethod("DELETE");
            } else {
                conn.setRequestMethod("POST");
            }

            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            if (!TextUtils.isEmpty(this.authorization)) {
                conn.setRequestProperty("Authorization", "Bearer " + this.authorization);
            }
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true); // 允许写入请求体

            // 发送 JSON 数据
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = conn.getResponseCode();
            //callback.onRequestReturn(responseCode, conn.getResponseMessage());
            Log.i(TAG, "responseCode: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                response = result.toString();
            } else {
                response = "Error Code: " + responseCode;
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            response = "Error: " + e.getMessage();
        }
        return response;
    }

    private void onPostExecute(String result) {
        // 在主线程处理结果
        Log.i(TAG, "Response: " + result);
        callback.onRequestReturn(mUrl, result);
    }

    public interface TaskCallback {
        void onRequestReturn(String url, String result);
    }
}