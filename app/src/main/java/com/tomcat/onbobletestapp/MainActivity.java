package com.tomcat.onbobletestapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity
{
    ListView    mListView;
    TextView    mTVDevInfo;


    //ListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uiProcess();
        controlProcess();
    }

    protected void onResume()
    {
        super.onResume();
    }

    private void uiProcess()
    {
        mListView = (ListView)findViewById(R.id.listView);
        mTVDevInfo = (TextView)findViewById(R.id.Items);


    }

    private void controlProcess()
    {
        String[] name = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
        String[] address = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j" };
        String[] rssi = new String[]{"10", "32", "45", "66", "21", "5", "66", "42", "76", "80" };
        String[] tempInfo = new String[name.length];

        for (int i=0; i<name.length; i++)
        {
            tempInfo[i] = "Device:" + name [i] + "\t\t" + "Addr:" + address[i] +"\t\t" + "rssi:" + rssi[i];
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tempInfo);
        mListView.setAdapter(adapter);

        mTVDevInfo.setText(Integer.toString(adapter.getCount()));
    }

    /*
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if((requestCode == RESULT_OK) && (resultCode == Activity.RESULT_CANCELED))
        {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
    */
}
