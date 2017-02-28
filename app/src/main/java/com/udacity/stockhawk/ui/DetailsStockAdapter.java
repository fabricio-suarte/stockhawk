package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.HistoryDataTranslator;
import com.udacity.stockhawk.data.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class DetailsStockAdapter extends RecyclerView.Adapter<DetailsStockAdapter.StockViewHolder> {

    private static final String LOG = DetailsStockAdapter.class.getCanonicalName();

    private final Context context;
    private final DecimalFormat dollarFormat;
    private final DateFormat dateFormat;

    private JSONArray data;
    private final DetailsStockAdapterOnClickHandler clickHandler;
    private TextView emptyTextView;

    DetailsStockAdapter(Context context, DetailsStockAdapterOnClickHandler clickHandler, TextView emptyTextView) {
        this.context = context;
        this.clickHandler = clickHandler;
        this.emptyTextView = emptyTextView;

        this.dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        this.dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
    }

    void setData(JSONArray data) {
        this.data = data;
        notifyDataSetChanged();

        if(this.getItemCount() == 0){
            this.emptyTextView.setVisibility(View.VISIBLE);
        }
        else {
            this.emptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(context).inflate(R.layout.list_item_detail_stock, parent, false);

        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        JSONObject obj;

        if(data == null || data.length() == 0)
            return;

        try {
            obj = data.getJSONObject(position);

            Date date = new Date(obj.getLong(HistoryDataTranslator.JSON_ATTRIBUTE_DATE));
            holder.date.setText( this.dateFormat.format(date) );
            holder.close.setText(dollarFormat.format(obj.getDouble(HistoryDataTranslator.JSON_ATTRIBUTE_CLOSE)));

            holder.open.setText(dollarFormat.format(obj.getDouble(HistoryDataTranslator.JSON_ATTRIBUTE_OPEN)));
            holder.high.setText(dollarFormat.format(obj.getDouble(HistoryDataTranslator.JSON_ATTRIBUTE_HIGH)));
            holder.low.setText(dollarFormat.format(obj.getDouble(HistoryDataTranslator.JSON_ATTRIBUTE_LOW)));
            holder.volume.setText(obj.getString(HistoryDataTranslator.JSON_ATTRIBUTE_VOLUME));
        }
        catch (JSONException ex) {
            Log.e(LOG, "Error while trying to parse Json data!", ex);
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (data != null) {
            count = data.length();
        }
        return count;
    }

    interface DetailsStockAdapterOnClickHandler {
        void onClick(int position, View expandImageView, View detailContainer);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.textViewItemDate)
        TextView date;

        @BindView(R.id.textViewItemClose)
        TextView close;

        @BindView(R.id.item_detail_data_container)
        View detailDataContainer;

        //These ones are inside the container
        @BindView(R.id.textViewItemDetailDataOpen)
        TextView open;

        @BindView(R.id.textViewItemDetailDataHigh)
        TextView high;

        @BindView(R.id.textViewItemDetailDataLow)
        TextView low;

        @BindView(R.id.textViewItemDetailDataVolume)
        TextView volume;

        StockViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            View container = v.findViewById(R.id.item_detail_data_container);
            View expand = v.findViewById(R.id.imageViewItemExpand);

            clickHandler.onClick(adapterPosition, expand, container);
        }
    }
}
