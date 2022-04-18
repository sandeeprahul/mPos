package in.hng.mpos.Database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;


public class UrlDB extends DBManager{

    public static final String TABLE_NAME = "UrlDetails";
    public static final String SERVER_URL = "SERVER_URL";
    public static final String IP_URL = "IP_URL";

    public UrlDB(Context context) {
        super(context);
        sqliteDB = open();
        createUrlDetailsTable();


    }
    public void createUrlDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + SERVER_URL + " text,"+ IP_URL + " text);";

        sqliteDB.execSQL(CreateTable);
    }

    public void deleteUrlTable() {
        String qry = "drop table if exists "+TABLE_NAME;
        sqliteDB.execSQL(qry);
        createUrlDetailsTable();
    }

    public void insertServerUrl(String Url,String ipUrl) {
        try {

            String qry = "INSERT INTO "+TABLE_NAME + " VALUES ('" + Url + "','"+ ipUrl +"')";
            sqliteDB.execSQL(qry);

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            //sqliteDB.endTransaction();
        }
    }

    public String getUrlDetails() {

        String qry = "SELECT * FROM "+TABLE_NAME;
        String server_url = "";
        try {
            Cursor cursor = sqliteDB.rawQuery(qry, null);
            while (cursor.moveToNext()) {
                server_url =cursor.getString(cursor.getColumnIndexOrThrow(SERVER_URL));
            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return server_url;

    }
    public String getIpUrlDetails() {

        String qry = "SELECT * FROM "+TABLE_NAME;
        String server_url = "";
        try {
            Cursor cursor = sqliteDB.rawQuery(qry, null);
            while (cursor.moveToNext()) {
                server_url =cursor.getString(cursor.getColumnIndexOrThrow(IP_URL));
            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return server_url;

    }
}
