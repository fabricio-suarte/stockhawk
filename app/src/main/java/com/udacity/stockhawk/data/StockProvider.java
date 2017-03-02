package com.udacity.stockhawk.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class StockProvider extends ContentProvider {

    private static final int QUOTE = 100;
    private static final int QUOTE_FOR_SYMBOL = 101;

    private static final int SYMBOL = 102;
    private static final int SYMBOL_FOR_NAME = 103;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private DbHelper dbHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        //Quotes matches
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE, QUOTE);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE_WITH_SYMBOL, QUOTE_FOR_SYMBOL);

        //Symbol matches
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_SYMBOL, SYMBOL);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_SYMBOL_WITH_NAME, SYMBOL_FOR_NAME);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)) {
            case QUOTE:
                returnCursor = db.query(
                        Contract.Quote.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case QUOTE_FOR_SYMBOL:
                returnCursor = db.query(
                        Contract.Quote.TABLE_NAME,
                        projection,
                        Contract.Quote.COLUMN_SYMBOL + " = ?",
                        new String[]{Contract.Quote.getStockFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );

            case SYMBOL:
                returnCursor = db.query(
                        Contract.Symbol.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case SYMBOL_FOR_NAME:
                returnCursor = db.query(
                        Contract.Symbol.TABLE_NAME,
                        projection,
                        Contract.Symbol.COLUMN_NAME + " = ?",
                        new String[]{Contract.Quote.getStockFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );

                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        Context context = getContext();
        if (context != null){
            returnCursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnUri;

        switch (uriMatcher.match(uri)) {
            case QUOTE:
                db.insert(
                        Contract.Quote.TABLE_NAME,
                        null,
                        values
                );
                returnUri = Contract.Quote.URI;
                break;

            case SYMBOL:
                db.insert(
                        Contract.Symbol.TABLE_NAME,
                        null,
                        values
                );
                returnUri = Contract.Symbol.URI;
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        Context context = getContext();
        if (context != null){
            context.getContentResolver().notifyChange(uri, null);
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;

        if (null == selection) {
            selection = "1";
        }
        switch (uriMatcher.match(uri)) {
            case QUOTE:
                rowsDeleted = db.delete(
                        Contract.Quote.TABLE_NAME,
                        selection,
                        selectionArgs
                );

                break;

            case QUOTE_FOR_SYMBOL:
                String symbol = Contract.Quote.getStockFromUri(uri);
                rowsDeleted = db.delete(
                        Contract.Quote.TABLE_NAME,
                        '"' + symbol + '"' + " =" + Contract.Quote.COLUMN_SYMBOL,
                        selectionArgs
                );
                break;

            case SYMBOL:
                rowsDeleted = db.delete(
                        Contract.Symbol.TABLE_NAME,
                        selection,
                        selectionArgs
                );

                break;

            case SYMBOL_FOR_NAME:
                String name = Contract.Symbol.getSymbolFromUri(uri);
                rowsDeleted = db.delete(
                        Contract.Symbol.TABLE_NAME,
                        '"' + name + '"' + " =" + Contract.Symbol.COLUMN_NAME,
                        selectionArgs
                );
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        if (rowsDeleted != 0) {
            Context context = getContext();
            if (context != null){
                context.getContentResolver().notifyChange(uri, null);
            }
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        int returnCount = 0;
        Context context = getContext();

        switch (uriMatcher.match(uri)) {
            case QUOTE:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        db.insert(
                                Contract.Quote.TABLE_NAME,
                                null,
                                value
                        );
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (context != null) {
                    context.getContentResolver().notifyChange(uri, null);
                }

                return returnCount;

            case SYMBOL:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        db.insert(
                                Contract.Symbol.TABLE_NAME,
                                null,
                                value
                        );
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (context != null) {
                    context.getContentResolver().notifyChange(uri, null);
                }

                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }
}
