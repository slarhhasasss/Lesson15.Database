package ru.kolesnikovdmitry.lesson15database

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.app.SearchManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.MenuItemCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main_layout.*
import ru.kolesnikovdmitry.lesson15database.data.HotelContract
import ru.kolesnikovdmitry.lesson15database.data.HotelDbHelper
import java.util.*


/*Полезные Технологии в этой програамме:
* Запись, Чтение, Удаление базы данных SQLite3
* CardView()
* Spinner - Выпадающее окно
* Динамическое добавление элементов View в родительский элемент
* OnTouchListener()
* Handler() - Таймер в главном потоке UI
 */











class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    //private lateinit var mFabActMain : FloatingActionButton
    private lateinit var mDbHelper   : HotelDbHelper
    private lateinit var mLinearLayout : LinearLayout
    private val mRequestCodeAddActivity = 101
    private val mRequestCodeInfoActivity = 102
    private var mAmountOfGuests = 0
    private lateinit var mActionBar : ActionBar
    /*var mIsPressed = 0
    var mIdCurCardView = 0*/
    //В этом массиве будут наши гости
    private var mArrayOfGuests: Array<Array<String>> = Array(0) {Array<String>(4) {""} }    //1 - айди гостя, 2 - Имя, 3 - возраст, 4 - гендер

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)

        /*mFabActMain = findViewById(R.id.FabActMain)
        mFabActMain.setOnClickListener {
            try{
                val intent = Intent(this, AddActivity::class.java) //теперь так создается экземпляр класса Intent в Kotlin.
                startActivityForResult(intent, mRequestCodeAddActivity)
            }catch (ex : Throwable) {
                Snackbar.make(it, ex.message.toString(), Snackbar.LENGTH_LONG).show()
            }
        }*/

        mDbHelper = HotelDbHelper(this)

        mLinearLayout = findViewById(R.id.linearLayoutContent)

        //сразу подсчитаем сколько у нас гостей есть в таблице
        val db : SQLiteDatabase = mDbHelper.readableDatabase

        val projection = arrayOf(                                                      //Выборка только по колонке свободности
            HotelContract.GuestsTable.COLUMN_IS_FREE)

        val cursor : Cursor = db.query(
            HotelContract.GuestsTable.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null)

        try {
            val isFreeColIndex = cursor.getColumnIndex(HotelContract.GuestsTable.COLUMN_IS_FREE)
            while (cursor.moveToNext()) {
                val curIsFree = cursor.getInt(isFreeColIndex)
                if (curIsFree == 0) {
                    mAmountOfGuests++
                }
            }
        }catch (th : Throwable) {
            Toast.makeText(this, th.message, Toast.LENGTH_LONG).show()
        }

        //Создаем Список Гостей на главном экране
        displayDatabaseInfo()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_act_main, menu)

        //Код для обработки поискового запроса
        val searchManager : SearchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val menuItemSearch : MenuItem = menu!!.findItem(R.id.menuItemSearchActMain)                 //!!. - non null assert
        val searchView : SearchView = MenuItemCompat.getActionView(menuItemSearch) as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        //здесь мы устанавливаем на этот SearchView слушатель нажатия клавиш (внизу)
        searchView.setOnQueryTextListener(this)

        return true
    }


    //Функция для поиска и отображения найденных элементов
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun displaySearchableInfo(str: String?) {
        mLinearLayout.removeAllViews()                                                             //Удаляем все карточки, чтобы далее вывести только нужные
        for (guest in mArrayOfGuests) {                                              //пробегаемся по гостям и ищем подходящих
            val curName = guest[1]                                                          //curName - имя
            if(curName == str || curName.contains(str.toString(), ignoreCase = true)) {           //curName.contains(str, ...) - возвращает true, если подстрока str есть в строке curName
                addNewGuestCard(guest[0].toInt(), guest[1], guest[2].toInt(), guest[3])           //Добавляем на экран карточку с этим гсостем
            }
        }



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_add_act_main -> {
                startActivityForResult(Intent(this, AddActivity::class.java), mRequestCodeAddActivity)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayDatabaseInfo() {
        //Перед тем, как отображать информацию, всегда очищаем массив, чтобы избежать перезаписи:
        mArrayOfGuests = mArrayOfGuests.dropLast(mArrayOfGuests.size).toTypedArray()

        val db : SQLiteDatabase = mDbHelper.readableDatabase                                        //открываем для чтения нашу бд
        val projection = arrayOf(HotelContract.GuestsTable._ID,
                                              HotelContract.GuestsTable.COLUMN_NAME,
                                              HotelContract.GuestsTable.COLUMN_AGE,
                                              HotelContract.GuestsTable.COLUMN_GENDER,
                                              HotelContract.GuestsTable.COLUMN_IS_FREE)

        val cursor : Cursor = db.query(
            HotelContract.GuestsTable.TABLE_NAME,                                                   //таблица
            projection,                                                                             //столбцы
            null,                                                                           // столбцы для условия where
            null,                                                                        //значения для условия where
            null,                                                                           // ??
            null,                                                                             //??
            null)                                                                            //порядок сортировки

        val textViewDatabaseName   : TextView = findViewById(R.id.textViewTableName)
        val textViewAmountOfGuests : TextView = findViewById(R.id.textViewAmountOfGuests)

        try{
            //textViewAmountOfGuests.text = cursor.count.toString()                                  //этот метод даст общее количество строк в таблице (пустых или нет)
            textViewAmountOfGuests.text = mAmountOfGuests.toString()
            textViewDatabaseName.text = HotelContract.GuestsTable.TABLE_NAME

            val idColIndex     = cursor.getColumnIndex(HotelContract.GuestsTable._ID)
            val nameColIndex   = cursor.getColumnIndex(HotelContract.GuestsTable.COLUMN_NAME)
            val ageColIndex    = cursor.getColumnIndex(HotelContract.GuestsTable.COLUMN_AGE)
            val genderColIndex = cursor.getColumnIndex(HotelContract.GuestsTable.COLUMN_GENDER)
            val isFreeColIndex = cursor.getColumnIndex(HotelContract.GuestsTable.COLUMN_IS_FREE)

            while (cursor.moveToNext()) {                                                            //пробегаемся по всем строкам и выводим их содержимое
                val curId        = cursor.getInt(idColIndex)                                    //по айди будем получать номер строки
                val curName   = cursor.getString(nameColIndex)
                val curAge       = cursor.getInt(ageColIndex)
                val curGender = cursor.getString(genderColIndex)
                val curIsFree    = cursor.getString(isFreeColIndex).toInt()

                if(curIsFree == 0) {
                    //Добавляем в наш массив гостей данного гостя:
                    val tmpGuestAddArr : Array<String> = arrayOf(curId.toString(), curName, curAge.toString(), curGender)
                    mArrayOfGuests += tmpGuestAddArr
                    //создам карточку с новым гостем:
                    addNewGuestCard(curId, curName, curAge, curGender)
                }
            }
            val emptyLay = TextView(applicationContext)
            emptyLay.height = 100
            emptyLay.text = "ЛОХ"
            emptyLay.gravity = Gravity.CENTER
            mLinearLayout.addView(emptyLay)
        } catch (ex : Throwable) {
            Toast.makeText(applicationContext, ex.message, Toast.LENGTH_LONG).show()
        } finally {
            cursor.close()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addNewGuestCard(curId: Int, curName: String?, curAge: Int, curGender: String?) {
        //создаем родительский элемент - кардВью, в кторый будем запихивать содержимое
        val cardView = CardView(applicationContext)
        val layParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, //Ширина cardView
                                                                              LinearLayout.LayoutParams.WRAP_CONTENT)  //высота CardView
        layParams.setMargins(20, 20, 20, 20)
        cardView.layoutParams = layParams                                                        //Задаем ширину и высоту CardView
        cardView.radius = 20F
        cardView.setContentPadding(20, 20, 20, 20)
        cardView.setCardBackgroundColor(Color.LTGRAY)
        cardView.cardElevation = 10F
        cardView.maxCardElevation = 20F
        cardView.setOnClickListener {
            //Во время нажатия на клавишу произойдет анимация нажатия на кнопку (в нашем случае она изменит цвет)
            cardView.setBackgroundColor(Color.GRAY)
            //Toast.makeText(applicationContext, curName, Toast.LENGTH_SHORT).show()
            //а затем через 200 миллисекунд она опять станет прежней (как бы включилась и выключилась)
            Handler().postDelayed({
                cardView.setBackgroundColor(Color.LTGRAY)
            }, 100)
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("curId", curId.toString())
            intent.putExtra("curName", curName.toString())
            intent.putExtra("curAge", curAge.toString())
            intent.putExtra("curGender", curGender.toString())
            startActivityForResult(intent, mRequestCodeInfoActivity)
        }

        /*cardView.setOnTouchListener { v : View, event : MotionEvent ->
            when(event.action) {
                //Опускание Пальца
                MotionEvent.ACTION_DOWN -> {
                    if(mIsPressed == 0) {
                        mIdCurCardView = curId
                        cardView.setBackgroundColor(Color.GRAY)
                    }
                    Handler().postDelayed({
                        if (mIsPressed == 1 && mIdCurCardView == curId) {
                            v.performLongClick()
                        }
                    }, 1000)
                    mIsPressed++
                    return@setOnTouchListener true
                }
                //Поднятие пальца
                MotionEvent.ACTION_UP -> {
                    cardView.setBackgroundColor(Color.LTGRAY)
                    if (mIsPressed == 1) {
                        v.performClick()
                    }
                     mIsPressed--
                    return@setOnTouchListener true
                }
                //Если начинается другой движ (например скролл, или просто за пределы вышел, передумал нажимать)
                MotionEvent.ACTION_CANCEL -> {
                    mIsPressed--
                    cardView.setBackgroundColor(Color.LTGRAY)
                    return@setOnTouchListener true
                }
                //А это значение отслеживает изменения состояния межжду опусканием пальца и его поднятием
                MotionEvent.ACTION_MOVE -> {
                    mIsPressed--
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener true
        }*/

        //так же создадим дочерний лейаут, в который будем все пихать
        val linearLayHorizontal = LinearLayout(this)
        linearLayHorizontal.layoutParams = layParams
        linearLayHorizontal.orientation = LinearLayout.HORIZONTAL
        linearLayHorizontal.setPadding(10, 10, 10, 10)
        //Текст Вьюшка для номера
        val textViewId = TextView(this)
        textViewId.setPadding(10, 10, 10, 10)
        //лэйаут для имени, пола и возраста
        val linearLayVertical = LinearLayout(this)
        linearLayVertical.setPadding(10, 10, 10, 10)
        linearLayVertical.orientation = LinearLayout.VERTICAL
        //ТекстВьюшки для имени, пола и возраста:
        val textViewName   = TextView(this)
        val textViewGender = TextView(this)
        val textViewAge    = TextView(this)
        //Теперь наполняем каждую текстВьюшку текстом:
        textViewId.text     = curId.toString()
        textViewName.text   = curName
        textViewGender.text = curGender
        textViewAge.text    = curAge.toString()
        //Добавим некоторые параметры нашим текстВьюшкам:
        textViewId.gravity = Gravity.CENTER
        val layParamsTextView = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                          LinearLayout.LayoutParams.WRAP_CONTENT)
        //чтобы все наши текстВьюшки были растянуты по контенту
        textViewId.layoutParams     = layParamsTextView
        textViewAge.layoutParams    = layParamsTextView
        textViewGender.layoutParams = layParamsTextView
        textViewName.layoutParams   = layParamsTextView
        //Одинаковый размер шрифта
        textViewAge.textSize    = 25F
        textViewGender.textSize = 25F
        textViewId.textSize     = 25F
        textViewName.textSize   = 25F
        //Начинаем заполнять контейнеры:
        //Сначала вертикальную разметку
        linearLayVertical.addView(textViewName)
        linearLayVertical.addView(textViewGender)
        linearLayVertical.addView(textViewAge)
        //теперь заполняем горизонтаьный LinearLayout:
        linearLayHorizontal.addView(textViewId)
        linearLayHorizontal.addView(linearLayVertical)

        //Теперь все это пихаем в cardView
        cardView.addView(linearLayHorizontal)

        cardView.setOnLongClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.menu.add(curId, 1, 1, "Delete").setOnMenuItemClickListener {
                removeGuest(curId)
                mLinearLayout.removeView(cardView)
                //уменьшаем на 1 количество гостей
                val textViewAmountOfGuests : TextView = findViewById(R.id.textViewAmountOfGuests)
                textViewAmountOfGuests.text = (textViewAmountOfGuests.text.toString().toInt() - 1).toString()
                mAmountOfGuests--
                Snackbar.make(mLinearLayout, "Guest was removed successfully!", Snackbar.LENGTH_SHORT).show()
                return@setOnMenuItemClickListener true
            }
            popupMenu.menu.add(curId, 2, 2, "Cancel").setOnMenuItemClickListener {
                cardView.isClickable = true
                popupMenu.dismiss()
                return@setOnMenuItemClickListener true
            }
            popupMenu.setOnDismissListener {
                cardView.isClickable = true
                popupMenu.dismiss()
            }
            popupMenu.show()
            return@setOnLongClickListener true
        }

        //Теперь этот CardView надо запихнуть в наш корневой LinearLayout из разметки
        mLinearLayout.addView(cardView)
    }


    private fun removeGuest(curId: Int) {
        val db : SQLiteDatabase = mDbHelper.writableDatabase
        //db.delete(HotelContract.GuestsTable.TABLE_NAME,  HotelContract.GuestsTable._ID + " = ? ", arrayOf(curId.toString()))
        //теперь мы не удаляем строку, а просто ставим, что она свободна
        val values = ContentValues()
        values.put(HotelContract.GuestsTable.COLUMN_IS_FREE, 1)
        //метод update(): 1 арг - название таблицы, 2 - ContentValues, 3 - условие выборки, 4 - то, что стоит на месте ? 3м аргументе
        db.update(HotelContract.GuestsTable.TABLE_NAME, values, "_ID = ?", arrayOf(curId.toString()))
    }


    private fun deleteDatabaseInfo() {
        linearLayoutContent.removeAllViews()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            mRequestCodeAddActivity -> {
                if (resultCode == Activity.RESULT_OK) {
                    deleteDatabaseInfo()
                    mAmountOfGuests++
                    displayDatabaseInfo()
                    Snackbar.make(mLinearLayout, "New guests has been added successfully!", Snackbar.LENGTH_LONG).show()
                }
            }
            mRequestCodeInfoActivity -> {
                if(resultCode == Activity.RESULT_OK) {
                    deleteDatabaseInfo()
                    displayDatabaseInfo()
                    Snackbar.make(mLinearLayout, "Guest's info was changed!", Snackbar.LENGTH_LONG).show()
                }
                else if(resultCode == InfoActivity.RESULT_ERROR) {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    //что произойдет, когда нажмем кнопку искать
    override fun onQueryTextSubmit(query: String?): Boolean {
        displaySearchableInfo(query)
        return true
    }

    //а здесь что происходит, когда добавляются новые символы в поиск
    override fun onQueryTextChange(newText: String?): Boolean {
        //Если очистили строку поиска, то высвечиваются все наши карточки заново
        if(newText == "") {
            mLinearLayout.removeAllViews()
            displayDatabaseInfo()
            //и выходит из этой функции
            return true
        }
        //если изменилось не на пустую строку, то выводим подходящие запросы
        displaySearchableInfo(newText)
        return true
    }

}