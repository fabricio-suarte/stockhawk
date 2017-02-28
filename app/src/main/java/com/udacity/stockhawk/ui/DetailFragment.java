package com.udacity.stockhawk.ui;


import android.annotation.TargetApi;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Stock details {@link Fragment} implementation.
 */
public class DetailFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>,
               DetailsStockAdapter.DetailsStockAdapterOnClickHandler{

    //region constants

    private static final String LOG = DetailFragment.class.getCanonicalName();

    private static final int STOCK_DETAIL_LOADER = 1;
    private static final String RECYCLER_VIEW_CURRENT_POS = "recyclerViewCurrentPos";
    private static final String CURRENT_SYMBOL = "currentSymbol";

    //endregion

    //region attributes

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recyclerViewDetails)
    RecyclerView detailskRecyclerView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.textViewDetailSymbol)
    TextView symbolTextView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recyclerview_details_empty)
    TextView emptyView;

    private DetailsStockAdapter adapter;
    private int recyclerViewCurrentPos;
    private String currentStockSymbol;

    //endregion

    //region constructor

    public DetailFragment() {
        // Required empty public constructor
    }

    //endregion

    //region fragment overrides


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            this.currentStockSymbol = savedInstanceState.getString(CURRENT_SYMBOL);
            this.recyclerViewCurrentPos = savedInstanceState.getInt(RECYCLER_VIEW_CURRENT_POS);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if(outState != null && this.recyclerViewCurrentPos != RecyclerView.NO_POSITION) {
            outState.putInt(RECYCLER_VIEW_CURRENT_POS, this.recyclerViewCurrentPos);
            outState.putString(CURRENT_SYMBOL, this.currentStockSymbol);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, root);

        this.adapter = new DetailsStockAdapter(this.getActivity(), this, this.emptyView);
        this.detailskRecyclerView.setAdapter(this.adapter);
        this.detailskRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        this.detailskRecyclerView.setHasFixedSize(true);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        //Let's set the loader
        this.getLoaderManager().initLoader(STOCK_DETAIL_LOADER, null, this);

        if(savedInstanceState != null) {

            //It has been set on 'onCreate'
            this.detailskRecyclerView.smoothScrollToPosition(this.recyclerViewCurrentPos);
        }

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

    //region DetailsStockAdapter.DetailsStockAdapterOnClickHandler implementation

    @Override
    public void onClick(int position, View expandImageView, View detailContainer) {

        boolean isExpanded = detailContainer.getVisibility() == View.VISIBLE;

        Drawable d;

        //Shift items...
        if(isExpanded) {

            detailContainer.setVisibility(View.GONE);
            d = this.getActivity().getDrawable(R.drawable.ic_action_expand);
        }
        else {

            detailContainer.setVisibility(View.VISIBLE);
            d = this.getActivity().getDrawable(R.drawable.ic_action_expand_less);
        }

        expandImageView.setBackground(d);
        this.recyclerViewCurrentPos = position;
    }

    //endregion

    //region public methods

    public static DetailFragment create(String stockSymbol) {

        DetailFragment obj = new DetailFragment();
        obj.currentStockSymbol = stockSymbol;

        return obj;
    }

    //endregion

}
