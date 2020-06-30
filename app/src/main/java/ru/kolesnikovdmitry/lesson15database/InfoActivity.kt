package ru.kolesnikovdmitry.lesson15database

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.RestrictionsManager.RESULT_ERROR
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ru.kolesnikovdmitry.lesson15database.data.HotelContract
import ru.kolesnikovdmitry.lesson15database.data.HotelDbHelper

class InfoActivity: AppCompatActivity() {

    companion object {
        var RESULT_ERROR = 8800
    }

    lateinit var mEditTextName : EditText
    lateinit var mEditTextAge  : EditText
    lateinit var mSpinner      : Spinner

    private lateinit var mDbHelper   : HotelDbHelper

    var mCurGender : String = ""
    var mCurName   : String = ""
    var mCurAge    : Int = 0
    var mIsFree = 0
    var mId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        mDbHelper = HotelDbHelper(this)

        mEditTextAge = findViewById(R.id.editTextAgeActInfo)
        mEditTextName = findViewById(R.id.editTextNameActInfo)
        mSpinner = findViewById(R.id.spinnerActInfo)

        initializeValues()                                                                          //в этой функции мы открываем переменные и присваеваем mCurGender и прочие

        mEditTextName.setText(mCurName)
        mEditTextAge.setText(mCurAge.toString())
        try {
            setUpSpinner()
        } catch (th : Throwable) {
            finish()
        }
    }

    private fun initializeValues() {
        try {
            mId = intent.getStringExtra("curId").toString().toInt()
            mCurName = intent.getStringExtra("curName").toString()
            mCurAge = intent.getStringExtra("curAge").toString().toInt()
            mCurGender = intent.getStringExtra("curGender").toString()
        } catch (th: Throwable) {
            Toast.makeText(this, th.message, Toast.LENGTH_LONG).show()
            setResult(RESULT_ERROR, intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    private fun setUpSpinner() {
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.ArrayOfGender,
            android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        mSpinner.adapter = spinnerAdapter
        when (mCurGender) {
            HotelContract.GuestsTable.GENDER_NS_S -> mSpinner.setSelection(3)
            HotelContract.GuestsTable.GENDER_TRANS_S -> mSpinner.setSelection(2)
            HotelContract.GuestsTable.GENDER_FEMALE_S -> mSpinner.setSelection(1)
            HotelContract.GuestsTable.GENDER_MALE_S -> mSpinner.setSelection(0)
        }

        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //nothing
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    HotelContract.GuestsTable.GENDER_MALE -> {
                        mSpinner.setSelection(0)
                        mCurGender = HotelContract.GuestsTable.GENDER_MALE_S
                    }
                    HotelContract.GuestsTable.GENDER_FEMALE -> {
                        mSpinner.setSelection(1)
                        mCurGender = HotelContract.GuestsTable.GENDER_FEMALE_S
                    }
                    HotelContract.GuestsTable.GENDER_TRANS -> {
                        mSpinner.setSelection(2)
                        mCurGender = HotelContract.GuestsTable.GENDER_TRANS_S
                    }
                    HotelContract.GuestsTable.GENDER_NS -> {
                        mSpinner.setSelection(3)
                        mCurGender = HotelContract.GuestsTable.GENDER_NS_S
                    }
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_act_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuItemConfirmActInfo -> {
                if(updateDatabaseInfo() == 1) {
                    return false
                }
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        return true
    }

    private fun updateDatabaseInfo() : Int {
        mCurName = mEditTextName.text.toString()

        if (mCurName == "") {
            Snackbar.make(mSpinner, "Enter name, please!", Snackbar.LENGTH_LONG).show()
            return 1
        }
        if (mEditTextAge.text.toString() == "") {
            Snackbar.make(mSpinner, "Enter age, please!", Snackbar.LENGTH_LONG).show()
            return 1
        }
        mCurAge = mEditTextAge.text.toString().toInt()

        val db : SQLiteDatabase = mDbHelper.writableDatabase
        val values  = ContentValues()
        values.put(HotelContract.GuestsTable.COLUMN_NAME, mCurName)
        values.put(HotelContract.GuestsTable.COLUMN_AGE, mCurAge)
        values.put(HotelContract.GuestsTable.COLUMN_GENDER, mCurGender)
        db.update(
            HotelContract.GuestsTable.TABLE_NAME,
            values,
            "_ID = ?",
            arrayOf(mId.toString()))
        return 0
    }
}