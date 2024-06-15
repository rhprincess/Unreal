package io.twinkle.unreal.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import io.twinkle.unreal.R

class MaterialWarningBar(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var message: String = ""
        set(value) {
            messageView.text = value
            field = value
        }
    private var infoIcon: Drawable? = null
    private var infoIconTint: Int = 0

    var messageView: TextView
    var infoIconView: ImageView
    private var actionButtonContainer: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.material_warning_bar, this)
        messageView = findViewById(R.id.info_message)
        infoIconView = findViewById(R.id.info_icon)
        actionButtonContainer = findViewById(R.id.action_container)
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialWarningBar)
            message = typedArray.getString(R.styleable.MaterialWarningBar_message) ?: ""
            infoIcon = typedArray.getDrawable(R.styleable.MaterialWarningBar_infoIcon)
            infoIconTint =
                typedArray.getColor(R.styleable.MaterialWarningBar_infoIconTint, 0)
            typedArray.recycle()
            messageView.text = message
            if (infoIcon != null) {
                infoIconView.visibility = View.VISIBLE
                infoIconView.setImageDrawable(infoIcon)
            }
            if (infoIconTint != 0) {
                infoIconView.imageTintList = ColorStateList.valueOf(infoIconTint)
            }
        }
    }

    fun addActionButton(text: String, action: (View) -> Unit) {
        val button = LayoutInflater.from(context)
            .inflate(R.layout.action_button_layout, actionButtonContainer, false) as MaterialButton
        button.text = text
        button.setOnClickListener {
            action(it)
        }
        actionButtonContainer.addView(button)
    }

}