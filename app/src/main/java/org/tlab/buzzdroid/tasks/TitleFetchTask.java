package org.tlab.buzzdroid.tasks;

import android.os.AsyncTask;
import android.widget.EditText;

import org.tlab.buzzdroid.HttpUtil;
import org.tlab.buzzdroid.activities.AddBookmarkActivity;

public class TitleFetchTask extends AsyncTask<String, String, String> {
    private AddBookmarkActivity mActivity;
    private EditText mEditTitle;

    public TitleFetchTask(AddBookmarkActivity activity, EditText editText) {
        this.mActivity = activity;
        this.mEditTitle = editText;
    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        String result = HttpUtil.getTitle(url);
        return result;
    }

    protected void onPostExecute(String result) {
        mEditTitle.setText(result);
        return;
    }

}
