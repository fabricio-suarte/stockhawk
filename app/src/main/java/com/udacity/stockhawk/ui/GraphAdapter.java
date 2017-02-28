package com.udacity.stockhawk.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.HistoryDataTranslator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An Adapter for Graph data seting...
 */

class GraphAdapter {

    private static final String TAG = GraphAdapter.class.getCanonicalName();

    private JSONArray stockData;
    private LineChart chart;
    private Context context;
    private final DateFormat dateFormat;

    GraphAdapter(Context context, LineChart chart) {
        this.chart = chart;
        this.context = context;
        this.dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
    }

    public void setData(JSONArray data) {
        this.stockData = data;
        this.plotData();
    }

    private void plotData() {

        this.chart.clear();

        if(this.stockData != null) {

            JSONObject obj;
            Entry entry;
            List<Entry> entries = new ArrayList<>();
            final List<String> dates = new ArrayList<>();

            try {
                for (int i = 0; i < this.stockData.length(); i++) {
                    obj = this.stockData.getJSONObject(i);
                    entry = this.getEntry(obj, i);

                    if(entry != null) {
                        entries.add(entry);
                        dates.add( this.getLabelDate(obj));
                    }
                }
            }
            catch (Exception ex) {

                Log.e(TAG, "An error occurred while processing graph data: " + ex.toString());
                return;
            }

            // XAxis settings.
            // the labels that should be drawn on the XAxis
            IAxisValueFormatter formatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return dates.get((int) value);
                }
            };

            XAxis xAxis = this.chart.getXAxis();
            xAxis.setValueFormatter(formatter);
            xAxis.setTextColor( ContextCompat.getColor(this.context, R.color.android_white));
            xAxis.setTextSize( this.context.getResources().getDimension(R.dimen.graph_xAxis_text_size));

            //YAxis
            YAxis yAxisL = this.chart.getAxisLeft();
            yAxisL.setTextColor(ContextCompat.getColor(this.context, R.color.android_white));
            yAxisL.setTextSize(this.context.getResources().getDimension(R.dimen.graph_yAxis_text_size));

            YAxis yAxisR = this.chart.getAxisRight();
            yAxisR.setTextColor(ContextCompat.getColor(this.context, R.color.android_white));
            yAxisR.setTextSize(this.context.getResources().getDimension(R.dimen.graph_yAxis_text_size));

            //Legend
            Legend legend = this.chart.getLegend();
            legend.setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimaryLight));
            legend.setTextSize(this.context.getResources().getDimension(R.dimen.graph_description_text_size));

            //Line data set and data
            LineDataSet dataSet = new LineDataSet(entries, this.context.getString(R.string.graph_entries_label));
            dataSet.setValueTextColor( ContextCompat.getColor(this.context, R.color.colorAccent));
            dataSet.setValueTextSize( this.context.getResources().getDimension(R.dimen.graph_value_text_size));
            dataSet.setHighLightColor( ContextCompat.getColor(this.context, R.color.colorPrimaryDark) );
            LineData data = new LineData(dataSet);

            //Description settings
            Description description = new Description();
            description.setText(this.context.getString(R.string.graph_label));
            description.setTextSize( this.context.getResources().getDimension(R.dimen.graph_description_text_size));
            description.setTextColor( ContextCompat.getColor(this.context, R.color.colorPrimaryLight));

            //setting all the above settings...
            this.chart.setDescription(description);
            this.chart.getXAxis().setValueFormatter(formatter);
            this.chart.setData(data);
            this.chart.invalidate(); // refresh
        }
    }

    private Entry getEntry(JSONObject obj, int position) throws JSONException {

        float closeVal = (float) obj.getDouble(HistoryDataTranslator.JSON_ATTRIBUTE_CLOSE);
//        float openVal = (float) obj.getDouble(HistoryDataTranslator.JSON_ATTRIBUTE_OPEN);
//        float highVal = (float) obj.getDouble(HistoryDataTranslator.JSON_ATTRIBUTE_HIGH);
//        float lowVal = (float) obj.getDouble(HistoryDataTranslator.JSON_ATTRIBUTE_LOW);

        //holder.volume.setText(obj.getString(HistoryDataTranslator.JSON_ATTRIBUTE_VOLUME));

        return new Entry(position, closeVal);
    }

    private String getLabelDate(JSONObject obj)  throws JSONException{

        Date date = new Date(obj.getLong(HistoryDataTranslator.JSON_ATTRIBUTE_DATE));
        String dateLabel = this.dateFormat.format(date);

        return dateLabel;
    }

}