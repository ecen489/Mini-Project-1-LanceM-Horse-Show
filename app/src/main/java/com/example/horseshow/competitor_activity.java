package com.example.horseshow;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class competitor_activity extends AppCompatActivity {


    String[] riders_plus_horse_list;
    private ListView lvItems;
    private List<Product> lstProducts;
    int intCount = 0;

    //counting length of rows in text file
    InputStream inputStreamCounter;
    BufferedReader bufferedReaderCounter;

    //loading the vlaues in to text file string array
    InputStream inputStreamLoader;
    BufferedReader bufferedReaderLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competitor_activity);

        //get Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //capture layout TextVeiw and set the string as its text
        TextView compititon_name = findViewById(R.id.compeitionName2);
        compititon_name.setText(message);

        /* Code above is to connect to previous screen, code below is for the list view */

        //listview object linked to xml
        lvItems = (ListView) findViewById(R.id.lvItems);
        lstProducts = new ArrayList<>();

        lvItems.setAdapter(new CountdownAdapter(competitor_activity.this, lstProducts));


        //connect Inputstream andd buffer into text file and each other, counters
        inputStreamCounter = this.getResources().openRawResource(R.raw.people_competiting);
        bufferedReaderCounter = new BufferedReader(new InputStreamReader(inputStreamCounter));

        //loaders
        inputStreamLoader = this.getResources().openRawResource(R.raw.people_competiting);
        bufferedReaderLoader = new BufferedReader(new InputStreamReader(inputStreamLoader));

        //count number of rows/lines in text file
        try {
            while (bufferedReaderCounter.readLine() != null) {
                intCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        riders_plus_horse_list = new String[intCount];

        //load rows or lines into string array
        try {
            int j = 255000; // about 4.5 minutes
            for (int i = 0; i < intCount; i++) {
                riders_plus_horse_list[i] = bufferedReaderLoader.readLine();
                lstProducts.add(new Product(riders_plus_horse_list[i], System.currentTimeMillis() + j));
                j = j+255000;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Define adapter
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, riders_plus_horse_list);

        //assign adaper object to listview object
        //lvItems.setAdapter(adapter);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), riders_plus_horse_list[position] + "was clicked", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private class Product {
    String name;
    long expirationTime;

    public Product(String name, long expirationTime) {
        this.name = name;
        this.expirationTime = expirationTime;
    }
}


public class CountdownAdapter extends ArrayAdapter<Product> {

    private LayoutInflater lf;
    private List<ViewHolder> lstHolders;
    private Handler mHandler = new Handler();
    private Runnable updateRemainingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (lstHolders) {
                long currentTime = System.currentTimeMillis();
                for (ViewHolder holder : lstHolders) {
                    holder.updateTimeRemaining(currentTime);
                }
            }
        }
    };

    public CountdownAdapter(Context context, List<Product> objects) {
        super(context, 0, objects);
        lf = LayoutInflater.from(context);
        lstHolders = new ArrayList<>();
        startUpdateTimer();
    }

    private void startUpdateTimer() {
        Timer tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(updateRemainingTimeRunnable);
            }
        }, 1000, 1000);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = lf.inflate(R.layout.list_item, parent, false);
            holder.tvProduct = (TextView) convertView.findViewById(R.id.tvProduct);
            holder.tvTimeRemaining = (TextView) convertView.findViewById(R.id.tvTimeRemaining);
            convertView.setTag(holder);
            synchronized (lstHolders) {
                lstHolders.add(holder);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.setData(getItem(position));

        return convertView;
    }
}

    private class ViewHolder {
        TextView tvProduct;
        TextView tvTimeRemaining;
        Product mProduct;

        public void setData(Product item) {
            mProduct = item;
            tvProduct.setText(item.name);
            updateTimeRemaining(System.currentTimeMillis());
        }

        public void updateTimeRemaining(long currentTime) {
            long timeDiff = mProduct.expirationTime - currentTime;
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
                //int hours = (int) ((timeDiff / (1000 * 60 * 60)) % 24);
                tvTimeRemaining.setText("You're up in " + minutes + " mins " + seconds + " sec");
            } else {
                tvTimeRemaining.setText("Your UP!!");
            }
        }
    }
}
