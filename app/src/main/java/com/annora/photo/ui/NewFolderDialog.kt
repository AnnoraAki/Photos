package com.annora.photo.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.annora.photo.R
import com.annora.photo.utils.getScreenWeight
import com.annora.photo.utils.toast
import kotlinx.android.synthetic.main.dialog_new_folder.*

class NewFolderDialog(
    context: Context,
    private val onClicked: (name: String) -> Unit
) : Dialog(context) {

    private var canCreate = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_new_folder)

        et_name.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                canCreate = true
                tv_create_sure.setTextColor(ContextCompat.getColor(context, R.color.blue_light))
            } else {
                canCreate = false
                tv_create_sure.setTextColor(ContextCompat.getColor(context, R.color.text_gray))
            }
        }

        tv_create_cancel.setOnClickListener {
            dismiss()
        }
        tv_create_sure.setOnClickListener {
            if (canCreate) {
                //todo 名字不能重复
                onClicked(et_name.text.toString())
            } else {
                context.toast("需要填写一个名字哦～")
            }
        }
    }


    // 防止显示后布局内容无法填充整个dialog的问题
    override fun show() {
        super.show()
        val lp = WindowManager.LayoutParams()
        val window = this.window
        lp.copyFrom(window?.attributes)
        lp.width = (context.getScreenWeight() * 0.7).toInt()
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        window?.attributes = lp
    }

    override fun dismiss() {
        super.dismiss()
        et_name.setText("")
    }
}