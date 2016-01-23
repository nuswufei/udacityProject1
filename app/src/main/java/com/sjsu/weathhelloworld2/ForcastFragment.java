package com.sjsu.weathhelloworld2;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForcastFragment extends Fragment {
    final static String tag = ForcastFragment.class.getSimpleName();
    ArrayAdapter<String> madapter;

    public ForcastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_forcast, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.forcast_list);

        List<String> weekForecast
                = new ArrayList<String>();
        madapter
                = new ArrayAdapter<String>(getContext(),
                R.layout.item_list,
                R.id.forcast_item,
                weekForecast);
        listView.setAdapter(madapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forcast_menu, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        new FetchWeatherTask(getContext(), madapter).execute(
                PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_location_key), "")
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                new FetchWeatherTask(getContext(), madapter).execute(
                        PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_location_key), "")
                );
                return true;
            case R.id.setting:
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.view_action:
                String zipCode = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_location_key), getString(R.string.defaultlocation));
                Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                        .appendQueryParameter("q", zipCode)
                        .build();
                Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                mapIntent.setData(geoLocation);
                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
