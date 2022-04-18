package in.hng.mpos.Database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBManager extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "HGBilling";
	public static final int DATABASE_VERSION = 1;
	public SQLiteDatabase sqliteDB;
	public static DBManager dbManager;
	Context context;


	//database manager constructor
	public DBManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context=context;
	}

	// this will establish a connection with database only once
	public static synchronized  DBManager getInstance(Context ctx) {
		if (dbManager == null) 
		{
			dbManager = new DBManager(ctx.getApplicationContext());
		}
		return dbManager;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("inside on create");


		//	sqliteDB.execSQL(VisitorDB.createTable());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public SQLiteDatabase open() throws SQLException {


		if (getInstance(context) == null) {

			dbManager= new DBManager(context);
		}
		if (sqliteDB == null) 
		{
			sqliteDB = getInstance(context).getWritableDatabase();
		}

		return sqliteDB;
	}
	
	
	
	
	
}