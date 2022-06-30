package com.example.youart

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import java.text.NumberFormat

class BidDialogFragment : DialogFragment() {
    private var priceTxt : EditText? = null
    private var currentInputVal = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment
        return inflater.inflate(R.layout.fragment_bid_dialog, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        priceTxt = view.findViewById(R.id.bid_value)
        priceTxt!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TEST", s.toString())
                Log.d("TEST2",s.toString() )
                if(s.toString() == null){
                    return;
                }
                if (!currentInputVal.equals(s.toString())) {
                    priceTxt!!.removeTextChangedListener(this);
                    val numberFormat = NumberFormat.getCurrencyInstance()
                    numberFormat.setMaximumFractionDigits(0);
                    var num = s.toString().filter { it.isDigit()}
                    if(num.isEmpty()){
                        num="0"
                    }
                    val convert = numberFormat.format(num.toFloat())
                    currentInputVal = convert
                    priceTxt!!.setText(convert)
                    priceTxt!!.addTextChangedListener(this);
                    priceTxt!!.setSelection(priceTxt!!.length());

                }
            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }
}