package in.hng.mpos.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.LoyaltyDetails;

/**
 * Created by Cbly on 08-Mar-18.
 */

public class LoyaltyDetailsDB extends DBManager  {

    public static final String TABLE_NAME = "LoyaltyDetails";
    public static final String CUSTOMER_OFFER_ID = "CUSTOMER_OFFER_ID";
    public static final String OFFER_DESC = "OFFER_DESC";
    public static final String FROM_TIME = "FROM_TIME";
    public static final String TO_TIME = "TO_TIME";
    public static final String VALID_FROM = "VALID_FROM";
    public static final String VALID_TO = "VALID_TO";
    public static final String NAME = "NAME";
    public static final String OFFER_CODE = "OFFER_CODE";
    public static final String OFFER_ID = "OFFER_ID";
    public static final String OFFER_TYPE = "OFFER_TYPE";
    public static final String OFFER_VALID_DAYS = "OFFER_VALID_DAYS";
    public static final String OFFER_VALUE = "OFFER_VALUE";
    public static final String OFFER_VALUE_TYPE = "OFFER_VALUE_TYPE";
    public static final String OUTLET_NAME = "OUTLET_NAME";
    public static final String SKU_CODE = "SKU_CODE";
    public static final String PURCHASE_VAL1 = "PURCHASE_VAL1";
    public static final String PURCHASE_VAL2 = "PURCHASE_VAL2";
    public static final String STORE_NAME = "STORE_NAME";

    public LoyaltyDetailsDB(Context context) {
        super(context);

        sqliteDB = open();
        createLoyaltyDetailsTable();


    }
    public void createLoyaltyDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + CUSTOMER_OFFER_ID + " text,"
                + OFFER_DESC + " text,"
                + FROM_TIME + " text,"
                + TO_TIME + " text,"
                + VALID_FROM + " text,"
                + VALID_TO + " text,"
                + NAME + " text,"
                + OFFER_CODE + " text,"
                + OFFER_ID + " text,"
                + OFFER_TYPE + " text,"
                + OFFER_VALID_DAYS + " text,"
                + OFFER_VALUE + " text,"
                + OFFER_VALUE_TYPE + " text,"
                + OUTLET_NAME + " text,"
                + SKU_CODE + " text,"
                + PURCHASE_VAL1 + " text,"
                + PURCHASE_VAL2 + " text,"
                + STORE_NAME + " text);";

        sqliteDB.execSQL(CreateTable);
    }
    public void deleteLoyaltyTable()
    {
        String qry = "drop table if exists "+TABLE_NAME;
        sqliteDB.execSQL(qry);
    }

    public void insertBulkOfferDetails(JSONArray offerdata) {
        try {

            String sql = "INSERT INTO " + TABLE_NAME + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();

            for (int i = 0; i < offerdata.length(); i++) {
                JSONObject row = offerdata.getJSONObject(i);

                statement.clearBindings();

                statement.bindString(1, row.getString("CustomerOfferId").toString()); // sku name
                statement.bindString(2, row.getString("Description").toString()); // sku code
                statement.bindString(3, row.getString("FromTime")); // store SKU
                statement.bindString(4, row.getString("ToTime")); // SKU qty
                statement.bindString(5, row.getString("ValidFrom")); // mrp
                statement.bindString(6, row.getString("ValidTill")); // tax code
                statement.bindString(7, row.getString("Name").toString()); // tax rate
                statement.bindString(8, row.getString("OfferCode").toString()); // total mrp
                statement.bindString(9, row.getString("OfferId").toString()); // sell value
                statement.bindString(10, row.getString("OfferType").toString()); // disc MRP
                statement.bindString(11, row.getString("OfferValidDays").toString()); // unit disc value
                statement.bindString(12, row.getString("OfferValue").toString()); // unit sell value
                statement.bindString(13, row.getString("OfferValueType").toString()); // sell value
                statement.bindString(14, row.getString("OutletName").toString()); // sell value
                statement.bindString(15, row.getString("ProductCode").toString()); // disc MRP
                statement.bindString(16, row.getString("PurchaseValue1").toString()); // unit disc value
                statement.bindString(17, row.getString("PurchaseValue2").toString()); // unit sell value
                statement.bindString(18, row.getString("ProductName").toString()); // unit sell value
                statement.execute();

                //invoiceAmountEdt.setText(row.getString("location_code"));
            }
            sqliteDB.setTransactionSuccessful();



        }catch(Exception e){
            e.printStackTrace();
        }finally {
            sqliteDB.endTransaction();
        }

    }

    public ArrayList<LoyaltyDetails> getOfferDetails() {

        String qry = "SELECT * FROM "+TABLE_NAME;
        ArrayList<LoyaltyDetails> offerLists = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            offerLists = new ArrayList<LoyaltyDetails>();


            while (cursor.moveToNext()) {

                LoyaltyDetails offers = new LoyaltyDetails();
                offers.setCustomerOfferID(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_OFFER_ID)));
                offers.setOfferDESC(cursor.getString(cursor.getColumnIndexOrThrow(OFFER_DESC)));
                offers.setFromTime(cursor.getString(cursor.getColumnIndexOrThrow(FROM_TIME)));
                offers.setToTime(cursor.getString(cursor.getColumnIndexOrThrow(TO_TIME)));
                offers.setValidFrom(cursor.getString(cursor.getColumnIndex(VALID_FROM)));
                offers.setValidTo(cursor.getString(cursor.getColumnIndex(VALID_TO)));
                offers.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                offers.setOfferCode(cursor.getString(cursor.getColumnIndex(OFFER_CODE)));
                offers.setOfferID(cursor.getString(cursor.getColumnIndexOrThrow(OFFER_ID)));
                offers.setOfferType(cursor.getString(cursor.getColumnIndexOrThrow(OFFER_TYPE)));
                offers.setOfferValidDays(cursor.getString(cursor.getColumnIndex(OFFER_VALID_DAYS)));
                offers.setOfferValue(cursor.getString(cursor.getColumnIndex(OFFER_VALUE)));
                offers.setOfferValueType(cursor.getString(cursor.getColumnIndex(OFFER_VALUE_TYPE)));
                offers.setOutletName(cursor.getString(cursor.getColumnIndex(OUTLET_NAME)));
                offers.setSkuCode(cursor.getString(cursor.getColumnIndex(SKU_CODE)));
                offers.setPurchaseVal1(cursor.getString(cursor.getColumnIndex(PURCHASE_VAL1)));
                offers.setPurchaseVal2(cursor.getString(cursor.getColumnIndex(PURCHASE_VAL2)));
                offers.setStoreName(cursor.getString(cursor.getColumnIndex(STORE_NAME)));
                offerLists.add(offers);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return offerLists;

    }
    public ArrayList<LoyaltyDetails> checkOfferList(){

        //this.createSKUTable();
        String qry = "SELECT * FROM "+TABLE_NAME ;

        ArrayList<LoyaltyDetails> offerlist = null;
        //boolean flag = false ;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            offerlist = new ArrayList<LoyaltyDetails>();

            while (cursor.moveToNext()) {

                LoyaltyDetails offers = new LoyaltyDetails();
                offers.setCustomerOfferID(cursor.getString(cursor.getColumnIndexOrThrow("CUSTOMER_OFFER_ID")));
                offers.setOfferDESC(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_DESC")));
                offers.setFromTime(cursor.getString(cursor.getColumnIndexOrThrow("FROM_TIME")));
                offers.setToTime(cursor.getString(cursor.getColumnIndexOrThrow("TO_TIME")));
                offers.setValidFrom(cursor.getString(cursor.getColumnIndexOrThrow("VALID_FROM")));
                offers.setValidTo(cursor.getString(cursor.getColumnIndexOrThrow("VALID_TO")));
                offers.setName(cursor.getString(cursor.getColumnIndexOrThrow("NAME")));
                offers.setOfferCode(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_CODE")));
                offers.setOfferID(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_ID")));
                offers.setOfferType(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_TYPE"))); offers.setOfferDESC(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_DESC")));
                offers.setOfferValidDays(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_VALID_DAYS"))); offers.setOfferDESC(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_DESC")));
                offers.setOfferValue(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_VALUE"))); offers.setOfferDESC(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_DESC")));
                offers.setOfferValueType(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_VALUE_TYPE"))); offers.setOfferDESC(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_DESC")));
                offers.setOutletName(cursor.getString(cursor.getColumnIndexOrThrow("OUTLET_NAME"))); offers.setOfferDESC(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_DESC")));
                offers.setSkuCode(cursor.getString(cursor.getColumnIndexOrThrow("SKU_CODE"))); offers.setOfferDESC(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_DESC")));
                offers.setPurchaseVal1(cursor.getString(cursor.getColumnIndexOrThrow("PURCHASE_VAL1"))); offers.setOfferDESC(cursor.getString(cursor.getColumnIndexOrThrow("OFFER_DESC")));
                offers.setPurchaseVal2(cursor.getString(cursor.getColumnIndexOrThrow("PURCHASE_VAL2")));
                offers.setStoreName(cursor.getString(cursor.getColumnIndexOrThrow("STORE_NAME")));









                // Log.d("SKU NO --- ",cursor.getString(cursor.getColumnIndexOrThrow("sku_code"))+"==="+cursor.getString(cursor.getColumnIndexOrThrow(PO_NO)));
                offerlist.add(offers);

            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }

        return offerlist;

    }






}
