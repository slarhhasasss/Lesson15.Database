package ru.kolesnikovdmitry.lesson15database.data

import android.provider.BaseColumns

class HotelContract {

    class GuestsTable : BaseColumns{
        //Публичные переменные класса объявлять здесь!
        companion object {
            const val TABLE_NAME = "guests"

            const val _ID     = BaseColumns._ID
            const val COLUMN_NAME    = "name"
            const val COLUMN_AGE     = "age"
            const val COLUMN_GENDER  = "gender"
            const val COLUMN_IS_FREE = "is_free"

            const val GENDER_MALE   = 0
            const val GENDER_FEMALE = 1
            const val GENDER_TRANS  = 2
            const val GENDER_NS     = 3

            const val GENDER_MALE_S   = "Male"
            const val GENDER_FEMALE_S = "Female"
            const val GENDER_TRANS_S  = "Trans"
            const val GENDER_NS_S     = "NS"
        }
    }
}