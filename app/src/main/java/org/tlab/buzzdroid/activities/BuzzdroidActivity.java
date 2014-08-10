package org.tlab.buzzdroid.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.tlab.buzzdroid.R;
import org.tlab.buzzdroid.models.Article;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BuzzdroidActivity extends ListActivity {
    private static final String TAG = BuzzdroidActivity.class.getSimpleName();

    GoogleAnalyticsTracker tracker;

    final static String ANALYTICS_ID = "UA-3314949-15";

    private static String RECENT_ARTICLE_URL = "http://api.buzzurl.jp/api/articles/v1/json/{userId}";
    private static String RECENT_SEARCH_URL = "http://api.buzzurl.jp/api/articles/v1/json/{userId}/keyword/{keyword}";

    // Menu
    private static final int MENU_ID_RELOAD = 0;
    private static final int MENU_ID_SEARCH = 1;
    private static final int MENU_ID_ADD = 2;
    private static final int MENU_ID_SETTINGS = 3;
    private static final int MENU_ID_ABOUT = 4;

    ArrayAdapter<String> mAdapter;
    ArrayList<Article> mArticles;

    private ProgressDialog mDialog;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(ANALYTICS_ID, 60, this);

        mArticles = new ArrayList<Article>(1);
        mAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item);
        setListAdapter(mAdapter);

        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long id) {
                Article article = mArticles.get(position);
                showURLWithBrowser(article.getUrl());
            }

        });

        getArticles();
    }

    @Override
    protected void onStart() {
        super.onStart();
        tracker.trackPageView("/bookmark_list");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /* create menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuReload = menu.add(0, MENU_ID_RELOAD, 0, getString(R.string.menu_reload));
        MenuItem menuAdd = menu.add(0, MENU_ID_ADD, 1, getString(R.string.menu_add));
        MenuItem menuSearch = menu.add(0, MENU_ID_SEARCH, 2, getString(R.string.menu_search));
        MenuItem menuSettings = menu.add(0, MENU_ID_SETTINGS, 3, getString(R.string.menu_settings));
        MenuItem menuAbout = menu.add(0, MENU_ID_ABOUT, 4, getString(R.string.menu_about));

        menuReload.setIcon(android.R.drawable.ic_menu_rotate);
        menuSearch.setIcon(android.R.drawable.ic_menu_search);
        menuAdd.setIcon(android.R.drawable.ic_menu_add);
        menuSettings.setIcon(android.R.drawable.ic_menu_preferences);
        menuAbout.setIcon(android.R.drawable.ic_menu_info_details);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_RELOAD:
                getArticles();
                break;
            case MENU_ID_SEARCH:
                setNameDialog().show();
                break;
            case MENU_ID_ADD:
                addBookmark();
                break;
            case MENU_ID_SETTINGS:
                showSettings();
                break;
            case MENU_ID_ABOUT:
                showAbout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getArticles() {
        if (!checkLogin()) {
            Toast.makeText(this, getString(R.string.alert_login), Toast.LENGTH_SHORT).show();
            showSettings();
            return;
        }
        new ArticleDownloadTask(this).execute();
    }

    private void searchArticles(String keyword) {
        if (!checkLogin()) {
            Toast.makeText(this, getString(R.string.alert_login), Toast.LENGTH_SHORT).show();
            showSettings();
            return;
        }

        new KeywordSearchTask(this).execute(keyword);
    }

    private void addBookmark() {
        Intent intent = new Intent(this, AddBookmarkActivity.class);
        startActivity(intent);
    }

    private void showAbout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View aboutView = inflater.inflate(R.layout.about, null);

        AlertDialog mAlert = new AlertDialog.Builder(this)
                .setIcon(R.drawable.icon).setTitle(R.string.app_name)
                .setView(aboutView).setPositiveButton(getString(R.string.button_ok), null).create();
        mAlert.show();
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void showURLWithBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(url.toString()));
        startActivity(intent);
    }

    private boolean checkLogin() {
        String username = getUsername();

        if (username.equals("")) {
            return false;
        }
        return true;
    }

    private String getUsername() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString("username", "");
    }

    private Dialog setNameDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View entryView = factory.inflate(R.layout.dialog_keyword_search, null);

        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.menu_search))
                .setView(entryView)
                .setPositiveButton(getString(R.string.button_search), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText edit = (EditText) entryView.findViewById(R.id.edit_keyword);
                        String keyword = edit.getText().toString();
                        if (!keyword.equals("")) {
                            searchArticles(keyword);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.message_enter_keyword), Toast.LENGTH_SHORT).show();
                            setNameDialog().show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    class ArticleDownloadTask extends AsyncTask<Void, Void, List<Article>> {

        private BuzzdroidActivity mActivity;
        private Gson gson;

        public ArticleDownloadTask(BuzzdroidActivity activity) {
            this.mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAdapter.clear();

            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.fetching_bookmarks));
            mDialog.setIndeterminate(true);
            mDialog.show();

            gson = new Gson();
        }

        @Override
        protected List<Article> doInBackground(Void... params) {
            String sUrl = RECENT_ARTICLE_URL;

            String username = getUsername();

            if (!username.equals("")) {
                sUrl = sUrl.replace("{userId}", username);
            }
            // TODO: check userID

            try {
                URL url = new URL(sUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url
                        .openConnection();
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    List<Article> articles = gson.fromJson(reader, new TypeToken<List<Article>>() {
                    }.getType());
                    return articles;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(List<Article> articles) {
            for (Article article : articles) {
                mAdapter.add(article.getTitle());
                mArticles.add(article);
            }

            mAdapter.notifyDataSetChanged();

            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }
    }

    class KeywordSearchTask extends AsyncTask<String, Void, List<Article>> {

        private BuzzdroidActivity mActivity;
        private Gson gson;

        public KeywordSearchTask(BuzzdroidActivity acivity) {
            this.mActivity = acivity;
            gson = new Gson();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAdapter.clear();

            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.searching_bookmarks));
            mDialog.setIndeterminate(true);
            mDialog.show();
        }

        @Override
        protected List<Article> doInBackground(String... params) {
            String keyword = params[0];
            String sUrl = RECENT_SEARCH_URL;
            String username = getUsername();

            if (!username.equals("")) {
                sUrl = sUrl.replace("{userId}", username);
            }

            if (!keyword.equals("")) {
                try {
                    sUrl = sUrl.replace("{keyword}", URLEncoder.encode(keyword, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            // TODO: check userID

            try {
                URL url = new URL(sUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url
                        .openConnection();
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    List<Article> articles = gson.fromJson(reader, new TypeToken<List<Article>>() {
                    }.getType());
                    return articles;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(List<Article> articles) {
            for (Article article : articles) {
                mAdapter.add(article.getTitle());
                mArticles.add(article);
            }

            mAdapter.notifyDataSetChanged();

            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }
    }
}
