package in.hng.mpos.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.gettersetter.CardDetails;
import in.hng.mpos.gettersetter.PrinterDetails;

public class CardDB extends DBManager  {
    public static final String TABLE_NAME = "CardDetails";
    public static final String EDC_ID = "EDC_ID";
    public static final String EDC_NAME = "EDC_NAME";

    public CardDB(Context context) {
        super(context);

        sqliteDB = open();
        createEDCDetailsTable();


    }

    public void deletePrinterTable()
    {
        String qry = "drop table if exists "+TABLE_NAME;
        sqliteDB.execSQL(qry);
    }

    public void createEDCDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + EDC_ID + " text,"
                + EDC_NAME + " text);";

        sqliteDB.execSQL(CreateTable);
    }

    public void insertEDCDetails(HashMap<String,String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(EDC_ID, linedata.get("edcID"));
            values.put(EDC_NAME, linedata.get("edcName"));
            dbSql.insert(TABLE_NAME, null, values);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            //sqliteDB.endTransaction();
        }
    }

    public ArrayList<CardDetails> getEDCdetails() {

        String qry = "SELECT * FROM "+TABLE_NAME;
        ArrayList<CardDetails> edcDetails = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            edcDetails = new ArrayList<CardDetails>();
            while (cursor.moveToNext()) {

                CardDetails EDC = new CardDetails();
                EDC.setEdcID(cursor.getString(cursor.getColumnIndexOrThrow(EDC_ID)));
                EDC.setEdcName(cursor.getString(cursor.getColumnIndexOrThrow(EDC_NAME)));
                edcDetails.add(EDC);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return edcDetails;

    }
}
