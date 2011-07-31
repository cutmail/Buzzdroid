package org.tlab.buzzdroid;

import me.cutmail.buzzurl.Article;

import org.apache.http.auth.UsernamePasswordCredentials;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class AddBookmarkTask extends AsyncTask<Article, String, Boolean>{

  private AddBookmark mActivity;
  private UsernamePasswordCredentials mCredentials;
  private ProgressDialog mDialog;

  public AddBookmarkTask(AddBookmark activity, UsernamePasswordCredentials credentials) {
    mActivity = activity;
    mCredentials = credentials;
  }

  @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mDialog = new ProgressDialog(mActivity);
      mDialog.setMessage(getString(R.string.message_saving));
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
        Toast.makeText(mActivity, getString(R.string.message_bookmark_successed), Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(mActivity, getString(R.string.message_bookmark_failed), Toast.LENGTH_SHORT).show();
      }
      mActivity.finish();
    }
}
