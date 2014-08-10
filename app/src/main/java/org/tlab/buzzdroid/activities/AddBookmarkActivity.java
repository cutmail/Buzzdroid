package org.tlab.buzzdroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.tlab.buzzdroid.tasks.AddBookmarkTask;
import org.tlab.buzzdroid.R;
import org.tlab.buzzdroid.tasks.TitleFetchTask;
import org.tlab.buzzdroid.models.Article;

public class AddBookmarkActivity extends Activity implements OnClickListener {
    private static final String TAG = "AddBookmark";

    private EditText mEditUrl;
    private EditText mEditTitle;
    private EditText mEditNotes;
    private CheckBox mCheckAccess;

    private Button mButtonSave;
    private Button mButtonCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bookmark);

        regularStartup(savedInstanceState);
    }

    private void regularStartup(Bundle savedInstanceState) {
        mEditUrl = (EditText) findViewById(R.id.edit_url);
        mEditTitle = (EditText) findViewById(R.id.edit_title);
        mEditNotes = (EditText) findViewById(R.id.edit_notes);
        mCheckAccess = (CheckBox) findViewById(R.id.access);

        mButtonSave = (Button) findViewById(R.id.button_save);
        mButtonCancel = (Button) findViewById(R.id.button_cancel);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();
            String type = intent.getType();
            if (Intent.ACTION_INSERT.equals(action)
                    && Browser.BOOKMARKS_URI.equals(data)) {
                String url = intent.getStringExtra("url");
                String title = intent.getStringExtra("title");
                mEditUrl.setText(url);
                mEditTitle.setText(title);

                TitleFetchTask task = new TitleFetchTask(this, mEditTitle);
                task.execute(url);
            } else if (Intent.ACTION_SEND.equals(action)
                    && "text/plain".equals(type)) {
                String url = intent.getStringExtra(Intent.EXTRA_TEXT);
                mEditUrl.setText(url);
                String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                mEditTitle.setText(title);
            } else {
                mEditUrl.setText("http://");
                mEditTitle.setText("");
            }
        } else {

        }

        mButtonSave.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);
    }

    private static boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    private void saveBookmark() {
        boolean error = false;

        if (isEmpty(mEditUrl) || mEditUrl.getText().toString().equals("http://")) {
            mEditUrl.requestFocus();
            mEditUrl.setError(getString(R.string.error_url_empty));
            error = true;
        } else {
            mEditUrl.setError(null);
            error = false;
        }

        if (isEmpty(mEditTitle)) {
            mEditTitle.requestFocus();
            mEditTitle.setError(getString(R.string.error_title_empty));
            error = true;
        } else {
            mEditTitle.setError(null);
            error = false;
        }

        if (error == true) {
            return;
        }

        // TODO: check Login
        if (!checkLogin()) {
            Toast.makeText(this, getString(R.string.alert_login), Toast.LENGTH_SHORT).show();
            showSettings();
            return;
        }

        Article article = new Article();

        article.setUrl(mEditUrl.getText().toString());
        article.setTitle(mEditTitle.getText().toString());
        article.setComment(mEditNotes.getText().toString());
        article.setAccess(mCheckAccess.isChecked());

        UsernamePasswordCredentials cred = new UsernamePasswordCredentials(getMail(), getPassword());

        AddBookmarkTask task = new AddBookmarkTask(this, cred);
        task.execute(article);
    }

    private boolean checkLogin() {
        String mail = getMail();
        String pass = getPassword();

        if (mail.equals("") || pass.equals("")) {
            return false;
        }
        return true;
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        if (v == mButtonSave) {
            saveBookmark();
        } else if (v == mButtonCancel) {
            finish();
        }
    }

    private String getMail() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString("mail", "");
    }

    private String getPassword() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString("password", "");
    }
}
