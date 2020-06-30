package ru.kolesnikovdmitry.lesson15database

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ru.kolesnikovdmitry.lesson15database.data.HotelContract
import ru.kolesnikovdmitry.lesson15database.data.HotelDbHelper

class AddActivity: AppCompatActivity() {

    private lateinit var mBtnCancelName : ImageButton
    private lateinit var mEditTextName  : EditText
    private lateinit var mEditTextAge   : EditText
    private lateinit var mBtnCancelAge  : ImageButton
    private lateinit var mBtnConfirm    : Button
    private lateinit var mDbHelper      : HotelDbHelper
    private lateinit var mSpinnerGender : Spinner

    private var mGender = 3                                                                         //0 - male, 1- female, 2- Trans, 3 - NS

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_layout)

        mEditTextName  = findViewById(R.id.editTextNameActAdd)
        mBtnCancelName = findViewById(R.id.imageButtonNameActAdd)
        mBtnCancelAge  = findViewById(R.id.imageButtonAgeActAdd)
        mEditTextAge   = findViewById(R.id.editTextAgeActAdd)
        mBtnConfirm    = findViewById(R.id.btnConfirmActAdd)
        mSpinnerGender = findViewById(R.id.spinnerAddAct)

        mDbHelper   = HotelDbHelper(this)

        try{
            setUpSpinner()
        } catch (ex : Throwable) {
            Toast.makeText(applicationContext, ex.message, Toast.LENGTH_LONG).show()
        }

        mBtnCancelName.setOnClickListener {
            mEditTextName.setText("")
        }

        mBtnCancelAge.setOnClickListener {
            mEditTextAge.setText("")
        }

        mBtnConfirm.setOnClickListener {                                                    //здесь по нажатию кнопки мы будем искать первую свободную строку, и в нее записывать
            val curName : String = mEditTextName.text.toString()                           //для этого с помощью курсора пробегаемся в поисках свободной строки, если нет, то добавляем в конце новую
            val curAge  : String = mEditTextAge.text.toString()

            //Если возраст не указан
            if (curAge == "") {
                Snackbar.make(mBtnConfirm, "Please, enter Your age!", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //если имя
            if (curName == "") {
                Snackbar.make(mBtnConfirm, "Please, enter Your name!", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val projections = arrayOf(HotelContract.GuestsTable._ID,
                                                   HotelContract.GuestsTable.COLUMN_IS_FREE)

            val db : SQLiteDatabase = mDbHelper.writableDatabase

            val cursor : Cursor = db.query(
                HotelContract.GuestsTable.TABLE_NAME,
                projections,
                null,
                null,
                null,
                null,
                null)

            //данные для вставки
            val values = ContentValues()
            values.put(HotelContract.GuestsTable.COLUMN_GENDER, getGenderName())
            values.put(HotelContract.GuestsTable.COLUMN_AGE, curAge)
            values.put(HotelContract.GuestsTable.COLUMN_NAME, curName)
            values.put(HotelContract.GuestsTable.COLUMN_IS_FREE, 0)

            try {
                //объявляем переменный, который хранят индекс колонки
                val idColIndex     = cursor.getColumnIndex(HotelContract.GuestsTable._ID)
                val isFreeColIndex = cursor.getColumnIndex(HotelContract.GuestsTable.COLUMN_IS_FREE)

                while (cursor.moveToNext()) {
                    //берем данные из этих колонок
                    val curIsFree = cursor.getInt(isFreeColIndex)
                    val curId     = cursor.getInt(idColIndex)

                    if (curIsFree == 1) { //если находим свободную строку, то записываем в нее данные( как бы обновляем эту строку)
                        db.update(HotelContract.GuestsTable.TABLE_NAME, values, "_ID = ?", arrayOf(curId.toString()))
                        //и благополучно завершаем активность
                        val intent = Intent()
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                        return@setOnClickListener
                    }
                }
            } catch (th : Throwable) {
                Toast.makeText(applicationContext, th.message, Toast.LENGTH_LONG).show()
            }

            db.insert(HotelContract.GuestsTable.TABLE_NAME, null, values)

            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun getGenderName(): String? {
        when (mGender) {
            0 -> return HotelContract.GuestsTable.GENDER_MALE_S
            1 -> return HotelContract.GuestsTable.GENDER_FEMALE_S
            2 -> return HotelContract.GuestsTable.GENDER_TRANS_S
            3 -> return HotelContract.GuestsTable.GENDER_NS_S
        }
        return "Monkey"
    }


    private fun setUpSpinner() {
        val genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
            R.array.ArrayOfGender, android.R.layout.simple_spinner_item)
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        mSpinnerGender.adapter = genderSpinnerAdapter
        mSpinnerGender.setSelection(HotelContract.GuestsTable.GENDER_NS)

        //Именно так можно определять выбор айтема на спинере:
        mSpinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                mSpinnerGender.setSelection(HotelContract.GuestsTable.GENDER_NS)
                mGender = HotelContract.GuestsTable.GENDER_NS
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    HotelContract.GuestsTable.GENDER_MALE -> {
                        mSpinnerGender.setSelection(HotelContract.GuestsTable.GENDER_MALE)
                        mGender = HotelContract.GuestsTable.GENDER_MALE
                    }
                    HotelContract.GuestsTable.GENDER_FEMALE -> {
                        mSpinnerGender.setSelection(HotelContract.GuestsTable.GENDER_FEMALE)
                        mGender = HotelContract.GuestsTable.GENDER_FEMALE
                    }
                    HotelContract.GuestsTable.GENDER_TRANS -> {
                        mSpinnerGender.setSelection(HotelContract.GuestsTable.GENDER_TRANS)
                        mGender = HotelContract.GuestsTable.GENDER_TRANS
                    }
                    HotelContract.GuestsTable.GENDER_NS -> {
                        mSpinnerGender.setSelection(HotelContract.GuestsTable.GENDER_NS)
                        mGender = HotelContract.GuestsTable.GENDER_NS
                    }
                }
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }
}








