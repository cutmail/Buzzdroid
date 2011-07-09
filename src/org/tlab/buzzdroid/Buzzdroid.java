package org.tlab.buzzdroid;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import jp.co.nobot.libYieldMaker.libYieldMaker;

import me.cutmail.buzzurl.Article;
import me.cutmail.buzzurl.ArticleGen;
import net.vvakame.util.jsonpullparser.JsonFormatException;
import net.vvakame.util.jsonpullparser.util.OnJsonObjectAddListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
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

public class Buzzdroid extends ListActivity {
	private static final String TAG = "Buzzdroid";

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		libYieldMaker mv = (libYieldMaker) findViewById(R.id.admakerview);
		mv.setActivity(this);

                // TODO: set URL
                mv.setUrl("");
                mv.startView();

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
	protected void onResume() {
		super.onResume();
//		getArticles();
	}
	
	/* create menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuReload = menu.add(0, MENU_ID_RELOAD, 0, "更新");
		MenuItem menuAdd = menu.add(0, MENU_ID_ADD, 1, "ブックマーク追加");
		MenuItem menuSearch = menu.add(0, MENU_ID_SEARCH, 2, "キーワード検索");
		MenuItem menuSettings = menu.add(0, MENU_ID_SETTINGS, 3, "設定");
		MenuItem menuAbout = menu.add(0, MENU_ID_ABOUT, 4, "情報");

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
			Toast.makeText(this, "ログインしてください。", Toast.LENGTH_SHORT).show();
			showSettings();
			return;
		}
		new ArticleDownloadTask(this).execute();
	}
	
	private void searchArticles(String keyword) {
		if (!checkLogin()) {
			Toast.makeText(this, "ログインしてください。", Toast.LENGTH_SHORT).show();
			showSettings();
			return;
		}
		new KeywordSearchTask(this).execute(keyword);
	}

	private void addBookmark() {
		 Intent intent = new Intent(this, AddBookmark.class);
		 startActivity(intent);
	}

	private void showAbout() {
		LayoutInflater inflater = LayoutInflater.from(this);
		final View aboutView = inflater.inflate(R.layout.about, null);

		AlertDialog mAlert = new AlertDialog.Builder(this)
				.setIcon(R.drawable.icon).setTitle(R.string.app_name)
				.setView(aboutView).setPositiveButton("OK", null).create();
		mAlert.show();
	}

	private void showSettings() {
		Intent intent = new Intent(this, Settings.class);
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
//	    .setIcon(R.drawable.icon)
	    .setTitle("キーワード検索")
	    .setView(entryView)
	    .setPositiveButton("検索", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            EditText edit = (EditText) entryView.findViewById(R.id.edit_keyword);
	            String keyword = edit.getText().toString();
	            if (!keyword.equals("")) {
	            	searchArticles(keyword);
	            } else {
	            	Toast.makeText(getApplicationContext(), "キーワードを入力してください。", Toast.LENGTH_SHORT).show();
	            	setNameDialog().show();
	            }
	        }
	    })
	    .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	dialog.dismiss();
	        }
	    })
	    .create();
	}

	class ArticleDownloadTask extends AsyncTask<Void, Article, Void> {
		
		private Buzzdroid mActivity;
		
		OnJsonObjectAddListener listener = new OnJsonObjectAddListener() {
			@Override
			public void onAdd(Object obj) {
				if (obj instanceof Article) {
					Article article = (Article) obj;
					publishProgress(article);
				}
			}
		};
		
		public ArticleDownloadTask(Buzzdroid activity) {
			this.mActivity = activity;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mAdapter.clear();
			
			mDialog = new ProgressDialog(mActivity);
			mDialog.setTitle("ブックマークを取得中...");
			mDialog.setMessage("ブックマークを取得しています");
			mDialog.setIndeterminate(true);
			mDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
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
					ArticleGen.getList(urlConnection.getInputStream(), listener);
				} finally {
					urlConnection.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JsonFormatException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Article... progress) {
			for (Article article : progress) {
				mAdapter.add(article.getTitle());
				mAdapter.notifyDataSetChanged();
				mArticles.add(article);
			}
			
			if (mDialog != null) {
				mDialog.dismiss();
				mDialog = null;
			}
		}
	}
	
	class KeywordSearchTask extends AsyncTask<String, Article, Void> {
		
		private Buzzdroid mActivity;
		
		OnJsonObjectAddListener listener = new OnJsonObjectAddListener() {
			@Override
			public void onAdd(Object obj) {
				if (obj instanceof Article) {
					Article article = (Article) obj;
					publishProgress(article);
				}
			}
		};
		
		public KeywordSearchTask(Buzzdroid acivity) {
			this.mActivity = acivity;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mAdapter.clear();
			
			mDialog = new ProgressDialog(mActivity);
			mDialog.setTitle("ブックマークを取得中...");
			mDialog.setMessage("ブックマークを取得しています");
			mDialog.setIndeterminate(true);
			mDialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			String keyword = params[0];
			String sUrl = RECENT_SEARCH_URL;
			String username = getUsername();
			
			if (!username.equals("")) {
				sUrl = sUrl.replace("{userId}", username);
			}
			
			if (!keyword.equals("")) {
				sUrl = sUrl.replace("{keyword}", keyword);
			}
			// TODO: check userID
			
			try {
				URL url = new URL(sUrl);
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				try {
					ArticleGen.getList(urlConnection.getInputStream(), listener);
				} finally {
					urlConnection.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JsonFormatException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Article... progress) {
			for (Article article : progress) {
				mAdapter.add(article.getTitle());
				mAdapter.notifyDataSetChanged();
				mArticles.add(article);
			}
			
			if (mDialog != null) {
				mDialog.dismiss();
				mDialog = null;
			}
		}
	}
}