package kr.co.nexmore.onimaniapp.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_add_friend.*
import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.common.transitions.FabTransform

class AddFriendActivity : AppCompatActivity() {

    private lateinit var imm : InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        FabTransform.setup(this, add_friend_a_search_container)

        initButton()
    }

    private fun initButton() {
        add_friend_a_btn_find.setOnClickListener {
            doneButton()
        }
        add_friend_a_input_email.imeOptions = EditorInfo.IME_ACTION_DONE
        add_friend_a_input_email.setOnEditorActionListener { _, actionId, _ ->
            if ( actionId == EditorInfo.IME_ACTION_DONE ) {
                doneButton()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun doneButton() {
        hideKeyboard()
        val inputString = add_friend_a_input_email.text.toString()

        if ( inputString.isEmpty() ) {
            Snackbar.make(add_friend_a_input_email, "이메일을 입력해주세요.", Snackbar.LENGTH_LONG).show()
            return
        }

        val intent = Intent()
        intent.putExtra("email", inputString)
        setResult(Activity.RESULT_OK, intent)
        finishAfterTransition()
    }

    private fun hideKeyboard() {
        imm.hideSoftInputFromWindow(add_friend_a_input_email.windowToken, 0)
    }

    fun dismiss(view: View) {
        hideKeyboard()
        finishAfterTransition()
    }
}
