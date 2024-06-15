package io.twinkle.unreal.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.materialswitch.MaterialSwitch
import io.twinkle.unreal.R

class MainSwitchBar(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), CompoundButton.OnCheckedChangeListener {

    private val mSwitchChangeListeners: ArrayList<OnMainSwitchChangeListener> = arrayListOf()

    private var switchWidget: MaterialSwitch
    private var titleView: TextView
    private var iconView: ImageView
    private var cardView: MaterialCardView
    var barEnabled: Boolean = true
        set(value) {
            switchWidget.isEnabled = value
            titleView.isEnabled = value
            if (value) {
                setBackground(isChecked)
            } else {
                cardView.setCardBackgroundColor(
                    MaterialColors.getColor(
                        this,
                        R.attr.colorSurfaceContainer
                    )
                )
            }
            field = value
        }
        get() = switchWidget.isEnabled
    var isChecked: Boolean = false
        set(value) {
            switchWidget.isChecked = value
            setBackground(value)
            field = value
        }
        get() = switchWidget.isChecked

    var title: String = ""
        set(value) {
            titleView.text = value
            field = value
        }
    private var icon: Drawable? = null
        set(value) {
            if (value != null) {
                iconView.visibility = View.VISIBLE
                iconView.setImageDrawable(icon)
            } else {
                iconView.visibility = View.GONE
                iconView.setImageDrawable(null)
            }
            field = value
        }
    private var iconTint: Int = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.main_switch_bar, this)
        switchWidget = findViewById(R.id.switch_widget)
        titleView = findViewById(R.id.title)
        iconView = findViewById(R.id.restricted_icon)
        cardView = findViewById(R.id.main_switch_card)
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MainSwitchBar)
            title = typedArray.getString(R.styleable.MainSwitchBar_title) ?: ""
            icon = typedArray.getDrawable(R.styleable.MainSwitchBar_icon)
            iconTint =
                typedArray.getColor(R.styleable.MainSwitchBar_iconTint, 0)
            barEnabled = typedArray.getBoolean(R.styleable.MainSwitchBar_enabled, true)
            typedArray.recycle()
            titleView.text = title
            if (icon != null) {
                iconView.visibility = View.VISIBLE
                iconView.setImageDrawable(icon)
            }
            if (iconTint != 0) {
                iconView.imageTintList = ColorStateList.valueOf(iconTint)
            }

            setBackground(switchWidget.isChecked)
        }
        addOnSwitchChangeListener { _: MaterialSwitch?, checked: Boolean ->
            isChecked = checked
        }
        switchWidget.setOnCheckedChangeListener(this)
        cardView.setOnClickListener {
            switchWidget.performClick()
        }
    }

    private fun propagateChecked(isChecked: Boolean) {
        setBackground(isChecked)
        val count = mSwitchChangeListeners.size
        for (n in 0 until count) {
            mSwitchChangeListeners[n].onSwitchChanged(switchWidget, isChecked)
        }
    }

    fun addOnSwitchChangeListener(listener: OnMainSwitchChangeListener) {
        if (!mSwitchChangeListeners.contains(listener)) {
            mSwitchChangeListeners.add(listener)
        }
    }

    /**
     * Remove a listener for switch changes
     */
    fun removeOnSwitchChangeListener(listener: OnMainSwitchChangeListener) {
        mSwitchChangeListeners.remove(listener)
    }

    override fun onCheckedChanged(button: CompoundButton?, b: Boolean) {
        propagateChecked(b)
    }

    private fun setBackground(boolean: Boolean) {
        if (boolean) {
            cardView.setCardBackgroundColor(
                MaterialColors.getColor(
                    this,
                    R.attr.colorPrimaryContainer
                )
            )
        } else {
            cardView.setCardBackgroundColor(
                MaterialColors.getColor(
                    this,
                    R.attr.colorSurfaceContainer
                )
            )
        }
    }


    internal class SavedState : BaseSavedState {
        var mChecked = false

        constructor(superState: Parcelable?) : super(superState)

        /**
         * Constructor called from [.CREATOR]
         */
        private constructor(`in`: Parcel) : super(`in`) {
            mChecked = (`in`.readValue(null) as Boolean?)!!
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeValue(mChecked)
        }

        override fun toString(): String {
            return ("MainSwitchBar.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + mChecked + "}")
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }


    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.mChecked = switchWidget.isChecked
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        switchWidget.setChecked(ss.mChecked)
        isChecked = ss.mChecked
        setBackground(ss.mChecked)
        requestLayout()
    }
}