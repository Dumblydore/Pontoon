package me.mauricee.pontoon.ui.preferences.baseTheme

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.core.os.bundleOf
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.BaseTheme
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.databinding.PreferenceBaseThemeBinding
import me.mauricee.pontoon.ext.view.viewBinding
import javax.inject.Inject

class BaseThemePreference(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.dialogPreferenceStyle
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        defStyleAttr
    )

    var theme: BaseTheme = BaseTheme.Light
        set(value) {
            if (value != field) {
                persistString(value.name)
            }
            field = value
        }

    override fun getDialogLayoutResource(): Int = R.layout.preference_base_theme

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index)!!
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        theme = (defaultValue as? String)?.let { BaseTheme.fromString(it) } ?: BaseTheme.Light
    }

    @AndroidEntryPoint
    class Fragment : PreferenceDialogFragmentCompat() {
        @Inject
        lateinit var adapter: BaseThemeAdapter

        @Inject
        lateinit var themeManager: ThemeManager

        private lateinit var selectedTheme: BaseTheme
        private lateinit var selection: Disposable

        override fun onDialogClosed(positiveResult: Boolean) {
            if (positiveResult) {
                (preference as? BaseThemePreference)?.let {
                    it.theme = selectedTheme
                    themeManager.baseTheme = selectedTheme
                    themeManager.commit()
                }
            }
            selection.dispose()
        }

        override fun onBindDialogView(view: View) {
            super.onBindDialogView(view)
            val binding = PreferenceBaseThemeBinding.bind(view)
            binding.preferenceBaseThemes.adapter = adapter
            binding.preferenceBaseThemes.layoutManager = GridLayoutManager(requireContext(), 3)
            selection = adapter.actions.subscribe { selectedTheme = it }
            selectedTheme = themeManager.baseTheme
        }

        companion object {
            fun newInstance(key: String): Fragment =
                Fragment().apply { arguments = bundleOf(ARG_KEY to key) }
        }
    }

}