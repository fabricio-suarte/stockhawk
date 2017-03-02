package com.udacity.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.StockHawkApp;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.message.MessageDecisionTreeBuilder;
import com.udacity.stockhawk.message.MessageNode;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.util.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The main {@link Fragment} implementation for this app
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler{

    //region constants

    private static final int STOCK_LOADER = 0;

    //endregion

    //region attributes

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private StockAdapter adapter;
    private CallbackListener listener;

    //endregion

    //region constructor

    public MainFragment() {
        // Required empty public constructor
    }

    //endregion

    //region callback interface for this fragment

    public interface CallbackListener {
        void onStockSelected(String symbol);
        void onAddStockClick();
    }

    //endregion

    //region fragment overrides

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, root);

        //floating action button
        this.fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(@SuppressWarnings("UnusedParameters") View v) {
               if(listener != null)
                   listener.onAddStockClick();
            }
        });


        this.adapter = new StockAdapter(this.getActivity(), this);
        this.stockRecyclerView.setAdapter(adapter);
        this.stockRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        this.swipeRefreshLayout.setOnRefreshListener(this);
        this.swipeRefreshLayout.setRefreshing(true);
        this.onRefresh();

        QuoteSyncJob.initialize(this.getActivity());

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());

                getActivity().getContentResolver().delete(Contract.Symbol.makeUriForSymbol(symbol), null, null);
                getActivity().getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);

                //Notify widgets of this data change
                Intent dataUpdatedIntent = new Intent(StockHawkApp.ACTION_DATA_UPDATED);
                getActivity().sendBroadcast(dataUpdatedIntent);
            }

        }).attachToRecyclerView(stockRecyclerView);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //Let's set the loader
        this.getLoaderManager().initLoader(STOCK_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof CallbackListener) {
            this.listener = (CallbackListener) context;
        }
        else {
            throw new RuntimeException("In order to use this Fragment, activity must implement its CallBackListener interface!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        this.listener = null;
    }

    //endregion

    //region LoaderManager.LoaderCallbacks<Cursor> implementation

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this.getActivity(),
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.swipeRefreshLayout.setRefreshing(false);
        this.adapter.setCursor(data);

        this.setErrorMessage();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.swipeRefreshLayout.setRefreshing(false);
        this.adapter.setCursor(null);
    }

    //endregion

    //region StockAdapter.StockAdapterOnClickHandler overrides

    @Override
    public void onClick(String symbol) {

        if(this.listener != null)
            this.listener.onStockSelected(symbol);
    }

    //endregion

    //region SwipeRefreshLayout.OnRefreshListener implementation

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this.getActivity());

        Activity context = this.getActivity();

        this.swipeRefreshLayout.setRefreshing(false);

        if( !Utility.networkUp(this.getActivity())) {

            Toast.makeText(context, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        }

        this.setErrorMessage();
    }

    //endregion

    //region public methods

    public void refreshLayout() {
        this.swipeRefreshLayout.setRefreshing(true);
    }

    public void notifyDataSetChanged() {
        this.adapter.notifyDataSetChanged();
    }

    //endregion

    //region private aux methods

    private void setErrorMessage() {

        MessageNode root = MessageDecisionTreeBuilder.buildTree(this.getActivity(),
                this.adapter.getItemCount());

        String message = MessageNode.getResultMessage(root);

        if( ! TextUtils.isEmpty(message)) {

            //There is a message to be shown
            this.error.setText(message);
            this.error.setVisibility(View.VISIBLE);
        }
        else{
            this.error.setVisibility(View.GONE);
        }

    }

    //endregion
}
