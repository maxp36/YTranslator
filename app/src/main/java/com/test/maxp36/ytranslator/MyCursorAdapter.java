package com.test.maxp36.ytranslator;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

public class MyCursorAdapter extends CursorAdapter {
    private Map<Integer, AppCompatImageButton> items;
    private LayoutInflater cursorInflater;

    private AppCompatImageButton btnFavorites;
    private AppCompatTextView originalTextView;
    private AppCompatTextView translatedTextView;
    private AppCompatTextView languageTextView;

    private Context context;

    private UpgradeList upgradeList;

    public MyCursorAdapter(Context context, Cursor c, int flags, UpgradeList upgradeList) {
        super(context, c, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.upgradeList = upgradeList;
        items = new HashMap<>();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = cursorInflater.inflate(R.layout.list_view_item, parent, false);
        v.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
        System.out.println("View " + v.getTag().toString() + " id " + v.getId());
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //view.setTag(cursor.getInt(cursor.getColumnIndex("_id")));
        btnFavorites = (AppCompatImageButton) view.findViewById(R.id.button_favorites);
        btnFavorites.setFocusable(false);
        if (cursor.getInt(cursor.getColumnIndex("IS_FAVORITES")) == 1) {
            btnFavorites.setImageResource(R.mipmap.ic_bookmark_green);
            btnFavorites.setTag("1");
        } else {
            btnFavorites.setImageResource(R.mipmap.ic_mark);
            btnFavorites.setTag("0");
        }
        btnFavorites.setOnClickListener(new OnItemClickListener(cursor.getInt(cursor.getColumnIndex("_id")), cursor.getPosition()));

        /*btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnFavorites.getTag().toString().equals("1")) {
                    btnFavorites.setImageResource(R.mipmap.ic_mark);
                    btnFavorites.setTag("0");
                } else {
                    btnFavorites.setImageResource(R.mipmap.ic_bookmark_green);
                    btnFavorites.setTag("1");
                }
                System.out.println("v.getTag() " + view1.getTag() + " btnTag " + btnFavorites.getTag());
            }
        });*/

        originalTextView = (AppCompatTextView)view.findViewById(R.id.original_text_view);
        originalTextView.setText(cursor.getString(cursor.getColumnIndex("ORIGINAL_TEXT")));

        translatedTextView = (AppCompatTextView)view.findViewById(R.id.translated_text_view);
        translatedTextView.setText(cursor.getString(cursor.getColumnIndex("TRANSLATED_TEXT")));

        languageTextView = (AppCompatTextView)view.findViewById(R.id.languages_item_text_view);
        languageTextView.setText(cursor.getString(cursor.getColumnIndex("FROM_LANGUAGE_KEY"))
                + "-" + cursor.getString(cursor.getColumnIndex("TO_LANGUAGE_KEY")));

        items.put(cursor.getPosition(), btnFavorites);

    }

    private class OnItemClickListener implements View.OnClickListener {
        private int position;
        private int id;

        public OnItemClickListener(int id, int position) {
            super();
            this.id = id;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            AppCompatImageButton btn = items.get(position);
            if (btn.getTag().toString().equals("1")) {
                btn.setImageResource(R.mipmap.ic_mark);
                btn.setTag("0");
                new AsyncChangeMark().execute(Integer.toString(id), "0");
            } else {
                btn.setImageResource(R.mipmap.ic_bookmark_green);
                btn.setTag("1");
                new AsyncChangeMark().execute(Integer.toString(id), "1");
            }
            //System.out.println("v.getTag() " + view1.getTag() + " btnTag " + btnFavorites.getTag());
        }
    }


    private class AsyncChangeMark extends AsyncTask<String, Void, Void> {
        private SQLiteOpenHelper mySQLiteOpenHelper;
        private SQLiteDatabase db;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try {
                mySQLiteOpenHelper = new MySQLiteOpenHelper(context);
                db = mySQLiteOpenHelper.getWritableDatabase();
            } catch (SQLiteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(String... params) {

            try {
                ContentValues cv = new ContentValues();
                cv.put("IS_FAVORITES", params[1]);
                db.update("HISTORY", cv, "_id = ?", new String[]{params[0]});
                db.close();

            } catch (SQLiteException ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            upgradeList.upgradeList();
        }
    }

    public interface UpgradeList {
        void upgradeList();
    }
}
