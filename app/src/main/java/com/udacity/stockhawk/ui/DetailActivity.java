package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.util.Utility;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_STOCK_SYMBOL = "extraStockSymbol";
    private String symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Utility.setToolbar(this);

        String symbol = this.getIntent().getExtras().getString(EXTRA_STOCK_SYMBOL);

        if(TextUtils.isEmpty(symbol))
            throw new RuntimeException("Please, provide an stock symbol in order to call this activity!");

        this.symbol = symbol;

        //There are two kinds of detail fragment: list and graph
        this.loadDetailFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_display_type);
        this.setDisplayModeMenuItemIcon(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_change_display_type) {

            PrefUtils.toggleDetailsDisplayMode(this);
            setDisplayModeMenuItemIcon(item);

            this.loadDetailFragment();

            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    //region private aux methods

    private GraphFragment getGraphFragment() {
        String tag = this.getString(R.string.fragment_graph_tag);
        Fragment fragment = this.getSupportFragmentManager().findFragmentByTag(tag);

        if(fragment == null)
            return null;

        return (GraphFragment) fragment;
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDetailsDisplayMode(this)
                .equals(getString(R.string.pref_detail_display_mode_graph_key))) {
            item.setIcon(R.drawable.ic_action_list);
        } else {
            item.setIcon(R.drawable.ic_action_graph);
        }
    }

    private void loadDetailFragment() {
        String listKey = this.getString(R.string.pref_detail_display_mode_list_key);
        //String graphKey = this.getString(R.string.pref_detail_display_mode_graph_key);

        String currentDetailDisplayMode = PrefUtils.getDetailsDisplayMode(this);
        String tag;
        Fragment frag;

        if(currentDetailDisplayMode.equals(listKey)) {

            frag = DetailFragment.create(this.symbol);
            tag = this.getString(R.string.fragment_detail_tag);
        }
        else {

            frag = GraphFragment.create(this.symbol);
            tag = this.getString(R.string.fragment_graph_tag);
        }

        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_detail, frag, tag)
                .commit();
    }

    //endregion
}
