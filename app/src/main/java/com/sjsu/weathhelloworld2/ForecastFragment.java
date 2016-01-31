package com.sjsu.weathhelloworld2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.sjsu.weathhelloworld2.data.WeatherContract;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    final static String tag = ForecastFragment.class.getSimpleName();
    ForecastAdapter madapter;
    private static final int FORECAST_LOADER = 0;

    public ForecastFragment() {

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

        String locationSetting = Utility.getPreferredLocation(getActivity());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        final Cursor cursor = getActivity().getContentResolver().query(weatherForLocationUri, null, null, null, sortOrder);
        madapter = new ForecastAdapter(getActivity(), cursor, 0);
        listView.setAdapter(madapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor selectedCursor = (Cursor) parent.getItemAtPosition(position);
                if (selectedCursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getContext());
                    Intent intent = new Intent(getContext(), DetailActivity.class)
                            .setData(
                                    WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                            locationSetting,
                                            selectedCursor.getLong(COL_WEATHER_DATE)
                                    )
                            );
                    startActivity(intent);
                }
            }
        });
        cursor.close();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
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
        new FetchWeatherTask(getContext()).execute(
                PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.pref_location_key), "")
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                new FetchWeatherTask(getContext()).execute(
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

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String locationSetting = Utility.getPreferredLocation(getContext());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        return new CursorLoader(getContext(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        madapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        madapter.swapCursor(null);
    }

    public void onLocationChanged() {
        String locationSetting = Utility.getPreferredLocation(getContext());
        new FetchWeatherTask(getContext()).execute(locationSetting);
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }
}
