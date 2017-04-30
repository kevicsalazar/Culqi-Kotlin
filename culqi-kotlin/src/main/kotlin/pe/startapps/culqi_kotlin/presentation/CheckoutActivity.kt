package pe.startapps.culqi_kotlin.presentation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_checkout.*
import pe.startapps.culqi_kotlin.R
import android.text.InputFilter
import android.view.View
import pe.startapps.culqi_kotlin.domain.TokenBuilder
import pe.startapps.culqi_kotlin.domain.model.CulqiCard
import pe.startapps.culqi_kotlin.utils.textChangedListener
import android.app.Activity
import android.content.Intent
import com.google.gson.Gson
import pe.startapps.culqi_kotlin.domain.CulqiValidation


/**
 * @author Kevin Salazar
 * @link kevicsalazar.com
 */
class CheckoutActivity : AppCompatActivity() {

    var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        setFinishOnTouchOutside(false)

        val apiKey = intent.getStringExtra("apiKey")
        val amount = intent.getStringExtra("amount")

        tvAmount.text = amount

        etCardNumber.textChangedListener {
            afterTextChanged {
                val text = etCardNumber.text.toString()
                if (text.isEmpty()) {
                    etCardNumber.error = "Ingrese un número de tarjeta"
                } else if (!CulqiValidation.luhn(text)) {
                    etCardNumber.error = "Ingrese un número de tarjeta válido"
                } else {
                    etCardNumber.error = null
                }
                val cvv = CulqiValidation.bin(text, ivCardType)
                if (cvv > 0) {
                    etCVV.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(cvv))
                    etCVV.isEnabled = true
                } else {
                    etCVV.isEnabled = false
                    etCVV.setText("")
                }
                checkFields()
            }
        }

        etExpirationDate.textChangedListener {
            afterTextChanged {
                val text = etExpirationDate.text.toString()
                if (!CulqiValidation.expirationDate(text)) {
                    etExpirationDate.error = "Ingrese una fecha válida"
                } else if (CulqiValidation.expirationDate(text) && text.length == 2) {
                    etExpirationDate.setText("$text/")
                    etExpirationDate.setSelection(text.length + 1)
                }
                checkFields()
            }
        }

        etEmailAddress.textChangedListener {
            afterTextChanged {
                val text = etEmailAddress.text.toString()
                if (!CulqiValidation.emailAddress(text)) {
                    etEmailAddress.error = "Ingrese un correo válido"
                }
                checkFields()
            }
        }

        btnPay.setOnClickListener {

            val cardNumber = etCardNumber.text.toString()
            val expirationDate = etExpirationDate.text.toString()
            val email = etEmailAddress.text.toString()
            val cvv = etCVV.text.toString()

            if (!loading) {

                val expirationMonth = expirationDate.substring(0, 2).toInt()
                val expirationYear = expirationDate.substring(3, 5).toInt()

                val card = CulqiCard(cardNumber, cvv, expirationMonth, expirationYear, email)

                showProgress()

                TokenBuilder(apiKey).createToken(card, {
                    hideProgress()
                    val resultIntent = Intent()
                    resultIntent.putExtra("successful", true)
                    resultIntent.putExtra("token", Gson().toJson(it))
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }, {
                    hideProgress()
                    val resultIntent = Intent()
                    resultIntent.putExtra("successful", false)
                    resultIntent.putExtra("error", Gson().toJson(it))
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                })

            }

        }

        btnPay.isEnabled = false

    }

    fun checkFields() {

        val cardNumber = etCardNumber.text.toString()
        val expirationDate = etExpirationDate.text.toString()
        val email = etEmailAddress.text.toString()

        if (CulqiValidation.luhn(cardNumber)
                && CulqiValidation.expirationDate(expirationDate)
                && CulqiValidation.emailAddress(email)) {
            btnPay.setBackgroundResource(R.drawable.bg_button)
            btnPay.isEnabled = true
        } else {
            btnPay.setBackgroundResource(R.drawable.bg_button_disabled)
            btnPay.isEnabled = false
        }

    }

    fun showProgress() {
        loading = true
        layoutPay.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        loading = false
        layoutPay.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
    }

}