package com.development.maxp36.ytranslator.Fragments;


import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;

import com.development.maxp36.ytranslator.Key;
import com.development.maxp36.ytranslator.MySQLiteOpenHelper;
import com.development.maxp36.ytranslator.R;
import com.google.android.flexbox.FlexboxLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class TranslatorFragment extends Fragment {

    /*Данные для HTTP-запроса*/
    private final String API_KEY_TRANSLATE = "trnsl.1.1.20170320T085807Z.9efac8a0a4c1c670.fbd7a049f4aa9dc25c91ababad73a547b6a6ef5d";
    private final String API_KEY_DICTIONARY = "dict.1.1.20170321T050842Z.164a2d1fe76ac6c4.5c6aea6fbbf63d1a2d8212a009737084a12cd207";
    private final String urlTranslate = "https://translate.yandex.net/api/v1.5/tr/translate";
    private final String urlDictionary = "https://dictionary.yandex.net/api/v1/dicservice/lookup";

    /*Язык запроса и результата в полной форме*/
    private String nameFromLanguage;
    private String nameToLanguage;

    /*Карта соответствий названия языка и языкового ключа*/
    private Map<String, String> mapLanguages;

    /*Контейнер для хранения результатов запроса сервиса Яндекс.Словарь*/
    private LinkedHashMap<Key, String> dictionaryArticle;

    private AsyncParsingDictionary asyncParsingDictionary;
    private AsyncParsingTranslate asyncParsingTranslate;

    private AppCompatEditText originalText;
    private AppCompatTextView resultText;
    private AppCompatSpinner fromLanguage;
    private AppCompatSpinner toLanguage;
    private LinearLayoutCompat dictionaryArticleView;
    private AppCompatTextView apiHelp;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.translator_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dictionaryArticleView = (LinearLayoutCompat)getActivity().findViewById(R.id.dictionaryArticle);
        apiHelp = (AppCompatTextView)getActivity().findViewById(R.id.api_help);

        initLanguagesMap();
        initAppBar();
        initOriginalTextView();
        initResultTextView();
        initButtoms();
    }


    private void initLanguagesMap() {
        mapLanguages = new HashMap<>();
        XmlResourceParser parser = getContext().getResources().getXml(R.xml.languages_map);

        String key;
        String value;

        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("Item")) {
                    /*В данном случае, ключом является название языка*/
                    value = parser.getAttributeValue(0);
                    key = parser.getAttributeValue(1);
                    mapLanguages.put(key, value);
                }
                eventType = parser.next();
            }
        }
        catch (IOException | XmlPullParserException ex) {
            ex.printStackTrace();
        }
    }

    private void initAppBar() {
        initSpinners();
        AppCompatImageButton changeLanguages = (AppCompatImageButton)getActivity().findViewById(R.id.button_change_languages);
        changeLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameFromLanguage = toLanguage.getSelectedItem().toString();
                nameToLanguage = fromLanguage.getSelectedItem().toString();

                int tempId = fromLanguage.getSelectedItemPosition();
                fromLanguage.setSelection(toLanguage.getSelectedItemPosition());
                toLanguage.setSelection(tempId);

                if (!originalText.getText().toString().isEmpty()) {
                    originalText.setText(resultText.getText());
                }
            }
        });
    }

    private void initSpinners() {
        /*Инициализация спинера выбора искодного языка*/
        fromLanguage = (AppCompatSpinner)getActivity().findViewById(R.id.first_language_spinner);
        /*Изначальный выбор - Русский*/
        fromLanguage.setSelection(0);
        fromLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nameFromLanguage = fromLanguage.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /*Инициализация спинера выбора конечного языка*/
        toLanguage = (AppCompatSpinner)getActivity().findViewById(R.id.second_language_spinner);
        /*Изначальный выбор - Английский*/
        toLanguage.setSelection(1);
        toLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nameToLanguage = toLanguage.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initOriginalTextView() {
        originalText = (AppCompatEditText)getActivity().findViewById(R.id.translator_edit_text);
        originalText.setImeOptions(EditorInfo.IME_ACTION_GO);
        originalText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        originalText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(originalText.getApplicationWindowToken(), 0);

                    if (!originalText.getText().toString().isEmpty() && !resultText.getText().toString().isEmpty()) {
                        new AsyncAddItem().execute(originalText.getText().toString(),
                                resultText.getText().toString(),
                                convertLanguageToKey(nameFromLanguage).toUpperCase(),
                                convertLanguageToKey(nameToLanguage).toUpperCase(),
                                "0");
                    }
                    return true;
                }
                return false;
            }
        });

        originalText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                translate();

                /*Отображение информации об используемом API только при переводе*/
                if (s.toString().isEmpty()) {
                    apiHelp.setVisibility(View.INVISIBLE);
                } else {
                    apiHelp.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initResultTextView() {
        resultText = (AppCompatTextView)getActivity().findViewById(R.id.result_text_view);
    }

    private void initButtoms() {
        AppCompatImageButton clearOriginalText = (AppCompatImageButton)getActivity().findViewById(R.id.clear_edit_text_button);
        clearOriginalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                originalText.setText("");
            }
        });

        AppCompatImageButton markFavorites = (AppCompatImageButton)getActivity().findViewById(R.id.mark_result_text_button);
        markFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!originalText.getText().toString().isEmpty() && !resultText.getText().toString().isEmpty()) {
                    new AsyncAddItem().execute(originalText.getText().toString(),
                            resultText.getText().toString(),
                            convertLanguageToKey(nameFromLanguage).toUpperCase(),
                            convertLanguageToKey(nameToLanguage).toUpperCase(),
                            "1");
                }
            }
        });
    }

    private String convertLanguageToKey(String language) {
        return mapLanguages.get(language);
    }

    private void translate() {
        /*Если AsyncParsingDictionary и AsyncParsingTranslate уже запущены, прерываем их
        * и запускаем новые*/
        if (asyncParsingDictionary != null && asyncParsingTranslate != null) {
            if (asyncParsingDictionary.getStatus() == AsyncTask.Status.RUNNING) {
                asyncParsingDictionary.cancel(true);
            }
            if (asyncParsingTranslate.getStatus() == AsyncTask.Status.RUNNING) {
                asyncParsingTranslate.cancel(true);
            }
        }
        asyncParsingDictionary = new AsyncParsingDictionary();
        asyncParsingTranslate = new AsyncParsingTranslate();

        asyncParsingDictionary.execute(originalText.getText().toString());
        asyncParsingTranslate.execute(originalText.getText().toString());

    }

    /*Запрос словарной статьи и вывод на экран.
    * Передается 1 параметр - текст для перевода.*/
    private class AsyncParsingDictionary extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            boolean isDef = false;
            boolean isTr = false;
            boolean isSyn = false;

            boolean isNotEmpty = false;

            dictionaryArticle = new LinkedHashMap<>();

            if (!isCancelled() && !params[0].isEmpty()) {
                try {
                    String url_s = urlDictionary
                            + "?key="
                            + API_KEY_DICTIONARY
                            + "&text="
                            + URLEncoder.encode(params[0], "UTF-8")
                            + "&lang="
                            + convertLanguageToKey(nameFromLanguage)
                            + "-"
                            + convertLanguageToKey(nameToLanguage)
                            + "&ui=ru";
                    URL url = new URL(url_s);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(1000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream stream = connection.getInputStream();

                    XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myParser = xmlFactoryObject.newPullParser();

                    myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myParser.setInput(stream, null);

                    /*Парсинг xml-файла результата*/
                    int event = myParser.getEventType();
                    while (event != XmlPullParser.END_DOCUMENT) {
                        String name = myParser.getName();

                        switch (event) {
                            case XmlPullParser.START_TAG:
                                if (name.equals("tr")) {
                                    isTr = true;
                                } else if (name.equals("syn")) {
                                    isSyn = true;
                                } else if (name.equals("def")) {
                                    isDef = true;
                                    isNotEmpty = true;
                                }
                                for (int i = 0; i < myParser.getAttributeCount(); i++) {
                                /* Получение аргументов:
                                * gen - Род существительного для тех языков, где это актуально (может отсутствовать).
                                * pos - Часть речи (может отсутствовать).
                                * ts - В атрибуте ts может указываться транскрипция искомого слова.
                                * num - Число (для имен существительных).
                                *   Возможные значения: pl - указывается для существительных во множественном числе.*/
                                    dictionaryArticle.put(new Key(myParser.getAttributeName(i)), myParser.getAttributeValue(i));
                                }
                                break;

                            case XmlPullParser.TEXT:
                                    if (isDef) {
                                        dictionaryArticle.put(new Key("def"), myParser.getText());
                                        isDef = false;
                                    } else if (isTr) {
                                        dictionaryArticle.put(new Key("tr"), myParser.getText());
                                        isTr = false;
                                    } else if (isSyn) {
                                        dictionaryArticle.put(new Key("syn"), myParser.getText());
                                        isSyn = false;
                                    }
                                break;

                            case XmlPullParser.END_TAG:
                                break;
                        }
                        event = myParser.next();
                    }

                    stream.close();
                    connection.disconnect();

                } catch (ProtocolException ex) {
                    ex.printStackTrace();
                    return false;
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                    return false;
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    return false;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return false;
                } catch (XmlPullParserException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            return isNotEmpty;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (!isCancelled()) {
                if (aBoolean) {
                    LinkedHashMap<Key, String> dictionaryArticleTemp = dictionaryArticle;
                    fillDictionaryViewFromMap(dictionaryArticleTemp);
                } else {
                    dictionaryArticleView.removeAllViews();
                }
            }
        }

        /*Выводит результат запроса на экран*/
        private void fillDictionaryViewFromMap(LinkedHashMap<Key, String> dictionaryArticleTemp) {

            dictionaryArticleView.removeAllViews();

            FlexboxLayout horisontalContainer = new FlexboxLayout(getContext());
            AppCompatTextView elem = new AppCompatTextView(getContext());

            String gen = "";

            String sTr = "";
            String sGen = "";
            String sSyn = "";

            int countTr = 0;

            boolean isTr = false;
            boolean isSyn = false;
            boolean isGen = false;

            for (final Map.Entry<Key, String> entry : dictionaryArticleTemp.entrySet()) {

                if (entry.getKey().getKey().equals("tr")) {

                    isTr = true;
                    sTr = entry.getValue();
                    countTr++;

                    horisontalContainer = new FlexboxLayout(getContext());
                    horisontalContainer.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    horisontalContainer.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
                    horisontalContainer.setAlignItems(FlexboxLayout.ALIGN_ITEMS_FLEX_END);
                    horisontalContainer.setAlignContent(FlexboxLayout.ALIGN_CONTENT_FLEX_START);

                    /*Нумерация*/
                    elem = new AppCompatTextView(getContext());
                    elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    elem.setTextSize(16);
                    elem.setTextColor(getResources().getColor(R.color.colorGrayReallyDark));
                    elem.setText(countTr + "  ");

                    horisontalContainer.addView(elem);

                    /*Перевод*/
                    elem = new AppCompatTextView(getContext());
                    elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    elem.setTextSize(16);
                    elem.setTextColor(getResources().getColor(R.color.colorSecondary));
                    elem.setClickable(true);
                    elem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            nameFromLanguage = toLanguage.getSelectedItem().toString();
                            nameToLanguage = fromLanguage.getSelectedItem().toString();
                            int tempId = fromLanguage.getSelectedItemPosition();
                            fromLanguage.setSelection(toLanguage.getSelectedItemPosition());
                            toLanguage.setSelection(tempId);
                            originalText.setText(entry.getValue());
                        }
                    });
                    elem.setText(entry.getValue());

                    horisontalContainer.addView(elem);

                    /*Род*/
                    if (!gen.isEmpty()) {
                        isTr = false;
                        isGen = true;
                        elem = new AppCompatTextView(getContext());
                        elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                        elem.setTextSize(14);
                        elem.setTextColor(getResources().getColor(R.color.colorGrayReallyDark));
                        elem.setText(" " + gen);

                        horisontalContainer.addView(elem);
                    }

                } else if (entry.getKey().getKey().equals("syn")) {

                    if (isGen) {
                        elem.setText(" " + sGen + ", ");
                        isGen = false;
                    } else if (isSyn) {
                        elem.setText(sSyn + ", ");
                    } else if (isTr) {
                        elem.setText(sTr + ", ");
                        isTr = false;
                    }
                    isSyn = true;
                    sSyn = entry.getValue();

                    /*Синоним*/
                    elem = new AppCompatTextView(getContext());
                    elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    elem.setTextSize(16);
                    elem.setTextColor(getResources().getColor(R.color.colorSecondary));
                    elem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            nameFromLanguage = toLanguage.getSelectedItem().toString();
                            nameToLanguage = fromLanguage.getSelectedItem().toString();
                            int tempId = fromLanguage.getSelectedItemPosition();
                            fromLanguage.setSelection(toLanguage.getSelectedItemPosition());
                            toLanguage.setSelection(tempId);
                            originalText.setText(entry.getValue());
                        }
                    });
                    elem.setText(entry.getValue());

                    horisontalContainer.addView(elem);

                    /*Род*/
                    if (!gen.isEmpty()) {
                        isSyn = false;
                        isGen = true;
                        elem = new AppCompatTextView(getContext());
                        elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                        elem.setTextSize(14);
                        elem.setTextColor(getResources().getColor(R.color.colorGrayReallyDark));
                        elem.setText(" " + gen);

                        horisontalContainer.addView(elem);
                    }

                } else if (entry.getKey().getKey().equals("def")) {

                    isTr = false;
                    isSyn = false;
                    isGen = false;
                    gen = "";
                    countTr = 0;

                    horisontalContainer = new FlexboxLayout(getContext());
                    horisontalContainer.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    horisontalContainer.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
                    horisontalContainer.setAlignItems(FlexboxLayout.ALIGN_ITEMS_FLEX_END);
                    horisontalContainer.setAlignContent(FlexboxLayout.ALIGN_CONTENT_FLEX_START);

                    /*Перевод*/
                    elem = new AppCompatTextView(getContext());
                    elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    elem.setTextSize(20);
                    elem.setTypeface(Typeface.DEFAULT_BOLD);
                    elem.setTextColor(getResources().getColor(R.color.colorBlack));
                    elem.setText(entry.getValue() + " ");

                    horisontalContainer.addView(elem);

                    if (countTr != 0) {
                        countTr = 0;
                    }

                } else if (entry.getKey().getKey().equals("gen")) {
                    sGen = gen;
                    gen = entry.getValue();
                }

                if (horisontalContainer.getChildCount() != 0
                        && (entry.getKey().getKey().equals("def")
                            || entry.getKey().getKey().equals("tr"))) {
                    dictionaryArticleView.addView(horisontalContainer);
                }
            }
        }

    }

    /*Запрос перевода и вывод результата на экран.
    * Передается 1 параметр - текст для перевода.*/
    private class AsyncParsingTranslate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String s = "";
            if (!isCancelled() && !params[0].isEmpty()) {
                try {
                    String url_s = urlTranslate
                            + "?key="
                            + API_KEY_TRANSLATE
                            + "&text="
                            + URLEncoder.encode(params[0], "UTF-8")
                            + "&lang="
                            + convertLanguageToKey(nameFromLanguage)
                            + "-"
                            + convertLanguageToKey(nameToLanguage);
                    URL url = new URL(url_s);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(1000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream stream = connection.getInputStream();

                    XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myParser = xmlFactoryObject.newPullParser();

                    myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myParser.setInput(stream, null);

                    /*Парсинг xml-файла результата*/
                    int event = myParser.getEventType();
                    String text = null;
                    while (event != XmlPullParser.END_DOCUMENT) {
                        String name = myParser.getName();

                        switch (event) {
                            case XmlPullParser.START_TAG:
                                break;
                            case XmlPullParser.TEXT:
                                text = myParser.getText();
                                break;
                            case XmlPullParser.END_TAG:
                                if (name.equals("text")) {
                                    s = text;
                                }
                                break;
                        }
                        event = myParser.next();
                        connection.disconnect();
                    }
                    stream.close();
                } catch (ProtocolException ex) {
                    ex.printStackTrace();
                    return s;
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                    return s;
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    return s;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return s;
                } catch (XmlPullParserException ex) {
                    ex.printStackTrace();
                    return s;
                }
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!isCancelled()) {
                resultText.setText(s);
            }
        }
    }

    /*Добавляет в базу данных запись с пометкой "Избранное"*/
    private class AsyncAddItem extends AsyncTask<String, Void, Void> {

        private SQLiteOpenHelper mySQLiteOpenHelper;
        private SQLiteDatabase db;
        private SQLiteDatabase tempDB;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mySQLiteOpenHelper = new MySQLiteOpenHelper(getContext());
            tempDB = mySQLiteOpenHelper.getReadableDatabase();
            db = mySQLiteOpenHelper.getWritableDatabase();
        }

        @Override
        protected Void doInBackground(String... params) {

            try {
                Cursor c = tempDB.query("HISTORY",
                                        null,
                                        "ORIGINAL_TEXT = ? AND TRANSLATED_TEXT = ?",
                                        new String[]{params[0], params[1]},
                                        null,
                                        null,
                                        null);
                if (c.getCount() == 0) {
                    ContentValues cv = new ContentValues();
                    cv.put("ORIGINAL_TEXT", params[0]);
                    cv.put("TRANSLATED_TEXT", params[1]);
                    cv.put("FROM_LANGUAGE_KEY", params[2]);
                    cv.put("TO_LANGUAGE_KEY", params[3]);
                    cv.put("IS_FAVORITES", Integer.parseInt(params[4]));
                    db.insert("HISTORY", null, cv);
                    db.close();
                }
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    int favorites = c.getInt(c.getColumnIndex("IS_FAVORITES"));
                    if (favorites == 0 && Integer.parseInt(params[4]) == 1) {
                        ContentValues cv = new ContentValues();
                        cv.put("ORIGINAL_TEXT", params[0]);
                        cv.put("TRANSLATED_TEXT", params[1]);
                        cv.put("FROM_LANGUAGE_KEY", params[2]);
                        cv.put("TO_LANGUAGE_KEY", params[3]);
                        cv.put("IS_FAVORITES", 1);
                        db.update("HISTORY", cv, "ORIGINAL_TEXT = ? AND TRANSLATED_TEXT = ?", new String[]{params[0], params[1]});
                        db.close();
                    }
                }
                tempDB.close();
                c.close();

            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }
}
