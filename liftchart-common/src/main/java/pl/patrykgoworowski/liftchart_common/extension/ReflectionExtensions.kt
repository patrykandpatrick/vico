package pl.patrykgoworowski.liftchart_common.extension

inline fun <reified T, V> T.setFieldValue(fieldName: String, value: V) {
    val field = T::class.java.getDeclaredField(fieldName)
    val wasAccessible = field.isAccessible
    field.isAccessible = true
    field.set(this, value)
    field.isAccessible = wasAccessible
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T, V> T.getFieldValue(fieldName: String): V {
    val field = T::class.java.getDeclaredField(fieldName)
    val wasAccessible = field.isAccessible
    field.isAccessible = true
    val value = field.get(this) as V
    field.isAccessible = wasAccessible
    return value
}