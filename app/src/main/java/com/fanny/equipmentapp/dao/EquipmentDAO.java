package com.fanny.equipmentapp.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by Fanny on 17/6/1.
 */

public class EquipmentDAO {
    public String queryEquip(Context context,String number){
        String equipment="";

        String path=new File(context.getFilesDir(),"equipment.db").getAbsolutePath();

        SQLiteDatabase db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READWRITE);

        if(db!=null){
            //判断是否是我单位产品设备，匹配条件后续设置，此处先设为gh
            boolean isEquip=number.matches("GH");
            String substring=number.substring(0,2);
            String sub=number.substring(3,7);
            if(isEquip && substring.equals("GH")) {
                String sql="select ";
                String[] selectionArgs=new String[]{sub};
                Cursor cursor=db.rawQuery(sql,selectionArgs);
                if(cursor!=null){
                    //进行判断操作
                }
            }
        }


        return equipment;
    }
}
