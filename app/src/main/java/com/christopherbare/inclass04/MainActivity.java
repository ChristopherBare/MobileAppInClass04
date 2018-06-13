package com.christopherbare.inclass04;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    ProgressBar progressBar;
    Button buttonThread;
    Button buttonAsync;
    Context context;
    ExecutorService threadPool;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        imageView = findViewById(R.id.iv_viewer);
        progressBar = findViewById(R.id.progressbar);
        buttonThread = findViewById(R.id.button_thread);
        buttonAsync = findViewById(R.id.button_async);
        progressBar.setMax(100);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case ThreadDownload.STATUS_START:
                        progressBar.setProgress(0);
                        break;
                    case ThreadDownload.STATUS_PROGRESS:
                        progressBar.setProgress(msg.getData().getInt(ThreadDownload.PROGRESS_KEY));
                        break;
                    case ThreadDownload.STATUS_STOP:
                        progressBar.setProgress(0);
                        byte[] byteArray = msg.getData().getByteArray(ThreadDownload.BITMAP_KEY);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        imageView.setImageBitmap(bitmap);
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });

        buttonThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadPool = Executors.newFixedThreadPool(1);
                threadPool.execute(new ThreadDownload());
            }
        });

        buttonAsync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncDownload().execute();
            }
        });

    }

    class AsyncDownload extends AsyncTask<Integer, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            for(int i = 0; i < 100; i++){
                for(int j = 0; j < 100; j++){

                }
                publishProgress(i);
            }
            Bitmap bitmap = getImageBitmap(getString(R.string.url_async));
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                return;
            }
            progressBar.setProgress(0);
            setImageView(result);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressBar.setProgress(progress[0]);
        }
    }

    class ThreadDownload implements Runnable {
        static final int STATUS_START = 0x00;
        static final int STATUS_PROGRESS = 0x01;
        static final int STATUS_STOP = 0x02;
        static final String PROGRESS_KEY = "PROGRESS";
        static final String BITMAP_KEY = "BITMAP";

        @Override
        public void run() {
            Message startMessage = new Message();
            startMessage.what = STATUS_START;
            handler.sendMessage(startMessage);
            for(int i = 0; i < 10000; i++){
                for(int j = 0; j < 100; j++){

                }
                Message message = new Message();
                message.what = STATUS_PROGRESS;
                Bundle bundle = new Bundle();
                bundle.putInt(PROGRESS_KEY, i);
                message.setData(bundle);
                handler.sendMessage(message);
            }
            Bitmap bitmap = getImageBitmap(getString(R.string.url_thread));

            Message stopMessage = new Message();
            stopMessage.what = STATUS_STOP;
            Bundle data = new Bundle();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            data.putByteArray(BITMAP_KEY, byteArray);
            stopMessage.setData(data);
            handler.sendMessage(stopMessage);
        }
    }
    Bitmap getImageBitmap(String... strings) {
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void setImageView(Bitmap bitmap){
        ImageView imageView = findViewById(R.id.iv_viewer);
        imageView.setImageBitmap(bitmap);
    }
}
