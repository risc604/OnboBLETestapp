package com.tomcat.onbobletestapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    private TextView    mTextView;
    private ListView    mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.Devices);
        mTextView.setText(" n devices");
        mListView = (ListView) findViewById(R.id.listView);
    }
}
