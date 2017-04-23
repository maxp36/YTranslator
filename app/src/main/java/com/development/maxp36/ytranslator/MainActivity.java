package com.development.maxp36.ytranslator;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.development.maxp36.ytranslator.Fragments.AboutFragment;
import com.development.maxp36.ytranslator.Fragments.MarksFragment;
import com.development.maxp36.ytranslator.Fragments.TranslatorFragment;


public class MainActivity extends AppCompatActivity {

    private Fragment translatorFragment = null;
    private Fragment marksFragment = null;
    private Fragment aboutFragment = null;

    private static final String TRANSLATOR_FRAGMENT_TAG = "translatorFragment";
    private static final String MARKS_FRAGMENT_TAG = "marksFragment";
    private static final String ABOUT_FRAGMENT_TAG = "aboutFragment";

    private static final String ID_TAB_TAG = "id_tab";
    private int id_tab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppDefault);
        setContentView(R.layout.activity_main);

        /*Восстановление номера прошлой открытой вкладки*/
        try {
            id_tab = savedInstanceState.getInt(ID_TAB_TAG);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            id_tab = 0;
        }

        /*Восстановление или инициализация фрагментов*/
        translatorFragment = getSupportFragmentManager().findFragmentByTag(TRANSLATOR_FRAGMENT_TAG);
        if (translatorFragment == null) {
            translatorFragment = new TranslatorFragment();
        }

        marksFragment = getSupportFragmentManager().findFragmentByTag(MARKS_FRAGMENT_TAG);
        if (marksFragment == null) {
            marksFragment = new MarksFragment();
        }

        aboutFragment = getSupportFragmentManager().findFragmentByTag(ABOUT_FRAGMENT_TAG);
        if (aboutFragment == null) {
            aboutFragment = new AboutFragment();
        }

        initBottomNavigationBar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*Сохранение номера открытой вкладки*/
        outState.putInt(ID_TAB_TAG, id_tab);
    }

    private void initBottomNavigationBar() {

        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar)findViewById(R.id.bottom_navigation);
        bottomNavigationBar.addItem(new BottomNavigationItem(R.mipmap.ic_translate, R.string.text_translator))
                .addItem(new BottomNavigationItem(R.mipmap.ic_mark, R.string.text_mark))
                .addItem(new BottomNavigationItem(R.mipmap.ic_about, R.string.text_about))
                .initialise();

        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                switch (position) {
                    case 0 : {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content, translatorFragment, TRANSLATOR_FRAGMENT_TAG)
                                .commit();
                        id_tab = 0;
                        break;
                    }
                    case 1 : {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content, marksFragment, MARKS_FRAGMENT_TAG)
                                .commit();
                        id_tab = 1;
                        break;
                    }
                    case 2 : {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content, aboutFragment, ABOUT_FRAGMENT_TAG)
                                .commit();
                        id_tab = 2;
                        break;
                    }
                }
            }

            @Override
            public void onTabUnselected(int position) {
            }

            @Override
            public void onTabReselected(int position) {
            }
        });

        /*Выбор текущей вкладки*/
        bottomNavigationBar.selectTab(id_tab, true);
    }
}
