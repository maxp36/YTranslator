package com.test.maxp36.ytranslator.Fragments;


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
import android.support.v7.widget.ListViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.Toast;

import com.test.maxp36.ytranslator.MyCursorAdapter;
import com.test.maxp36.ytranslator.MySQLiteOpenHelper;
import com.test.maxp36.ytranslator.R;

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
        Toast toast = Toast.makeText(getContext(), "onActivityCreated", Toast.LENGTH_SHORT);
        toast.show();

        ids = new ArrayList<>();

        searchEditText = (AppCompatEditText)getActivity().findViewById(R.id.history_search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cursorAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listView = (ListViewCompat)getActivity().findViewById(R.id.history_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String tag = ((LinearLayoutCompat)parent.getAdapter().getView(position, )).getTag().toString();
                String tag = view.getTag().toString();
                if (ids.contains(tag)) {
                    ids.remove(tag);
                    //view.setSelected(false);
                    view.setBackgroundColor(Color.TRANSPARENT);
                    Toast toast = Toast.makeText(getContext(), "REMOVE, view.tag = " + view.getTag(), Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    ids.add(tag);
                    //view.setSelected(true);
                    view.setBackgroundColor(getResources().getColor(R.color.colorLightGreenA200));
                    System.out.println("**" + ids.size());
                    Toast toast = Toast.makeText(getContext(), "ADD, view.tag = " + view.getTag(), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        /*listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ids.contains(view.getTag().toString())) {
                    ids.remove(view.getTag().toString());
                } else {
                    ids.add((String)view.getTag());
                }
                Toast toast = Toast.makeText(getContext(), "onItemSelected", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
        loadListView();

    }

    @Override
    public void upgradeList() {
        reloadListView();
    }

    public void reloadListView() {
        loadListView();
        MarksFragment marksFragment = (MarksFragment)getParentFragment();
        if (marksFragment != null) {
            FavoritesFragment fragment = (FavoritesFragment) getFragmentManager().findFragmentByTag(marksFragment.getAdapter().getFavoritesFragmentTag());
            if (fragment != null) {
                fragment.loadListView();
            }
        }
    }

    public void loadListView() {
        new AsyncLoadDatabase().execute();
    }

    public void removeItems() {
        new AsyncRemoveItems().execute();
    }

    private class AsyncLoadDatabase extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try {
                SQLiteOpenHelper mySQLiteOpenHelper = new MySQLiteOpenHelper(getContext());
                db = mySQLiteOpenHelper.getReadableDatabase();
                Toast toast = Toast.makeText(getContext(), "AsyncLoadDatabase", Toast.LENGTH_SHORT);
                toast.show();
            } catch (SQLiteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
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
            } catch (SQLiteException ex) {
                ex.printStackTrace();
            }

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

    private class AsyncLoadSearchData extends AsyncTask<String, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try {
                SQLiteOpenHelper mySQLiteOpenHelper = new MySQLiteOpenHelper(getContext());
                db = mySQLiteOpenHelper.getReadableDatabase();
                Toast toast = Toast.makeText(getContext(), "AsyncLoadDatabase", Toast.LENGTH_SHORT);
                toast.show();
            } catch (SQLiteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected Cursor doInBackground(String... params) {
            try {
                cursor = db.query("HISTORY",
                        null,
                        " ORIGINAL_TEXT LIKE '%" + params[0] + "%' OR TRANSLATED_TEXT LIKE '%" + params[0] + "%'",
                        null,
                        null,
                        null,
                        "_id DESC");
            } catch (SQLiteException ex) {
                ex.printStackTrace();
            }
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            searchCursor = cursor;
        }
    }

    private class AsyncRemoveItems extends AsyncTask<Object, Object, Void> {
        private SQLiteDatabase dbTemp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                SQLiteOpenHelper mySQLiteOpenHelper = new MySQLiteOpenHelper(getContext());
                dbTemp = mySQLiteOpenHelper.getWritableDatabase();
                Toast toast = Toast.makeText(getContext(), "AsyncRemoveItems", Toast.LENGTH_SHORT);
                toast.show();
            } catch (SQLiteException ex) {
                ex.printStackTrace();
            }
            System.out.println("onPreExecute" + ids.size());
        }

        @Override
        protected Void doInBackground(Object... params) {
            try {
                System.out.println("*******************" + ids.size());
                for (String s : ids) {
                    dbTemp.delete("HISTORY",
                            "_id = ?",
                            new String[]{s});
                }
                ids.clear();

            } catch (SQLiteException ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dbTemp.close();
            reloadListView();
            //cursorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }
}
