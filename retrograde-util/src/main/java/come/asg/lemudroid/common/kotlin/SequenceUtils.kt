package come.asg.lemudroid.common.kotlin

fun <T> lazySequenceOf(vararg producers: () -> T): Sequence<T> {
    return producers.asSequence()
        .map { it() }
}
