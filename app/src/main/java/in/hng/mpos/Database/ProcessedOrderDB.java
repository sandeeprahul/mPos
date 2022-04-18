package in.hng.mpos.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import in.hng.mpos.gettersetter.ProductList;

public class ProcessedOrderDB extends DBManager {

    private DBAdapter dba;
    public static final String TABLE_NAME = "ProcessedOrderDetail";
    public static final String ORDER_ID = "ORDER_ID";
    public static final String BILL_NO = "BILL_NO";
    public static final String SKU_NAME = "SKU_NAME";
    public static final String SKU_CODE = "SKU_CODE";
    public static final String QTY = "QTY";
    public static final String ORDER_QTY = "ORDER_QTY";
    public static final String STORE_SKU = "STORE_SKU";
    public static final String MRP = "MRP";
    public static final String TAX_CODE = "TAX_CODE";
    public static final String TAX_RATE = "TAX_RATE";
    public static final String TOTAL_AMT = "TOTAL_AMT";
    public static final String SELL_VALUE = "SELL_VALUE";
    public static final String DISC_MRP = "DISC_MRP";
    public static final String UNIT_DISC = "UNIT_DISC";
    public static final String UNIT_SELL = "UNIT_SELL";
    public static final String EAN_CODE = "EAN_CODE";


    public ProcessedOrderDB(Context context) {
        super(context);

        sqliteDB = open();
        createProcessedOrderTable();


    }

    public void createProcessedOrderTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + ORDER_ID + " text,"
                + BILL_NO + " text,"
                + SKU_NAME + " text,"
                + SKU_CODE + " text,"
                + STORE_SKU + " text,"
                + QTY + " text,"
                + ORDER_QTY + " text,"
                + MRP + " text,"
                + TAX_CODE + " text,"
                + TAX_RATE + " text,"
                + TOTAL_AMT + " text,"
                + SELL_VALUE + " text,"
                + DISC_MRP + " text,"
                + UNIT_DISC + " text,"
                + UNIT_SELL + " text,"
                + EAN_CODE + " text);";


        sqliteDB.execSQL(CreateTable);
    }

    public void insertProcessedOrderDetails(JSONArray linedata) {
        try {

            String sql = "INSERT INTO " + TABLE_NAME + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();

            for (int i = 0; i < linedata.length(); i++) {
                JSONObject row = linedata.getJSONObject(i);

                statement.clearBindings();
                statement.bindString(1, row.getString("SKU_NAME")); // sku name
                statement.bindString(2, row.getString("sku_code")); // sku code
                statement.bindString(3, row.getString("SKU_NAME")); // sku name
                statement.bindString(4, row.getString("sku_code")); // sku code
                statement.bindString(5, row.getString("store_sku_loc_stock_no")); // store SKU
                statement.bindString(6,  row.getString("sku_qty"));// SKU qty
                statement.bindString(7, "");
                statement.bindString(8, row.getString("mrp")); // mrp
                statement.bindString(9, row.getString("tax_code")); // tax code
                statement.bindString(10, row.getString("tax_rate")); // tax rate
                statement.bindString(11, row.getString("Total_Amt")); // total mrp
                statement.bindString(12, row.getString("Sell_Value")); // sell value
                statement.bindString(13, row.getString("Discounted_Mrp")); // disc MRP
                statement.bindString(14, row.getString("Unit_Discount_Value")); // unit disc value
                statement.bindString(15, row.getString("Unit_Sell_Value")); // unit sell value
                statement.bindString(16, row.getString("ean_code"));
                statement.execute();

                //invoiceAmountEdt.setText(row.getString("location_code"));
            }
            sqliteDB.setTransactionSuccessful();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDB.endTransaction();
        }

    }

    public ArrayList<ProductList> getAllProductsDetails() {

        String qry = "SELECT * FROM " + TABLE_NAME;
        ArrayList<ProductList> productLists = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            productLists = new ArrayList<ProductList>();


            while (cursor.moveToNext()) {

                ProductList product = new ProductList();

                product.setOrderID(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_ID)));
                product.setBillNo(cursor.getString(cursor.getColumnIndexOrThrow(BILL_NO)));
                product.setSkuCode(cursor.getString(cursor.getColumnIndexOrThrow(SKU_CODE)));
                product.setSkuName(cursor.getString(cursor.getColumnIndexOrThrow(SKU_NAME)));
                product.setStoreSKU(cursor.getString(cursor.getColumnIndexOrThrow(STORE_SKU)));
                product.setQty(cursor.getString(cursor.getColumnIndexOrThrow(QTY)));
                product.setMrp(cursor.getString(cursor.getColumnIndex(MRP)));
                product.setTaxCode(cursor.getString(cursor.getColumnIndex(TAX_CODE)));
                product.setTaxRate(cursor.getString(cursor.getColumnIndex(TAX_RATE)));
                product.setTotalAmt(cursor.getString(cursor.getColumnIndexOrThrow(TOTAL_AMT)));
                product.setSellValue(cursor.getString(cursor.getColumnIndexOrThrow(SELL_VALUE)));
                product.setDiscMRP(cursor.getString(cursor.getColumnIndex(DISC_MRP)));
                product.setUnitDisc(cursor.getString(cursor.getColumnIndex(UNIT_DISC)));
                product.setUnitSell(cursor.getString(cursor.getColumnIndex(UNIT_SELL)));
                product.setEanCode(cursor.getString(cursor.getColumnIndex(EAN_CODE)));


                productLists.add(product);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return productLists;

    }

    public void deleteProductsTable() {
        String qry = "drop table if exists " + TABLE_NAME;
        sqliteDB.execSQL(qry);
    }
}
