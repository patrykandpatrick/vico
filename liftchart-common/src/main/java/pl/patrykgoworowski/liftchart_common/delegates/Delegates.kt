package pl.patrykgoworowski.liftchart_common.delegates

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <T> observable(initialValue: T, crossinline onChange: (newValue: T) -> Unit):
        ReadWriteProperty<Any?, T> =
    object : ObservableProperty<T>(initialValue) {
        override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = onChange(newValue)
    }