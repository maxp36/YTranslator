package com.development.maxp36.ytranslator.Fragments;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.ListViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.Toast;

import com.development.maxp36.ytranslator.MyCursorAdapter;
import com.development.maxp36.ytranslator.MySQLiteOpenHelper;
import com.development.maxp36.ytranslator.R;

import java.util.ArrayList;
import java.util.List;


public class HistoryFragment extends Fragment implements MyCursorAdapter.UpgradeList {

    private ListViewCompat listView;
    private AppCompatEditText searchEditText;

    private SQLiteDatabase db;
    private Cursor cursor;
    private Cursor searchCursor;
    private CursorAdapter cursorAdapter;

    private static List<String> ids;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ids = new ArrayList<>();

        initSearch();
        initButton();
        initListView();

        loadListView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }

    @Override
    public void upgradeList() {
        reloadListView();
    }

    private void initSearch() {
        searchEditText = (AppCompatEditText)getActivity().findViewById(R.id.history_search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (cursorAdapter != null) {
                    cursorAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initButton() {
        AppCompatImageButton clearSearch = (AppCompatImageButton) getActivity().findViewById(R.id.clear_history_search);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
            }
        });
    }

    private void initListView() {
        listView = (ListViewCompat)getActivity().findViewById(R.id.history_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tag = view.getTag().toString();
                if (ids.contains(tag)) {
                    ids.remove(tag);
                    view.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    ids.add(tag);
                    view.setBackgroundColor(getResources().getColor(R.color.colorSecondaryLight));
                }
            }
        });
    }

    public void loadListView() {
        new AsyncLoadDatabase().execute();
    }

    public void reloadListView() {
        if (searchEditText.getText().toString().isEmpty()) {
            loadListView();
        } else {
            cursorAdapter.getFilter().filter(searchEditText.getText().toString());
        }

        /*Перезагрузка списка в соседней вкладке - FavoritesFragment*/
        MarksFragment marksFragment = (MarksFragment)getParentFragment();
        if (marksFragment != null) {
            FavoritesFragment fragment = (FavoritesFragment) getFragmentManager().findFragmentByTag(marksFragment.getAdapter().getFavoritesFragmentTag());
            if (fragment != null) {
                fragment.loadListView();
            }
        }
    }

    public void removeItems() {
        new AsyncRemoveItems().execute();
    }

    /*Загружает данные из базы данных в список*/
    private class AsyncLoadDatabase extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SQLiteOpenHelper mySQLiteOpenHelper = new MySQLiteOpenHelper(getContext());
            db = mySQLiteOpenHelper.getReadableDatabase();
        }

        @Override
        protected Void doInBackground(Void... params) {
            cursor = db.query("HISTORY",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "_id DESC");

            cursorAdapter = new MyCursorAdapter(getContext(), cursor, 0, HistoryFragment.this);
            cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    AsyncLoadSearchData alsd = new AsyncLoadSearchData();
                    alsd.execute(constraint.toString());
                    while (true) {
                        if (alsd.getStatus() == AsyncTask.Status.FINISHED) {
                            return searchCursor;
                        }
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Parcelable state = listView.onSaveInstanceState();
            listView.setAdapter(cursorAdapter);
            listView.onRestoreInstanceState(state);
        }
    }

    /*Загружает данные, удовлетворяющие условию поиска, в список.
    * Принимает 1 параметр - строку поиска*/
    private class AsyncLoadSearchData extends AsyncTask<String, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SQLiteOpenHelper mySQLiteOpenHelper = new MySQLiteOpenHelper(getContext());
            db = mySQLiteOpenHelper.getReadableDatabase();
        }

        @Override
        protected Cursor doInBackground(String... params) {
            cursor = db.query("HISTORY",
                    null,
                    " ORIGINAL_TEXT LIKE '%" + params[0] + "%' OR TRANSLATED_TEXT LIKE '%" + params[0] + "%'",
                    null,
                    null,
                    null,
                    "_id DESC");
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            searchCursor = cursor;
        }
    }

    /*Удаляет из базы данных все выделенные в списке записи*/
    private class AsyncRemoveItems extends AsyncTask<Void, Void, Void> {
        private SQLiteDatabase dbTemp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SQLiteOpenHelper mySQLiteOpenHelper = new MySQLiteOpenHelper(getContext());
            dbTemp = mySQLiteOpenHelper.getWritableDatabase();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (String s : ids) {
                dbTemp.delete("HISTORY",
                        "_id = ?",
                        new String[]{s});
            }
            ids.clear();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dbTemp.close();
            reloadListView();
        }
    }
}
