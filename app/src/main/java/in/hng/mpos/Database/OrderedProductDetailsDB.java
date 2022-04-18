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
import java.util.HashMap;

import in.hng.mpos.gettersetter.LoyaltyCredentials;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.SearchProduct;

public class OrderedProductDetailsDB extends DBManager {

    private DBAdapter dba;
    public static final String TABLE_NAME = "OrderedProductDetails";
    public static final String ORDER_ID = "ORDER_ID";
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


    public OrderedProductDetailsDB(Context context) {
        super(context);

        sqliteDB = open();
        createOrderedProductDetailsTable();


    }

    public void createOrderedProductDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + ORDER_ID + " text,"
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

    public void insertOrderedProductDetails(HashMap<String, String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(SKU_NAME, linedata.get("skuName"));
            values.put(SKU_CODE, linedata.get("skuCode"));
            values.put(QTY, linedata.get("Qty"));


            dbSql.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //sqliteDB.endTransaction();
        }
    }

    public void insertItem(String orderID, HashMap<String, String> linedata, Boolean isFirst) {
        String Order_qty = "";
        try {
            if (isFirst) {

                String query = "SELECT ORDER_QTY FROM " + TABLE_NAME + " WHERE " + SKU_CODE + " = '" + linedata.get("skuCode") + "' AND " + ORDER_ID + "='" + orderID + "'";
                Cursor c = sqliteDB.rawQuery(query, null);
                while (c.moveToNext()) {
                    Order_qty = c.getString(0);
                }
                c.close();
                deleteProduct(orderID,linedata.get("skuCode"), "", "");
            }
            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ORDER_ID, orderID);
            values.put(SKU_NAME, linedata.get("skuName"));
            values.put(SKU_CODE, linedata.get("skuCode"));
            values.put(STORE_SKU, linedata.get("storeSKU"));
            values.put(QTY, linedata.get("Qty"));
            values.put(ORDER_QTY, Order_qty);
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


    public ArrayList<SearchProduct> getEANDetails() {

        String qry = "SELECT * FROM " + TABLE_NAME;
        ArrayList<SearchProduct> productLists = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            productLists = new ArrayList<SearchProduct>();


            while (cursor.moveToNext()) {

                SearchProduct product = new SearchProduct();
                product.setSkuName(cursor.getString(cursor.getColumnIndexOrThrow(SKU_NAME)));
                product.setSkuCode(cursor.getString(cursor.getColumnIndexOrThrow(SKU_CODE)));
                product.setQty(cursor.getString(cursor.getColumnIndexOrThrow(QTY)));
                productLists.add(product);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return productLists;

    }

    public ArrayList<ProductList> getAllEANDetails(String orderID) {

        String qry = "SELECT * FROM " + TABLE_NAME + " WHERE " + ORDER_ID + "='" + orderID + "'";
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
                product.setOrderQty(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_QTY)));
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


    public ArrayList<SearchProduct> checkProductList(String SKUcode) {

        //this.createSKUTable();
        String qry = "SELECT * FROM " + TABLE_NAME + " WHERE " + SKU_CODE + " = '" + SKUcode + "'";

        ArrayList<SearchProduct> productlist = null;
        //boolean flag = false ;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            productlist = new ArrayList<SearchProduct>();

            while (cursor.moveToNext()) {

                SearchProduct product = new SearchProduct();
                product.setSkuName(cursor.getString(cursor.getColumnIndexOrThrow("SKU_NAME")));
                product.setQty(cursor.getString(cursor.getColumnIndexOrThrow("QTY")));

                // Log.d("SKU NO --- ",cursor.getString(cursor.getColumnIndexOrThrow("sku_code"))+"==="+cursor.getString(cursor.getColumnIndexOrThrow(PO_NO)));
                productlist.add(product);

            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }

        return productlist;

    }

    public void updateProductList(String orderID, HashMap<String, String> itemlist) {
        try {
            Integer Qty = 0;
            String query;

            if (!orderID.equalsIgnoreCase(""))

                query = "SELECT QTY FROM " + TABLE_NAME + " WHERE " + SKU_CODE + " = '" + itemlist.get("skuCode").toString() + "' AND " + ORDER_ID + "='" + orderID + "' ORDER BY QTY DESC LIMIT 1";
            else
                query = "SELECT QTY FROM " + TABLE_NAME + " WHERE " + SKU_CODE + " = '" + itemlist.get("skuCode").toString() + "' ORDER BY QTY DESC LIMIT 1";

            Cursor c = sqliteDB.rawQuery(query, null);
            while (c.moveToNext()) {

                Qty = c.getInt(0) + 1;

            }
            c.close();
            String sql = "UPDATE " + TABLE_NAME + "  SET QTY = ?  WHERE SKU_CODE = ? ";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();

            statement.clearBindings();
            statement.bindString(1, Qty.toString());  // MRP
            statement.bindString(2, itemlist.get("skuCode"));
            statement.execute();

            sqliteDB.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDB.endTransaction();
        }

    }


    public void updateProductListQty(String orderID, HashMap<String, String> itemlist) {
        try {
            Integer Qty = 0;
            String query;

            if (!orderID.equalsIgnoreCase(""))

                query = "SELECT QTY FROM " + TABLE_NAME + " WHERE " + SKU_CODE + " = '" + itemlist.get("skuCode").toString() + "' AND " + ORDER_ID + "='" + orderID + "' ORDER BY QTY DESC LIMIT 1";
            else
                query = "SELECT QTY FROM " + TABLE_NAME + " WHERE " + SKU_CODE + " = '" + itemlist.get("skuCode").toString() + "' ORDER BY QTY DESC LIMIT 1";

            Cursor c = sqliteDB.rawQuery(query, null);
            while (c.moveToNext()) {

                Qty = c.getInt(0) + 1;

            }
            c.close();

            Float mrp = Float.parseFloat(itemlist.get("Mrp").toString());
            Float totmrp = Qty * mrp;

            String sql = "UPDATE " + TABLE_NAME + "  SET QTY = ?,DISC_MRP = ?,TOTAL_AMT = ? WHERE STORE_SKU = ? AND MRP = ?;";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();

            statement.clearBindings();

            statement.bindString(1, Qty.toString());  // MRP
            statement.bindString(2, "");
            statement.bindString(3, totmrp.toString());
            statement.bindString(4, itemlist.get("storeSKU").toString());
            statement.bindString(5, itemlist.get("Mrp").toString());
            statement.execute();
            sqliteDB.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDB.endTransaction();
        }

    }


    public void updateProductQty(String SKUcode, String Qty) {
        try {


            String sql = "UPDATE " + TABLE_NAME + "  SET QTY = ? WHERE SKU_CODE = ? ;";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();
            statement.clearBindings();
            statement.bindString(1, Qty);  // MRP
            statement.bindString(2, SKUcode);
            statement.execute();
            sqliteDB.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDB.endTransaction();
        }

    }

    public void updateItemQty(String storeSKU, String Mrp, String Qty) {
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

    public boolean isFirstEntry(String orderID,String SKUcode) {
        boolean isFirst = true;
        try {

            float MRP = 0.0F;
            String query = "SELECT MRP FROM " + TABLE_NAME + " WHERE " + SKU_CODE + " = '" + SKUcode  + "' AND " + ORDER_ID + "='" + orderID + "'";
            Cursor c = sqliteDB.rawQuery(query, null);
            while (c.moveToNext()) {
                MRP = c.getFloat(0);
            }
            c.close();
            if (MRP == 0.00)
                isFirst = true;
            else
                isFirst = false;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return isFirst;
    }

    public boolean isProductExist(String strSKU) {
        boolean isExist = true;
        try {

            String skuCode = "";
            String query = "SELECT SKU_CODE FROM " + TABLE_NAME + " WHERE " + STORE_SKU + " = '" + strSKU + "'";
            Cursor c = sqliteDB.rawQuery(query, null);
            while (c.moveToNext()) {
                skuCode = c.getString(0);
            }
            c.close();
            if (skuCode.equalsIgnoreCase(""))
                isExist = false;
            else
                isExist = true;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExist;
    }

    public boolean isProductPending(String SKUcode) {
        boolean ispending = true;
        try {


            String skuCode = "";
            String query = "SELECT SKU_CODE FROM ( SELECT SUM(QTY) AS scannedQty, SUM(ORDER_QTY) AS orderQty, SKU_CODE FROM "
                    + TABLE_NAME + " WHERE " + SKU_CODE + " = '" + SKUcode + "'" + " GROUP BY " + SKU_CODE + ") WHERE orderQty > scannedQTY ";
            Cursor c = sqliteDB.rawQuery(query, null);
            while (c.moveToNext()) {
                skuCode = c.getString(0);
            }
            c.close();
            if (skuCode.equalsIgnoreCase(""))
                ispending = false;
            else
                ispending = true;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return ispending;
    }

    public void deleteProduct(String orderID,String skuCode, String storeSKU, String Mrp) {
        try {

            if (!skuCode.equalsIgnoreCase("")) {
                String sql = "DELETE FROM " + TABLE_NAME + " WHERE SKU_CODE = '" + skuCode  + "'";
                sqliteDB.execSQL(sql);
            } else {
                String sql = "DELETE FROM " + TABLE_NAME + " WHERE STORE_SKU = '" + storeSKU + "' AND MRP = '" + Mrp + "' AND " + ORDER_ID + "='" + orderID + "'";
                sqliteDB.execSQL(sql);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deleteOrder(String orderID) {
        try {


            String sql = "DELETE FROM " + TABLE_NAME + " WHERE ORDER_ID = '" + orderID + "'";
            sqliteDB.execSQL(sql);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deleteProductsTable() {
        String qry = "drop table if exists " + TABLE_NAME;
        sqliteDB.execSQL(qry);
    }

    public void insertBulkPOSKUDetails(String orderID, JSONArray linedata) {
        try {

            String sql = "INSERT INTO " + TABLE_NAME + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement = sqliteDB.compileStatement(sql);
            sqliteDB.beginTransaction();

            for (int i = 0; i < linedata.length(); i++) {
                JSONObject row = linedata.getJSONObject(i);

                statement.clearBindings();
                statement.bindString(1, orderID); // sku name
                statement.bindString(2, row.getString("sku_name")); // sku name
                statement.bindString(3, row.getString("sku_code")); // sku code
                statement.bindString(4, row.getString("store_sku_loc_stock_no")); // store SKU
                statement.bindString(5, "");
                statement.bindString(6, row.getString("sku_qty"));// SKU qty
                statement.bindString(7, row.getString("mrp")); // mrp
                statement.bindString(8, row.getString("tax_code")); // tax code
                statement.bindString(9, row.getString("tax_rate").toString()); // tax rate
                statement.bindString(10, row.getString("total_amt").toString()); // total mrp
                statement.bindString(11, row.getString("sell_value").toString()); // sell value
                statement.bindString(12, row.getString("discounted_mrp").toString()); // disc MRP
                statement.bindString(13, row.getString("unit_discount_value").toString()); // unit disc value
                statement.bindString(14, row.getString("unit_sell_value").toString()); // unit sell value
                statement.bindString(15, row.getString("ean_code").toString());
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
}
