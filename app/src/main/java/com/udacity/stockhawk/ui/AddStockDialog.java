package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.util.Utility;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class AddStockDialog extends DialogFragment {

    //region attributes

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.dialog_stock)
    EditText stock;

//    @SuppressWarnings("WeakerAccess")
//    @BindView(R.id.error)
//    TextView error;

    private AlertDialog dialog;
    private CallbackListener listener;

    private boolean errorDuringStockCheck;

    //endregion

    //region inner interfaces and classes

    public interface CallbackListener {
        void onAddStock(String symbol, boolean isValid, boolean errorDuringValidation);
    }

    /**
     * Checks if a given stock is valid in the Finance API service or not
     */

    class CheckStockAsyncTask extends AsyncTask<String, Object, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            boolean flag = false;

            try {
                Stock yahooStock = YahooFinance.get(params[0]);
                errorDuringStockCheck = false;

                flag = yahooStock != null && yahooStock.getName() != null;
            }
            catch (Exception ex) {
                errorDuringStockCheck = true;
                flag = false;
            }

            return flag;
        }

        @Override
        protected void onPostExecute(Boolean result) {

           //the stocks exists
           if (listener != null) {
               listener.onAddStock(stock.getText().toString(), result, errorDuringStockCheck);
           }

            dismissAllowingStateLoss();
        }
    }

    //endregion

    //region overrides of DialogFragment

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof MainFragment.CallbackListener) {
            this.listener = (AddStockDialog.CallbackListener) context;
        }
        else {
            throw new RuntimeException("In order to use this Fragment, activity must implement its CallBackListener interface!");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);

        stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                addStock();
                return true;
            }
        });

        builder.setView(custom);

        builder.setMessage(getString(R.string.dialog_title));

        builder.setPositiveButton(getString(R.string.dialog_add),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        addStock();

                    }
                });

        builder.setNegativeButton(getString(R.string.dialog_cancel), null);

        dialog = builder.create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        Button addButton = this.dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if(addButton != null) {
            addButton.setEnabled(Utility.networkUp(this.getActivity()));
        }
    }

    //endregion

    //region private aux methods

    private void addStock() {

        new CheckStockAsyncTask().execute(this.stock.getText().toString());
    }

    //endregion
}
