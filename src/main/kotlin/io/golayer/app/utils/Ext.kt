package io.golayer.app.utils

import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

const val MAIL_REGEX = ("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$")

fun String.isEmailValid(): Boolean = !this.isNullOrBlank() && Regex(MAIL_REGEX).matches(this)

const val SHEET_REGEX = ("^((?>(?>'[\\w\\s]+')|(?>[\\w\\s]+))(?>![A-Z]{1}[0-9]{1})?(?>:[A-Z]{1}[0-9])?)")
fun String.isSharedElementValid(): Boolean = !this.isNullOrBlank() && Regex(SHEET_REGEX).matches(this)

fun String.concatRand() = this + (Math.random() * 1000).toLong()


/*
    Memoize implementation backed with a Guava Cache MAp
    Expiry Policy 1 Minute
 */
fun <T, R> ((T) -> R).memoize(): (T) -> R = Memoize1(this)

class Memoize1<in T, out R>(val f: (T) -> R) : (T) -> R {
    private val values =
            CacheBuilder.newBuilder()
//                    .refreshAfterWrite(1, TimeUnit.MINUTES)
                    .expireAfterAccess(1, TimeUnit.MINUTES)
                    .build<T, R>()

    override fun invoke(x: T): R {
        return values.get(x) {
            f(x)
        }
    }
}




