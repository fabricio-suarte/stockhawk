package com.udacity.stockhawk.ui;


import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A Graph {@link Fragment}.
 */
public class GraphFragment extends Fragment
                           implements LoaderManager.LoaderCallbacks<Cursor>{

    //region constants

    private static final String LOG = GraphFragment.class.getCanonicalName();

    private static final int STOCK_DETAIL_LOADER = 1;
    private static final String CURRENT_SYMBOL = "currentSymbol";

    //endregion

    //region attributes

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.chart)
    LineChart chart;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.textViewDetailSymbol)
    TextView symbolTextView;


    private String currentStockSymbol;
    private GraphAdapter adapter;

    //endregion

    //region constructor

    public GraphFragment() {
        // Required empty public constructor
    }

    //endregion

    //region fragment overrides


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            this.currentStockSymbol = savedInstanceState.getString(CURRENT_SYMBOL);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if(outState != null) {
            outState.putString(CURRENT_SYMBOL, this.currentStockSymbol);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_graph, container, false);

        ButterKnife.bind(this, root);

        this.adapter = new GraphAdapter(this.getActivity(), this.chart);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        //Let's set the loader
        this.getLoaderManager().initLoader(STOCK_DETAIL_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    //endregion

    //region LoaderManager.LoaderCallbacks<Cursor>

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this.getActivity(),
                Contract.Quote.URI,
                new String[] { Contract.Quote.COLUMN_HISTORY},
                String.format("%s = ?", Contract.Quote.COLUMN_SYMBOL),
                new String[] { this.currentStockSymbol == null ? "" : this.currentStockSymbol },
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.moveToFirst()) {
            String json = data.getString(0);

            try {
                JSONArray jsonData = new JSONArray(json);
                this.adapter.setData(jsonData);

                this.symbolTextView.setText(this.currentStockSymbol);
            }
            catch (JSONException ex) {
                Log.e(LOG, "Error while trying to parse json!", ex);
            }
        }
        else {
            this.adapter.setData(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.adapter.setData(null);
    }

    //endregion

    //region public methods

    public static GraphFragment create(String stockSymbol) {

        GraphFragment obj = new GraphFragment();
        obj.currentStockSymbol = stockSymbol;

        return obj;
    }

    //endregion



}
