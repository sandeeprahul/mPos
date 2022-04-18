package in.hng.mpos.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.gettersetter.CustomerDetails;
import in.hng.mpos.gettersetter.UserDetails;

/**
 * Created by Cbly on 08-Mar-18.
 */

public class CustomerDB extends DBManager {
    public static final String TABLE_NAME = "CustomerDetails";
    public static final String CUSTOMER_ID = "CUSTOMER_ID";
    public static final String MOBILE_NO = "MOBILE_NO";
    public static final String NAME = "NAME";
    public static final String MAIL_ID = "MAIL_ID";
    public static final String GENDER = "GENDER";
    public static final String DOB = "DOB";
    public static final String ANNIVERSARY = "ANNIVERSARY";
    public static final String POINTS = "POINTS";
    public static final String TIER = "TIER";

    public CustomerDB(Context context) {
        super(context);
        sqliteDB = open();
        createCustomerDetailsTable();

    }
    public void createCustomerDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + CUSTOMER_ID + " text,"
                + MOBILE_NO + " text,"
                + NAME + " text,"
                + MAIL_ID + " text,"
                + GENDER + " text,"
                + DOB + " text,"
                + ANNIVERSARY + " text,"
                + POINTS + " text,"
                + TIER + " text);";

        sqliteDB.execSQL(CreateTable);
    }

    public void deleteCustomerTable() {
        String qry = "drop table if exists "+TABLE_NAME;
        sqliteDB.execSQL(qry);
    }

    public void insertCustomerDetails(HashMap<String,String> CustmoerData) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CUSTOMER_ID, CustmoerData.get("customerID"));
            values.put(MOBILE_NO, CustmoerData.get("mobileNO"));
            values.put(NAME, CustmoerData.get("name"));
            values.put(MAIL_ID, CustmoerData.get("mailID"));
            values.put(GENDER, CustmoerData.get("gender"));
            values.put(DOB, CustmoerData.get("dob"));
            values.put(ANNIVERSARY, CustmoerData.get("anniversary"));
            values.put(POINTS, CustmoerData.get("points"));
            values.put(TIER, CustmoerData.get("tier"));

            dbSql.insert(TABLE_NAME, null, values);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            //sqliteDB.endTransaction();
        }
    }

    public ArrayList<CustomerDetails> getCustomerDetails() {

        String qry = "SELECT * FROM "+TABLE_NAME;
        ArrayList<CustomerDetails> customerDetails = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            customerDetails = new ArrayList<CustomerDetails>();
            while (cursor.moveToNext()) {

                CustomerDetails customer = new CustomerDetails();
                customer.setCustomerID(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_ID)));
                customer.setMobileNO(cursor.getString(cursor.getColumnIndexOrThrow(MOBILE_NO)));
                customer.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow(NAME)));
                customer.setMailID(cursor.getString(cursor.getColumnIndexOrThrow(MAIL_ID)));
                customer.setGender(cursor.getString(cursor.getColumnIndexOrThrow(GENDER)));
                customer.setDob(cursor.getString(cursor.getColumnIndexOrThrow(DOB)));
                customer.setAnniversary(cursor.getString(cursor.getColumnIndexOrThrow(ANNIVERSARY)));
                customer.setPoints(cursor.getString(cursor.getColumnIndexOrThrow(POINTS)));
                customer.setTier(cursor.getString(cursor.getColumnIndexOrThrow(TIER)));
                customerDetails.add(customer);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return customerDetails;

    }

}
