package org.tlab.buzzdroid;

import android.os.AsyncTask;
import android.widget.EditText;

public class TitleFetchTask extends AsyncTask<String, String, String>{
	
	private AddBookmark mActivity;
	private EditText mEditTitle;
	
	public TitleFetchTask(AddBookmark activity, EditText editText) {
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
