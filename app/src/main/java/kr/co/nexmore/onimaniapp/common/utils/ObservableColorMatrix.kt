package kr.co.nexmore.onimaniapp.common.utils

import android.graphics.ColorMatrix
import android.util.Property

/**
 * An extension to [ColorMatrix] which caches the saturation value for animation purposes.
 */
class ObservableColorMatrix : ColorMatrix() {

    private var saturation = 1f

    fun getSaturation(): Float {
        return saturation
    }

    override fun setSaturation(saturation: Float) {
        this.saturation = saturation
        super.setSaturation(saturation)
    }


    /**
     * An implementation of [Property] to be used specifically with fields of
     * type
     * `float`. This type-specific subclass enables performance benefit by allowing
     * calls to a [set()][.set] function that takes the primitive
     * `float` type and avoids autoboxing and other overhead associated with the
     * `Float` class.
     *
     * @param <T> The class on which the Property is declared.
    </T> */
    abstract class FloatProperty<T>(name: String) : Property<T, Float>(Float::class.java, name) {

        /**
         * A type-specific override of the [.set] that is faster when dealing
         * with fields of type `float`.
         */
        abstract fun setValue(t: T, value: Float)

        override fun set(t: T, value: Float?) {
            setValue(t, value!!)
        }
    }

    companion object {

        val SATURATION: Property<ObservableColorMatrix, Float> = object : FloatProperty<ObservableColorMatrix>("saturation") {

            override fun setValue(cm: ObservableColorMatrix, value: Float) {
                cm.setSaturation(value)
            }

            override fun get(cm: ObservableColorMatrix): Float {
                return cm.getSaturation()
            }
        }
    }
}