package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.util.Utility;

public class MainActivity extends AppCompatActivity
        implements  MainFragment.CallbackListener,
                    AddStockDialog.CallbackListener{

    //region attributes

    private boolean twoPanel;
    private String symbol;

    //endregion

    //region this activity overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);

        //Configure my custom toolbar
        Utility.setToolbar(this);

        View container = this.findViewById(R.id.second_pane_container);
        if(container != null) {

            //We have a tablet layout
            this.twoPanel = true;

            //Once we have a two panel layout and this activity may have been called from our widget,
            //it is necessary to check by the symbol in order to loader the detail panel properly
            Intent intent = this.getIntent();
            if(intent != null) {
                String symbol = intent.getStringExtra(DetailActivity.EXTRA_STOCK_SYMBOL);

                this.symbol = symbol;

                if( ! TextUtils.isEmpty(symbol)) {
                    this.onStockSelected(symbol);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);

        MenuItem item1 = menu.findItem(R.id.action_change_units);
        this.setDisplayModeMenuItemIcon(item1);

        MenuItem item2 = menu.findItem(R.id.action_change_display_type);
        if(item2 != null) {
            this.setDetailDisplayModeMenuItemIcon(item2);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            this.setDisplayModeMenuItemIcon(item);

            MainFragment fragment = this.getMainFragment();
            if(fragment != null)
                fragment.notifyDataSetChanged();

            return true;
        }
        else if(id == R.id.action_change_display_type) {

            PrefUtils.toggleDetailsDisplayMode(this);
            this.setDetailDisplayModeMenuItemIcon(item);

            this.onStockSelected(this.symbol);

            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region AddStockDialog.CallbackListener implementation

    @Override
    public void onAddStock(String symbol, boolean isValid, boolean errorDuringValidation) {

        String message = null;

        if (isValid) {

            if (Utility.networkUp(this)) {

                MainFragment fragment = this.getMainFragment();
                if(fragment != null)
                    fragment.refreshLayout();

            } else {
                message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }
        else if(errorDuringValidation) {

            message = getString(R.string.error_server_connection);

        }
        else{
            message = getString(R.string.error_stock_does_not_exist, symbol);

        }

        if(message != null)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //endregion

    //region MainFragment.CallbackListener

    @Override
    public void onStockSelected(String symbol) {

        if(this.twoPanel) {

            String currentDetailDisplayMode = PrefUtils.getDetailsDisplayMode(this);
            String detailDisplayListKey = this.getString(R.string.pref_detail_display_mode_list_key);

            Fragment frag;
            String tag;

            this.symbol = symbol;

            if(currentDetailDisplayMode.equals(detailDisplayListKey)) {

                frag =  DetailFragment.create(symbol);
                tag = this.getString(R.string.fragment_detail_tag);
            }
            else {
                frag =  GraphFragment.create(symbol);
                tag = this.getString(R.string.fragment_graph_tag);
            }

            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.second_pane_container, frag, tag)
                    .commit();
        }
        else {

            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(DetailActivity.EXTRA_STOCK_SYMBOL, symbol);

            this.startActivity(detailIntent);
        }
    }

    //endregion

    @Override
    public void onAddStockClick() {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    //region private aux methods

    @Nullable
    private MainFragment getMainFragment() {
        String tag = this.getString(R.string.fragment_main_tag);
        Fragment fragment = this.getSupportFragmentManager().findFragmentByTag(tag);

        if(fragment == null)
            return null;

        return (MainFragment) fragment;
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    private void setDetailDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDetailsDisplayMode(this)
                .equals(getString(R.string.pref_detail_display_mode_graph_key))) {
            item.setIcon(R.drawable.ic_action_list);
        } else {
            item.setIcon(R.drawable.ic_action_graph);
        }
    }

    //endregion
}
