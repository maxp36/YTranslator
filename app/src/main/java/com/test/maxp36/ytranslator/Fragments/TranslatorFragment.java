package com.test.maxp36.ytranslator.Fragments;


import android.content.res.XmlResourceParser;
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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.test.maxp36.ytranslator.Key;
import com.test.maxp36.ytranslator.MainActivity;
import com.test.maxp36.ytranslator.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TranslatorFragment extends Fragment {

    private final String API_KEY_TRANSLATE = "trnsl.1.1.20170320T085807Z.9efac8a0a4c1c670.fbd7a049f4aa9dc25c91ababad73a547b6a6ef5d";
    private final String API_KEY_DICTIONARY = "dict.1.1.20170321T050842Z.164a2d1fe76ac6c4.5c6aea6fbbf63d1a2d8212a009737084a12cd207";
    private String urlTranslate = "https://translate.yandex.net/api/v1.5/tr/translate";
    private String urlDictionary = "https://dictionary.yandex.net/api/v1/dicservice/lookup";
    private String keyFromLanguage;
    private String keyToLanguage;

    private Map<String, String> mapLanguages;
    private LinkedHashMap<Key, String> dictionaryArticle;
    //private ConcurrentHashMap<Key, String> dictionaryArticle = new ConcurrentHashMap<>();
    //private Map<Key, String> dictionaryArticle = Collections.synchronizedMap(new LinkedHashMap<Key, String>());


    private AppCompatEditText editText;
    private AppCompatTextView resultText;
    private AppCompatSpinner fromLanguage;
    private AppCompatSpinner toLanguage;
    private LinearLayoutCompat dictionaryArticleView;

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
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initLanguagesHashMapResource();
        initAppBar();
        initEditTextView();
        initResultTextView();
        dictionaryArticleView = (LinearLayoutCompat)getActivity().findViewById(R.id.dictionaryArticle);

        /*try {
            fromLanguage.setSelection(savedInstanceState.getInt("fromLanguageItemId"));
            toLanguage.setSelection(savedInstanceState.getInt("toLanguageItemId"));
            keyFromLanguage = toLanguage.getSelectedItem().toString();
            keyToLanguage = fromLanguage.getSelectedItem().toString();
            editText.setText(savedInstanceState.getString("textForTranslate"));
            System.out.println("savedInstanceState: " + savedInstanceState.getInt("fromLanguageItemId") + savedInstanceState.getInt("toLanguageItemId") + savedInstanceState.getString("textForTranslate"));
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }*/

    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("fromLanguageItemId", fromLanguage.getSelectedItemPosition());
        outState.putInt("toLanguageItemId", toLanguage.getSelectedItemPosition());
        outState.putString("textForTranslate", editText.getText().toString());
    }*/

    private void initAppBar() {
        initSpinners();

        AppCompatImageButton changeLanguages = (AppCompatImageButton)getActivity().findViewById(R.id.button_change_languages);
        changeLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyFromLanguage = toLanguage.getSelectedItem().toString();
                keyToLanguage = fromLanguage.getSelectedItem().toString();
                int tempId = fromLanguage.getSelectedItemPosition();
                fromLanguage.setSelection(toLanguage.getSelectedItemPosition());
                toLanguage.setSelection(tempId);
                if (!editText.getText().toString().equals("")) {
                    editText.setText(resultText.getText());
                    //translate();
                }
            }
        });
    }

    private void initSpinners() {

        fromLanguage = (AppCompatSpinner)getActivity().findViewById(R.id.first_language_spinner);
        fromLanguage.setSelection(0);
        fromLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                keyFromLanguage = fromLanguage.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        toLanguage = (AppCompatSpinner)getActivity().findViewById(R.id.second_language_spinner);
        toLanguage.setSelection(1);
        toLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                keyToLanguage = toLanguage.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private String convertLanguageToKey(String language) {
        return mapLanguages.get(language);
    }

    private void initEditTextView() {

        editText = (AppCompatEditText)getActivity().findViewById(R.id.translator_edit_text);
        AppCompatImageButton clearEditText = (AppCompatImageButton)getActivity().findViewById(R.id.clear_edit_text_button);
        AppCompatImageButton mikeEditText = (AppCompatImageButton)getActivity().findViewById(R.id.mike_edit_text_button);
        AppCompatImageButton voiceEditText = (AppCompatImageButton)getActivity().findViewById(R.id.voice_edit_text_button);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!editText.getText().toString().equals("")) {
                    translate();
                } else {
                    resultText.setText("");
                    dictionaryArticleView.removeAllViews();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                /*afterTextChanged(Editable s) - метод вызывается, чтобы уведомить нас, что где-то в строке s, текст был изменен.
                В этом методе можно вносить изменения в текст s,
                но будьте осторожны, чтобы не зациклиться, потому что любые изменения в s рекурсивно вызовут этот же метод.*/
            }
        });

        clearEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
    }

    private void initResultTextView() {
        resultText = (AppCompatTextView)getActivity().findViewById(R.id.result_text_view);
    }

    private void initLanguagesHashMapResource() {
        mapLanguages = new HashMap<>();
        XmlResourceParser parser = getContext().getResources().getXml(R.xml.languages_map);

        String key;
        String value;

        try {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("Item")) {
                    key = parser.getAttributeValue(1); //в данном случае, ключом будет являться название языка
                    value = parser.getAttributeValue(0);
                    mapLanguages.put(key, value);
                }
                eventType = parser.next();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void translate() {

        new AsyncParsingDictionary().execute(editText.getText().toString());
        new AsyncParsingTranslate().execute(editText.getText().toString());

    }

    private class AsyncParsingDictionary extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            boolean isDef = false;
            boolean isTr = false;
            boolean isSyn = false;
            boolean isMean = false;
            boolean isEx = false;

            boolean isNotEmpty = false;
            dictionaryArticle = new LinkedHashMap<>();

            try {
                String url_s = urlDictionary + "?key=" + API_KEY_DICTIONARY + "&text=" +
                        URLEncoder.encode(params[0], "UTF-8") + "&lang=" + convertLanguageToKey(keyFromLanguage) + "-"
                        + convertLanguageToKey(keyToLanguage)
                        + "&ui=ru";
                URL url = new URL(url_s);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
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

                //парсинг xml
                int event = myParser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    String name = myParser.getName();

                    switch (event) {
                        case XmlPullParser.START_TAG:
                            System.out.println("START_TAG");
                            if (name.equals("mean")) {
                                isMean = true;
                            } else if (name.equals("ex")) {
                                isEx = true;
                            } else if (name.equals("tr")) {
                                isTr = true;
                            } else if (name.equals("syn")) {
                                isSyn = true;
                            } else if (name.equals("def")) {
                                isDef = true;
                                isNotEmpty = true;
                            }
                            for (int i = 0; i < myParser.getAttributeCount(); i++) {
                                /* получение аргументов:
                                * gen - Род существительного для тех языков, где это актуально (может отсутствовать).
                                * pos - Часть речи (может отсутствовать).
                                * ts - В атрибуте ts может указываться транскрипция искомого слова.
                                * num - Число (для имен существительных).
                                *   Возможные значения: pl - указывается для существительных во множественном числе.*/
                                dictionaryArticle.put(new Key(myParser.getAttributeName(i)), myParser.getAttributeValue(i));
                            }
                            break;

                        case XmlPullParser.TEXT:
                            if (!isEx) {
                                if (isDef) {
                                    dictionaryArticle.put(new Key("def"), myParser.getText());
                                    isDef = false;
                                } else if (isTr) {
                                    dictionaryArticle.put(new Key("tr"), myParser.getText());
                                    isTr = false;
                                } else if (isSyn) {
                                    dictionaryArticle.put(new Key("syn"), myParser.getText());
                                    isSyn = false;
                                } else if (isMean) {
                                    dictionaryArticle.put(new Key("mean"), myParser.getText());
                                    isMean = false;
                                }
                            }
                            break;

                        case XmlPullParser.END_TAG:
                            if (name.equals("ex")) {
                                isEx = false;
                            }
                            break;
                    }
                    event = myParser.next();
                }

                stream.close();
                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return isNotEmpty;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //Toast toast = Toast.makeText(getContext(), "boolen = " + aBoolean.toString(), Toast.LENGTH_SHORT);
            //toast.show();
            System.out.println("size " + dictionaryArticle.size());
            if (aBoolean) {
                LinkedHashMap<Key, String> dictionaryArticleTemp = dictionaryArticle;
                fillDictionaryViewFromMap(dictionaryArticleTemp);

                System.out.println("size " + dictionaryArticleTemp.size());
                for (Map.Entry<Key, String> entry : dictionaryArticleTemp.entrySet()) {
                    System.out.println(entry.getKey().getKey() + " = " + entry.getValue());
                }

                //dictionaryArticle.clear();

            } else {
                dictionaryArticleView.removeAllViews();
            }
        }

        private void fillDictionaryViewFromMap(LinkedHashMap<Key, String> dictionaryArticleTemp) {

            dictionaryArticleView.removeAllViews();

            //LinearLayoutCompat horisontalContainer = new LinearLayoutCompat(getContext());
            FlexboxLayout horisontalContainer = new FlexboxLayout(getContext());
            //GridLayout gridContainer = new GridLayout(getContext());
            AppCompatTextView elem = new AppCompatTextView(getContext());

            String gen = "";
            String pos = "";
            String ts = "";
            String num = "";

            String sTr = "";
            String sGen = "";
            String sSyn = "";


            int countTr = 0;
            //boolean isGridLayout = false;
            boolean isTr = false;
            boolean isSyn = false;
            boolean isGen = false;
            //boolean isDef = false;
            //boolean isMean = false;

            for (final Map.Entry<Key, String> entry : dictionaryArticleTemp.entrySet()) {

                /*if (!entry.getKey().getKey().equals("syn")
                        && !entry.getKey().getKey().equals("gen")
                        && !entry.getKey().getKey().equals("pos")
                        && !entry.getKey().getKey().equals("num")) {
                    //horisontalContainer = new LinearLayoutCompat(getContext());
                    horisontalContainer = new FlexboxLayout(getContext());
                }*/

                /*if (entry.getKey().getKey().equals("mean")) {

                    elem = new AppCompatTextView(getContext());
                    elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    elem.setTextSize(16);
                    elem.setTextColor(getResources().getColor(R.color.colorTextGray));

                    if (!isMean) {
                        elem.setText("  (" + entry.getValue() + ")");
                        horisontalContainer = new LinearLayoutCompat(getContext());
                        horisontalContainer.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                        horisontalContainer.setOrientation(LinearLayoutCompat.HORIZONTAL);
                        horisontalContainer.addView(elem);
                        isMean = true;
                    }/* else {                  // выводиться только 1 mean
                        elem.setText(", " + entry.getValue());
                    }*/

                //}
                if (entry.getKey().getKey().equals("tr")) {

                    isTr = true;
                    sTr = entry.getValue();
                    //isMean = false;
                    countTr++;
                    //isDef = false;

                    //if (!isGridLayout) {
                    //    isGridLayout = true;
                    //    gridContainer = new GridLayout(getContext());
                    //    gridContainer.setOrientation(GridLayout.HORIZONTAL);
                    //    gridContainer.setColumnCount(2);
                    //}

                    //if (isTr || isSyn) {
                    //    isSyn = false;
                    //    gridContainer.addView(horisontalContainer);
                    //}
                    //isTr = true;

                    elem = new AppCompatTextView(getContext());
                    elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    elem.setTextSize(16);
                    elem.setTextColor(getResources().getColor(R.color.colorTextGray));
                    if (!keyFromLanguage.equals(keyToLanguage)) {
                        elem.setText(countTr + "  ");
                    } else {
                        elem.setText("    ");
                    }

                    //gridContainer.addView(elem);

                    /*horisontalContainer = new LinearLayoutCompat(getContext());
                    horisontalContainer.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    horisontalContainer.setOrientation(LinearLayoutCompat.HORIZONTAL);
                    horisontalContainer.addView(elem);*/

                    horisontalContainer = new FlexboxLayout(getContext());
                    horisontalContainer.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    horisontalContainer.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
                    horisontalContainer.setAlignItems(FlexboxLayout.ALIGN_ITEMS_FLEX_END);
                    horisontalContainer.setAlignContent(FlexboxLayout.ALIGN_CONTENT_FLEX_START);
                    horisontalContainer.addView(elem);

                    elem = new AppCompatTextView(getContext());
                    elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    elem.setTextSize(16);
                    elem.setTextColor(getResources().getColor(R.color.colorTextBlue));
                    elem.setClickable(true);
                    elem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            keyFromLanguage = toLanguage.getSelectedItem().toString();
                            keyToLanguage = fromLanguage.getSelectedItem().toString();
                            int tempId = fromLanguage.getSelectedItemPosition();
                            fromLanguage.setSelection(toLanguage.getSelectedItemPosition());
                            toLanguage.setSelection(tempId);
                            editText.setText(entry.getValue());
                        }
                    });
                    elem.setText(entry.getValue());

                    horisontalContainer.addView(elem);

                    if (!gen.isEmpty()) {
                        isTr = false;
                        isGen = true;
                        elem = new AppCompatTextView(getContext());
                        elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                        elem.setTextSize(14);
                        elem.setTextColor(getResources().getColor(R.color.colorTextGray));
                        elem.setText(" " + gen);

                        horisontalContainer.addView(elem);
                        gen = "";
                    }

                } else if (entry.getKey().getKey().equals("syn")) {

                    //isMean = false;
                    //isTr = false;
                    //isSyn = true;

                    if (isGen) {
                        elem.setText(" " + sGen + ", ");
                        isGen = false;
                    } else if (isSyn) {
                        elem.setText(sSyn + ", ");
                        isSyn = false;
                    } else if (isTr) {
                        elem.setText(sTr + ", ");
                        isTr = false;
                    }
                    isSyn = true;
                    sSyn = entry.getValue();

                    elem = new AppCompatTextView(getContext());
                    elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    elem.setTextSize(16);
                    elem.setTextColor(getResources().getColor(R.color.colorTextBlue));
                    elem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            keyFromLanguage = toLanguage.getSelectedItem().toString();
                            keyToLanguage = fromLanguage.getSelectedItem().toString();
                            int tempId = fromLanguage.getSelectedItemPosition();
                            fromLanguage.setSelection(toLanguage.getSelectedItemPosition());
                            toLanguage.setSelection(tempId);
                            editText.setText(entry.getValue());
                        }
                    });
                    elem.setText(entry.getValue());

                    horisontalContainer.addView(elem);

                    if (!gen.isEmpty()) {
                        isSyn = false;
                        isGen = true;
                        elem = new AppCompatTextView(getContext());
                        elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                        elem.setTextSize(14);
                        elem.setTextColor(getResources().getColor(R.color.colorTextGray));
                        elem.setText(" " + gen);

                        horisontalContainer.addView(elem);
                        gen = "";
                    }

                } else if (entry.getKey().getKey().equals("def")) {

                    //isMean = false;
                    //isDef = true;

                    //if (isTr || isSyn) {
                    //    isTr = false;
                    //    isSyn = false;
                    //    gridContainer.addView(horisontalContainer);
                    //    dictionaryArticleView.addView(gridContainer);
                    //}

                    //isGridLayout = false;
                    countTr = 0;

                    elem = new AppCompatTextView(getContext());
                    elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    elem.setTextSize(20);
                    elem.setTypeface(Typeface.DEFAULT_BOLD);
                    elem.setTextColor(getResources().getColor(R.color.colorDark));
                    elem.setText(entry.getValue() + " ");
                    //((ViewGroup)elem.getParent()).removeView(elem);

                    System.out.println("I'm here");

                    /*horisontalContainer = new LinearLayoutCompat(getContext());
                    horisontalContainer.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    horisontalContainer.setOrientation(LinearLayoutCompat.HORIZONTAL);*/
                    horisontalContainer = new FlexboxLayout(getContext());
                    horisontalContainer.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                    horisontalContainer.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
                    horisontalContainer.setAlignItems(FlexboxLayout.ALIGN_ITEMS_FLEX_END);
                    horisontalContainer.setAlignContent(FlexboxLayout.ALIGN_CONTENT_FLEX_START);
                    horisontalContainer.addView(elem);

                    if (countTr != 0) {
                        countTr = 0;
                        //float scale = getResources().getDisplayMetrics().density;
                        //int dpAsPixels = (int) (32 * scale + 0.5f);
                        //horisontalContainer.setPadding(0, dpAsPixels, 0, 0);
                    }
                    //horisontalContainer.addView(elem);

                    if (!gen.isEmpty()) {
                        elem = new AppCompatTextView(getContext());
                        elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                        elem.setTextSize(14);
                        elem.setTextColor(getResources().getColor(R.color.colorTextGray));
                        elem.setText(gen + " ");

                        horisontalContainer.addView(elem);
                        gen = "";
                    }
                    if (!pos.isEmpty()) {
                        elem = new AppCompatTextView(getContext());
                        elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                        elem.setTextSize(14);
                        elem.setTextColor(getResources().getColor(R.color.colorTextGreen));
                        elem.setText(pos + " ");

                        horisontalContainer.addView(elem);
                        pos = "";
                    }
                    if (!ts.isEmpty()) {
                        elem = new AppCompatTextView(getContext());
                        elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                        elem.setTextSize(16);
                        elem.setTextColor(getResources().getColor(R.color.colorTextGray));
                        elem.setText(ts + " ");

                        horisontalContainer.addView(elem);
                        ts = "";
                    }
                    if (!num.isEmpty()) {
                        elem = new AppCompatTextView(getContext());
                        elem.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                        elem.setTextSize(14);
                        elem.setTextColor(getResources().getColor(R.color.colorTextGray));
                        elem.setText(num + " ");

                        horisontalContainer.addView(elem);
                        num = "";
                    }

                    //dictionaryArticleView.addView(horisontalContainer);

                } else if (entry.getKey().getKey().equals("gen")) {
                    sGen = gen;
                    gen = entry.getValue();
                } else if (entry.getKey().getKey().equals("pos")) {
                    pos = entry.getValue();
                }
                /*else if (entry.getKey().getKey().equals("ts")) { // неотображаются юникод символы
                    ts = entry.getValue();
                }*/
                else if (entry.getKey().getKey().equals("num")) {
                    num = entry.getValue();
                }

                //Toast toast1 = Toast.makeText(getContext(), "Size " + horisontalContainer.getChildCount(), Toast.LENGTH_SHORT);
                //toast1.show();

                /*if (horisontalContainer.getChildCount() != 0
                        && !entry.getKey().getKey().equals("syn")
                        && !entry.getKey().getKey().equals("gen")
                        && !entry.getKey().getKey().equals("pos")
                        && !entry.getKey().getKey().equals("num")) {
                    System.out.println("dictionaryArticleView.addView(horisontalContainer); ");
                    dictionaryArticleView.addView(horisontalContainer);
                }*/

                if (horisontalContainer.getChildCount() != 0
                        && (entry.getKey().getKey().equals("def")
                        || entry.getKey().getKey().equals("tr"))) {
                    System.out.println("dictionaryArticleView.addView(horisontalContainer); ");
                    dictionaryArticleView.addView(horisontalContainer);
                }
            }
            System.out.println("" +  dictionaryArticleView.getChildCount());
        }

    }

    private class AsyncParsingTranslate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String s = null;

            try {
                String url_s = urlTranslate + "?key=" + API_KEY_TRANSLATE + "&text=" +
                        URLEncoder.encode(params[0], "UTF-8") + "&lang=" + convertLanguageToKey(keyFromLanguage) + "-" + convertLanguageToKey(keyToLanguage);
                URL url = new URL(url_s);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
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

                //парсинг xml
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
                            //get text
                            if (name.equals("text")) {
                                s = text;
                            }
                            break;
                    }
                    event = myParser.next();
                    connection.disconnect();

                }

                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
                s = e.getMessage();
                return s;
            }

            return s;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            /*AppCompatTextView tv = new AppCompatTextView(getContext());
            tv.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
            tv.setText("Test text ");

            AppCompatTextView tv1 = new AppCompatTextView(getContext());
            tv1.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
            tv1.setText("Test text");
            boolean b = true;
            LinearLayoutCompat horisontalContainer1 = null;
            //horisontalContainer1.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                   // LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
            //horisontalContainer1.setOrientation(LinearLayoutCompat.HORIZONTAL);
            //horisontalContainer1.addView(tv);
            if (b) {
                horisontalContainer1 = new LinearLayoutCompat(getContext());
                horisontalContainer1.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                horisontalContainer1.setOrientation(LinearLayoutCompat.HORIZONTAL);
                tv = new AppCompatTextView(getContext());
                tv.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                tv.setText("new Test text");
                horisontalContainer1.addView(tv);
            }
            dictionaryArticleView.addView(horisontalContainer1);
            if (b) {
                tv = new AppCompatTextView(getContext());
                tv.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                tv.setText("new Test text1");
                horisontalContainer1.addView(tv);
            }*/
            //horisontalContainer1 = new LinearLayoutCompat(getContext());

            //dictionaryArticleView.addView(horisontalContainer1);

            /*tv = new AppCompatTextView(getContext());
            tv.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
            tv.setText("new Test text");
            horisontalContainer1 = new LinearLayoutCompat(getContext());
            horisontalContainer1.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
            horisontalContainer1.setOrientation(LinearLayoutCompat.HORIZONTAL);
            horisontalContainer1.addView(tv);


            dictionaryArticleView.addView(horisontalContainer1);
            horisontalContainer1.removeAllViews();
            horisontalContainer1.addView(tv);*/


            resultText.setText(s);
        }

        /*private void connectToUrl(String urlType, String api_key, String text) {
            try {
                String url_s = urlType + "?key=" + api_key + "&text=" +
                        text + "&lang=" + convertLanguageToKey(keyFromLanguage) + "-" + convertLanguageToKey(keyToLanguage);
                URL url = new URL(url_s);
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(1000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }
}
