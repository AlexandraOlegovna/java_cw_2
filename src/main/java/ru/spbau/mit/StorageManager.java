package ru.spbau.mit;

import java.util.function.Function;
import java.util.function.Supplier;

public interface StorageManager {
    <R> LazyValue<R> createLazyValue(Supplier<R> supplier);
    <T, R> MemoizedFunction<T, R> createMemoizedFunction(Function<T, R> function);

    /** Для задания №3 */
    <R> LazyValue<R> createLazyValue(Supplier<R> supplier, Supplier<R> onRecursion);
    <T, R> MemoizedFunction<T, R> createMemoizedFunction(Function<T, R> function, Function<T, R> onRecursion);
}

/**
 * Задание 1 (2 балла). Реализовать интерфейсы (см. комментарии к ru.spbau.mit.LazyValue и ru.spbau.mit.MemoizedFunction)
 *  - все интерфейсы должны быть thread-safe
 *  - с одним и тем же объектом ru.spbau.mit.StorageManager должно быть можно работать нескольким потокам одновременно
 *  - помните, что возвращаемые значения (`R`) могут быть null
 *
 * Задание 2. (1 балл). Сделайте так, чтобы могло происходить параллельно несколько вычислений по разным аргументам
 * для ru.spbau.mit.MemoizedFunction
 *
 * Задание 3. (1 балл). Поддержите расширенный интерфейс `ru.spbau.mit.StorageManager` с обработкой рекурсивных вызовов.
 * Семантика следующая: если вычисление вызывает непосредственно само себя рекурсивно, то в качестве результата
 * этого вычисления используется значение соответствующего вызова аргумента `onRecursion`.
 * Если `onRecursion == null`, то бросается `ru.spbau.mit.RecursiveComputationException`.
 */