package in.hng.mpos.Database;

/**
 * Created by Cbly on 02-Mar-18.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import in.hng.mpos.gettersetter.ProductList;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ProductDetailsDB extends DBManager {

    private DBAdapter dba;
    public static final String TABLE_NAME = "ProductDetails";
    public static final String SKU_NAME = "SKU_NAME";
    public static final String SKU_CODE = "SKU_CODE";
    public static final String STORE_SKU = "STORE_SKU";
    public static final String QTY = "QTY";
    public static final String MRP = "MRP";
    public static final String TAX_CODE = "TAX_CODE";
    public static final String TAX_RATE = "TAX_RATE";
    public static final String TOTAL_AMT = "TOTAL_AMT";
    public static final String SELL_VALUE = "SELL_VALUE";
    public static final String DISC_MRP = "DISC_MRP";
    public static final String UNIT_DISC = "UNIT_DISC";
    public static final String UNIT_SELL = "UNIT_SELL";
    public static final String EAN_CODE = "EAN_CODE";

    public ProductDetailsDB(Context context) {
        super(context);

        sqliteDB = open();
        createProductDetailsTable();


    }

    public void createProductDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + SKU_NAME + " text,"
                + SKU_CODE + " text,"
                + STORE_SKU + " text,"
                + QTY + " text,"
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

    public void insertProductDetails(HashMap<String, String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(SKU_NAME, linedata.get("skuName"));
            values.put(SKU_CODE, linedata.get("skuCode"));
            values.put(STORE_SKU, linedata.get("storeSKU"));
            values.put(QTY, linedata.get("Qty"));
            values.put(MRP, linedata.get("Mrp"));
            values.put(TAX_CODE, linedata.get("taxCode"));
            values.put(TAX_RATE, linedata.get("taxRate"));
            values.put(TOTAL_AMT, linedata.get("Mrp"));
            values.put(EAN_CODE, linedata.get("eanCode"));
            values.put(SELL_VALUE, "0");
            values.put(DISC_MRP, "0");
            values.put(UNIT_DISC, "0");
            values.put(UNIT_SELL, "0");


            dbSql.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //sqliteDB.endTransaction();
        }
    }


    public ArrayList<ProductList> getEANDetails() {

        String qry = "SELECT * FROM " + TABLE_NAME;
        ArrayList<ProductList> productLists = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            productLists = new ArrayList<ProductList>();


            while (cursor.moveToNext()) {

                ProductList product = new ProductList();
                product.setSkuName(cursor.getString(cursor.getColumnIndexOrThrow(SKU_NAME)));
                product.setSkuCode(cursor.getString(cursor.getColumnIndexOrThrow(SKU_CODE)));
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


    public ArrayList<ProductList> checkProductList(String StoreSKU, String mrp) {

        //this.createSKUTable();
        String qry = "SELECT * FROM " + TABLE_NAME + " WHERE " + STORE_SKU + " = '" + StoreSKU + "' AND " + MRP + " = '" + mrp + "'";

        ArrayList<ProductList> productlist = null;
        //boolean flag = false ;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            productlist = new ArrayList<ProductList>();

            while (cursor.moveToNext()) {

                ProductList product = new ProductList();
                product.setSkuName(cursor.getString(cursor.getColumnIndexOrThrow("SKU_NAME")));
                product.setQty(cursor.getString(cursor.getColumnIndexOrThrow("QTY")));
                product.setMrp(cursor.getString(cursor.getColumnIndexOrThrow("MRP")));

                // Log.d("SKU NO --- ",cursor.getString(cursor.getColumnIndexOrThrow("sku_code"))+"==="+cursor.getString(cursor.getColumnIndexOrThrow(PO_NO)));
                productlist.add(product);

            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }

        return productlist;

    }

    public void updateProductList(HashMap<String, String> itemlist) {
        try {
            Integer Qty = 0;
            String query = "SELECT QTY FROM " + TABLE_NAME + " WHERE " + STORE_SKU + " = '" + itemlist.get("storeSKU").toString() + "' AND " + MRP + " = '" + itemlist.get("Mrp").toString() + "' ORDER BY QTY DESC LIMIT 1";
            Cursor c = sqliteDB.rawQuery(query, null);

            while (c.moveToNext()) {

                Qty = c.getInt(0) + 1;

            }
            c.close();
            Float mrp = Float.parseFloat(itemlist.get("Mrp").toString());
            Float totmrp = Qty * mrp;
            //Qty = Integer.parseInt(itemlist.get("Qty").toString()) + 1;
            String sql = "UPDATE " + TABLE_NAME + "  SET QTY = ? , TOTAL_AMT = ? WHERE STORE_SKU = ? AND MRP = ?;";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();

            statement.clearBindings();

            statement.bindString(1, Qty.toString());  // MRP
            statement.bindString(2, totmrp.toString());
            statement.bindString(3, itemlist.get("storeSKU"));
            statement.bindString(4, itemlist.get("Mrp"));// actual qty

            statement.execute();

            sqliteDB.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDB.endTransaction();
        }

    }

    public void updateProductQty(String storeSKU, String Mrp, String Qty) {
        try {

            Float mrp = Float.parseFloat(Mrp.toString());
            Float totmrp = Integer.parseInt(Qty) * mrp;
            String sql = "UPDATE " + TABLE_NAME + "  SET QTY = ?,DISC_MRP = ?,TOTAL_AMT = ? WHERE STORE_SKU = ? AND MRP = ?;";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();

            statement.clearBindings();

            statement.bindString(1, Qty);  // MRP
            statement.bindString(2, "");
            statement.bindString(3, totmrp.toString());
            statement.bindString(4, storeSKU);
            statement.bindString(5, Mrp);
            statement.execute();
            sqliteDB.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDB.endTransaction();
        }

    }

    public void deleteProduct(String storeSKU, String Mrp) {
        try {


            String sql = "DELETE FROM " + TABLE_NAME + " WHERE STORE_SKU = ? AND MRP = ?;";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();
            statement.clearBindings();
            statement.bindString(1, storeSKU);  // MRP
            statement.bindString(2, Mrp);
            statement.execute();
            sqliteDB.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDB.endTransaction();
        }

    }

    public void deleteProductManual(String skuCode, String Mrp) {
        try {


            String sql = "DELETE FROM " + TABLE_NAME + " WHERE SKU_CODE = ? AND MRP = ?;";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();
            statement.clearBindings();
            statement.bindString(1, skuCode);  // MRP
            statement.bindString(2, Mrp);
            statement.execute();
            sqliteDB.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDB.endTransaction();
        }

    }

    public void deleteUserTable() {
        String qry = "drop table if exists " + TABLE_NAME;
        sqliteDB.execSQL(qry);
    }

    public void insertBulkPOSKUDetails(JSONArray linedata) {
        try {

            String sql = "INSERT INTO " + TABLE_NAME + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();

            for (int i = 0; i < linedata.length(); i++) {
                JSONObject row = linedata.getJSONObject(i);

                statement.clearBindings();

                statement.bindString(1, row.getString("SKU_NAME").toString()); // sku name
                statement.bindString(2, row.getString("sku_code").toString()); // sku code
                statement.bindString(3, row.getString("store_sku_loc_stock_no")); // store SKU
                statement.bindString(4, row.getString("sku_qty")); // SKU qty
                statement.bindString(5, row.getString("mrp")); // mrp
                statement.bindString(6, row.getString("tax_code")); // tax code
                statement.bindString(7, row.getString("tax_rate").toString()); // tax rate
                statement.bindString(8, row.getString("Total_Amt").toString()); // total mrp
                statement.bindString(9, row.getString("Sell_Value").toString()); // sell value
                statement.bindString(10, row.getString("Discounted_Mrp").toString()); // disc MRP
                statement.bindString(11, row.getString("Unit_Discount_Value").toString()); // unit disc value
                statement.bindString(12, row.getString("Unit_Sell_Value").toString()); // unit sell value
                statement.bindString(13, row.getString("ean_code").toString());
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

    public void insertBulkManualSKUDetails(JSONArray linedata) {
        try {

            String sql = "INSERT INTO " + TABLE_NAME + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();

            for (int i = 0; i < linedata.length(); i++) {
                JSONObject row = linedata.getJSONObject(i);

                statement.clearBindings();

                statement.bindString(1, row.getString("skuName")); // sku name
                statement.bindString(2, row.getString("skuCode")); // sku code
                statement.bindString(3, row.getString("storeSkuLocStockNo")); // store SKU
                statement.bindString(4, row.getString("skuQty")); // SKU qty
                statement.bindString(5, row.getString("mrp")); // mrp
                statement.bindString(6, "0"); // tax code
                statement.bindString(7, "0"); // tax rate
                statement.bindString(8, row.getString("totalAmt")); // total mrp
                statement.bindString(9, row.getString("sellValue")); // sell value
                statement.bindString(10, "0"); // disc MRP
                statement.bindString(11, "0"); // unit disc value
                statement.bindString(12, row.getString("unitSellValue")); // unit sell value
                statement.bindString(13, row.getString("eanCode"));
                statement.execute();

            }
            sqliteDB.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDB.endTransaction();
        }
    }

    public ArrayList<ProductList> checkProductListManual(String skuCode, String mrp) {

        //this.createSKUTable();
        String qry = "SELECT * FROM " + TABLE_NAME + " WHERE " + SKU_CODE + " = '" + skuCode + "' AND " + MRP + " = '" + mrp + "'";

        ArrayList<ProductList> productlist = null;
        //boolean flag = false ;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            productlist = new ArrayList<ProductList>();

            while (cursor.moveToNext()) {

                ProductList product = new ProductList();
                product.setSkuName(cursor.getString(cursor.getColumnIndexOrThrow(SKU_NAME)));
                product.setQty(cursor.getString(cursor.getColumnIndexOrThrow(QTY)));
                product.setMrp(cursor.getString(cursor.getColumnIndexOrThrow(MRP)));

                // Log.d("SKU NO --- ",cursor.getString(cursor.getColumnIndexOrThrow("sku_code"))+"==="+cursor.getString(cursor.getColumnIndexOrThrow(PO_NO)));
                productlist.add(product);

            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }

        return productlist;

    }

}
