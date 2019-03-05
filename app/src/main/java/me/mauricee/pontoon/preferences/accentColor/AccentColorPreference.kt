package me.mauricee.pontoon.preferences.accentColor

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.core.os.bundleOf
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.preference_base_theme.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.AccentColor
import me.mauricee.pontoon.common.theme.ThemeManager
import javax.inject.Inject

class AccentColorPreference(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.dialogPreferenceStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, defStyleAttr)

    var theme: AccentColor = AccentColor.Default
        set(value) {
            if (value != field) {
                persistString(value.name)
            }
        }

    override fun getDialogLayoutResource(): Int = R.layout.preference_base_theme

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        theme = (defaultValue as? String)?.let { AccentColor.fromString(it) } ?: AccentColor.Default
    }

    class Fragment : PreferenceDialogFragmentCompat() {
        @Inject
        lateinit var adapter: AccentColorAdapter
        @Inject
        lateinit var themeManager: ThemeManager

        private lateinit var selectedAccentColor: AccentColor
        private lateinit var selection: Disposable

        override fun onDialogClosed(positiveResult: Boolean) {
            if (positiveResult) {
                (preference as? AccentColorPreference)?.let {
                    it.theme = selectedAccentColor
                    themeManager.accentColor = selectedAccentColor
                    themeManager.commit()
                }
            }
            selection.dispose()
        }

        override fun onBindDialogView(view: View) {
            AndroidSupportInjection.inject(this)
            super.onBindDialogView(view)
            view.preference_base_themes.adapter = adapter
            view.preference_base_themes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            selection = adapter.actions.subscribe { selectedAccentColor = it }
            selectedAccentColor = themeManager.accentColor
        }

        companion object {
            fun newInstance(key: String): Fragment = Fragment().apply { arguments = bundleOf(ARG_KEY to key) }
        }
    }

}