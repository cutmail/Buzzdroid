package me.cutmail.buzzurl;

import net.vvakame.util.jsonpullparser.annotation.JsonKey;
import net.vvakame.util.jsonpullparser.annotation.JsonModel;
import net.vvakame.util.jsonpullparser.util.JsonArray;
import net.vvakame.util.jsonpullparser.util.JsonHash;

@JsonModel(decamelize = true)
public class Article {
//	@JsonKey
	private int rowid;

	@JsonKey
	private String url;
	
	@JsonKey
	private String title;
	
	@JsonKey
	private String comment;
	
	@JsonKey
	private JsonArray keywords;
	
	@JsonKey
	private int user_num;
	
	private boolean access;
	
	@JsonKey
	private String created_at;
	
	@JsonKey
	private String modified_at;

	public int getRowid() {
		return rowid;
	}

	public void setRowid(int rowid) {
		this.rowid = rowid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public JsonArray getKeywords() {
		return keywords;
	}

	public void setKeywords(JsonArray keywords) {
		this.keywords = keywords;
	}

	public int getUser_num() {
		return user_num;
	}

	public void setUser_num(int user_num) {
		this.user_num = user_num;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getModified_at() {
		return modified_at;
	}

	public void setModified_at(String modified_at) {
		this.modified_at = modified_at;
	}

	public boolean isAccess() {
		return access;
	}

	public void setAccess(boolean access) {
		this.access = access;
	}
}
