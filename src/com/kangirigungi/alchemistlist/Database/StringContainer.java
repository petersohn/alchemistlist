package com.kangirigungi.alchemistlist.Database;

import android.database.Cursor;
import android.database.SQLException;

public interface StringContainer {
	public String getString(long id);
	public Cursor searchString(String match, boolean exact) throws SQLException;
	public long addString(String value) throws SQLException;
}
