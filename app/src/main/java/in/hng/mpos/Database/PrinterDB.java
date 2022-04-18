package in.hng.mpos.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.gettersetter.PrinterDetails;
import in.hng.mpos.gettersetter.UserDetails;

public class PrinterDB extends DBManager  {
    public static final String TABLE_NAME = "PrinterDetails";
    public static final String PRINTER_ID = "PRINTER_ID";
    public static final String PRINTER_NAME = "PRINTER_NAME";

    public PrinterDB(Context context) {
        super(context);

        sqliteDB = open();
        createPrinterDetailsTable();


    }

    public void deletePrinterTable()
    {
        String qry = "drop table if exists "+TABLE_NAME;
        sqliteDB.execSQL(qry);
    }

    public void createPrinterDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + PRINTER_ID + " text,"
                + PRINTER_NAME + " text);";

        sqliteDB.execSQL(CreateTable);
    }

    public void insertPrinterDetails(HashMap<String,String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(PRINTER_ID, linedata.get("printerID"));
            values.put(PRINTER_NAME, linedata.get("printerName"));
            dbSql.insert(TABLE_NAME, null, values);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            //sqliteDB.endTransaction();
        }
    }

    public ArrayList<PrinterDetails> getPrinterDetails() {

        String qry = "SELECT * FROM "+TABLE_NAME;
        ArrayList<PrinterDetails> printerDetails = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            printerDetails = new ArrayList<PrinterDetails>();
            while (cursor.moveToNext()) {

                PrinterDetails printer = new PrinterDetails();
                printer.setPrinterID(cursor.getString(cursor.getColumnIndexOrThrow(PRINTER_ID)));
                printer.setPrinterName(cursor.getString(cursor.getColumnIndexOrThrow(PRINTER_NAME)));
                printerDetails.add(printer);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return printerDetails;

    }
}
