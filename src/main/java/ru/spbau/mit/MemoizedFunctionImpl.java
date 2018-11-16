package ru.spbau.mit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MemoizedFunctionImpl<T, R> implements MemoizedFunction<T, R> {

    private final ConcurrentHashMap<T, LazyValue<R>> values = new ConcurrentHashMap<>();
    private final Function<T, R> function;
    private volatile LazyValueImpl<R> forNull = null;

    public MemoizedFunctionImpl(Function<T, R> func) {
        function = func;
    }

    /**
     * Запрашивает значение функции в точке {@param argument}
     * <p>
     * 1. Если значение функции в точке не было вычислено, то вызвавший `apply` поток используется для
     * вычисления этого значения.
     * <p>
     * 2. Если некоторый другой поток вычисляет значение функции в точке `arg'`, то вызвавший `get` поток
     * блокируется до тех пор, пока не произойдет одно из трех событий:
     * - вычисление значения завершено (возможно, аварийно)
     * - было выполнено прерывание данного потока.
     * - произошел spurious wake-up
     * Обратите внимание, что для первого задания не требуется реализовывать т.н. fine-grained locking, т.е.
     * возможность нескольким потоком параллельно вычислять значения в нескольких точках.
     * Обратите внимание и на то, что от вас это потребуется сделать в следующем задании.
     * <p>
     * 3. Вызов `get` может завершиться тремя различными способами:
     * - Если вычисление значения в точке было успешно и вернуло объект X, то этот и все последующие вызовы
     * `get` закончатся именно возвращением того же самого X.
     * - Если вычисление значения бросило RuntimeException e, то этот и все последующие вызовы `get`
     * должны бросать тот же самый `e`
     * - Если вычисление было прервано (Thread.interrupt), то *данный* вызов `get` бросает InterruptedException.
     * Обратите внимание, что данный случай не гарантирует ничего относительно последующих вызовов `get`.
     *
     * @param argument
     */

    public LazyValue<R> getValue(T argument) {
        if (argument == null) {
            if (forNull == null)
                forNull = new LazyValueImpl<>(() -> function.apply(argument));
            return forNull;
        }
        return values.computeIfAbsent(argument, arg -> new LazyValueImpl<>(() -> function.apply(arg)));
    }

    @Override
    public R apply(T argument) throws RecursiveComputationException, InterruptedException {
        return getValue(argument).get();
    }

    @Override
    public boolean isComputedAt(T argument) {
        return getValue(argument).isReady();
    }
}