package ru.kolesnikovdmitry.lesson15database.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class HotelDbHelper (context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    /**
     * Вызывается при создании базы данных
     */
    override fun onCreate(db: SQLiteDatabase) {
        // Строка для создания таблицы
       val sqlCreateGuestTable = " CREATE TABLE " + HotelContract.GuestsTable.TABLE_NAME + " ( " +
               HotelContract.GuestsTable._ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
               HotelContract.GuestsTable.COLUMN_AGE    + " INTEGER NOT NULL DEFAULT 0, " +
               HotelContract.GuestsTable.COLUMN_GENDER + " TEXT DEFAULT \"NS\", " +
               HotelContract.GuestsTable.COLUMN_NAME   + " TEXT DEFAULT \"NO NAME\", " +
               HotelContract.GuestsTable.COLUMN_IS_FREE + " INTEGER NOT NULL DEFAULT 1 ); "

        //ЗАПУСКАЕМ СОЗДАНИЕ БД
        db.execSQL(sqlCreateGuestTable)
    }

    /**
     * Вызывается при обновлении схемы базы данных
     */
    override fun onUpgrade(db: SQLiteDatabase,  oldVersion: Int, newVersion: Int) {
        val sqlTextDelete = "DROP TABLE IF EXISTS " + HotelContract.GuestsTable.TABLE_NAME  + ";"
        db.execSQL(sqlTextDelete)
        onCreate(db)
    }

    //Чтобы сделать публичные переменный класса, их нужно объявлять в этом блоке:
    companion object {
        //val LOG_TAG = HotelDbHelper::class.java.simpleName

        /**
        * Имя файла базы данных
        */
        private const val DATABASE_NAME = "hotel.db"

        /**
        * Версия базы данных. При изменении схемы увеличить на единицу
        */
        private const val DATABASE_VERSION = 6
    }
}