package com.example.calculator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.Exception
import kotlin.math.round

class MainActivity : AppCompatActivity() {
    private lateinit var txtInput: TextView    // выражение введенное пользователем
    private lateinit var txtResult: TextView    // результат выражения

    private var savedResult: Double = 0.0
    private var canAddOperation: Boolean = false
    private var canAddDecimal: Boolean = true
    private var bracketsToClose: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        setContentView(R.layout.activity_main)

        txtInput = findViewById(R.id.inputExpr)
        txtInput.isSelected = true  // необходимо для правильной работы однострочного режима
        txtResult = findViewById(R.id.result)

        loadData()
    }

    // прим.: для сохранения данных выбрано состояние Stopped, тк onDestroy не всегда вызывается
    // из-за особенностей очистки озу
    override fun onStop() {
        super.onStop()
        saveData()
    }

    // ивент при нажатии на цифру
    fun numberAction(view: android.view.View) {
        if (view is Button) {
            if (view.text == ".") {
                if (canAddDecimal)
                    txtInput.append(view.text)
                canAddDecimal = false
            } else {
                txtInput.append(view.text)
                canAddOperation = true
            }
        }
    }

    // ивент при нажатии на кнопку сохранения
    fun saveAction(view: android.view.View) {
        if (txtResult.text.isNotEmpty())
            savedResult = txtResult.text.toString().toDouble()
    }

    // ивент при нажатии на кнопку чтения сохраненного результата
    fun readAction(view: android.view.View) {
        txtInput.append(savedResult.toString())
    }

    // функция запоминания текущего выражения и результата в SharedPreferences
    private fun saveData() {
        val lastInput = txtInput.text.toString()
        val lastRes = txtResult.text.toString()

        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putString("INPUT_KEY", lastInput)
            putString("RES_KEY", lastRes)
        }.apply()
    }

    // функция загрузки данных из SharedPreferences
    private fun loadData() {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedInput = sharedPreferences.getString("INPUT_KEY", null)
        val savedRes = sharedPreferences.getString("RES_KEY", null)

        txtInput.text = savedInput
        txtResult.text = savedRes
    }

    // сброс поля ввода
    fun resetAction(view: android.view.View) {
        txtInput.text = ""
        txtResult.text = ""
        canAddOperation = false
        bracketsToClose = 0
    }

    // удаление последнего символа
    fun deleteAction(view: android.view.View) {
        val length = txtInput.length()
        if (length > 0) {
            if (txtInput.text[length - 1] == '.') {
                canAddDecimal = true
            } else if (txtInput.text[length - 1] == '(') {
                bracketsToClose--
            } else if (txtInput.text[length - 1] == ')') {
                bracketsToClose++
            }

            if (length > 1) {
                canAddOperation = txtInput.text[length - 2].isDigit()
            } else {
                canAddOperation = false
            }

            txtInput.text = txtInput.text.subSequence(0, length - 1)
        }
    }

    // ивент при нажатии на одну из операций (+, -, *, /)
    fun operationAction(view: android.view.View) {
        if (view is Button && canAddOperation) {
            canAddOperation = false
            canAddDecimal = true
            txtInput.append(view.text)
        }
    }

    // вычисление выражения и вывод результата
    fun equalsAction(view: android.view.View) {
        val expr = txtInput.text.toString()
        val expression = ExpressionBuilder(expr).build()

        try {
            if (bracketsToClose > 0)
                throw Exception("Opened bracket")
            val result = round(expression.evaluate() * 100) / 100
            txtResult.text = result.toString()
        } catch (ex: ArithmeticException) {
            txtResult.text = "Error"
        } catch (ex: Exception) {
            txtResult.text = "Error"
        }
    }

    // ивент при нажатии на открывающую скобку
    fun oBracketAction(view: android.view.View) {
        if (view is Button && !canAddOperation) {
            txtInput.append(view.text)
            bracketsToClose++
        }
    }

    // ивент при нажатии на закрывающую скобку
    fun cBracketAction(view: android.view.View) {
        if (view is Button && canAddOperation && bracketsToClose > 0) {
            txtInput.append(view.text)
            bracketsToClose--
        }
    }

    // ивент при нажатии на одну из мат. функций (cos, sin, tan, log)
    fun functionAction(view: android.view.View) {
        if (view is Button && !canAddOperation) {
            txtInput.append(view.text)
            txtInput.append("(")
            bracketsToClose++
            canAddOperation = true
        }
    }

    // ивент для кнопки квадратного корня
    // прим.: реализован как отдельная функция для корректной работы ExpressionBuilder
    fun sqrtAction(view: android.view.View) {
        if (view is Button && !canAddOperation) {
            txtInput.append("sqrt(")
            bracketsToClose++
            canAddOperation = true
        }
    }
}
