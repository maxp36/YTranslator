package com.test.maxp36.ytranslator;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.test.maxp36.ytranslator.Fragments.MarksFragment;
import com.test.maxp36.ytranslator.Fragments.SettingsFragment;
import com.test.maxp36.ytranslator.Fragments.TranslatorFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationBar bottomNavigationBar;
    //private Fragment contentFragment;
    private Fragment translatorFragment = null;
    private Fragment marksFragment = null;
    private Fragment settingsFragment = null;
    private static final String TRANSLATOR_FRAGMENT = "translatorFragment";
    private static final String MARKS_FRAGMENT = "marksFragment";
    private static final String SETTINGS_FRAGMENT = "settingsFragment";
    private int id_tab;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppDefault);
        setContentView(R.layout.activity_main);

        try {
            id_tab = savedInstanceState.getInt("id_tab");
        } catch (NullPointerException ex) {
            id_tab = 0;
        }

        translatorFragment = (TranslatorFragment)getSupportFragmentManager().findFragmentByTag(TRANSLATOR_FRAGMENT);
        if (translatorFragment == null) {
            translatorFragment = new TranslatorFragment();
        }
        marksFragment = (MarksFragment)getSupportFragmentManager().findFragmentByTag(MARKS_FRAGMENT);
        if (marksFragment == null) {
            marksFragment = new MarksFragment();
        }
        settingsFragment = (SettingsFragment)getSupportFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT);
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }

        /*if(contentFragment != null) {
            Toast toast = Toast.makeText(getApplicationContext(), contentFragment.toString(), Toast.LENGTH_SHORT);
            toast.show();
        }*/

        initBottomNavigationBar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id_tab", id_tab);
    }

    private void initBottomNavigationBar() {
        Toast toast = Toast.makeText(getApplicationContext(), "initBottomNavigationBar()", Toast.LENGTH_SHORT);
        toast.show();

        bottomNavigationBar = (BottomNavigationBar)findViewById(R.id.bottom_navigation);
        bottomNavigationBar.addItem(new BottomNavigationItem(R.mipmap.ic_translate, R.string.text_translator))
                .addItem(new BottomNavigationItem(R.mipmap.ic_mark, R.string.text_mark))
                .addItem(new BottomNavigationItem(R.mipmap.ic_settings, R.string.text_settings))
                .initialise();
        bottomNavigationBar.setAutoHideEnabled(true);

        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {

                /*if (contentFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(contentFragment)
                            .commit();
                }*/

                switch (position) {
                    case 0 : {
                        /*contentFragment = new TranslatorFragment();
                        Toast toast = Toast.makeText(getApplicationContext(), contentFragment.toString(), Toast.LENGTH_SHORT);
                        toast.show();*/
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content, translatorFragment, TRANSLATOR_FRAGMENT)
                                .commit();
                        id_tab = 0;
                        break;
                    }
                    case 1 : {
                        /*contentFragment = new MarksFragment();
                        Toast toast = Toast.makeText(getApplicationContext(), contentFragment.toString(), Toast.LENGTH_SHORT);
                        toast.show();*/
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content, marksFragment, MARKS_FRAGMENT)
                                .commit();
                        id_tab = 1;
                        break;
                    }
                    case 2 : {
                        /*contentFragment = new SettingsFragment();
                        Toast toast = Toast.makeText(getApplicationContext(), contentFragment.toString(), Toast.LENGTH_SHORT);
                        toast.show();*/
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content, settingsFragment, SETTINGS_FRAGMENT)
                                .commit();
                        id_tab = 2;
                        break;
                    }
                }
                /*getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, contentFragment)
                        .commit();*/
            }

            @Override
            public void onTabUnselected(int position) {
            }

            @Override
            public void onTabReselected(int position) {
            }
        });

        bottomNavigationBar.selectTab(id_tab, true);
    }
}
