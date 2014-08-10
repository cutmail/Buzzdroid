package org.tlab.buzzdroid.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.tlab.buzzdroid.BuzzurlApi;
import org.tlab.buzzdroid.R;
import org.tlab.buzzdroid.activities.AddBookmarkActivity;
import org.tlab.buzzdroid.models.Article;

public class AddBookmarkTask extends AsyncTask<Article, String, Boolean> {

    private AddBookmarkActivity mActivity;
    private UsernamePasswordCredentials mCredentials;
    private ProgressDialog mDialog;

    public AddBookmarkTask(AddBookmarkActivity activity, UsernamePasswordCredentials credentials) {
        mActivity = activity;
        mCredentials = credentials;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(mActivity.getString(R.string.message_saving));
        mDialog.setIndeterminate(true);
        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Article... articles) {
        Boolean result = false;
        BuzzurlApi api = new BuzzurlApi(mCredentials);
        Article article = articles[0];

        result = api.createPosts(article);
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        if (result == true) {
            Toast.makeText(mActivity, mActivity.getString(R.string.message_bookmark_successed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mActivity, mActivity.getString(R.string.message_bookmark_failed), Toast.LENGTH_SHORT).show();
        }
        mActivity.finish();
    }
}
