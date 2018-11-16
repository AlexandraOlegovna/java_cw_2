package ru.spbau.mit;

import java.util.function.Supplier;

public class LazyValueImpl<R> implements LazyValue<R> {

    private Supplier<R> function;
    private R value;
    private volatile Exception exception;
//    private boolean isRunning;
    private volatile boolean hasAnswer;


    public LazyValueImpl(Supplier<R> func) {
        function = func;
//        isRunning = false;
        hasAnswer = false;
    }

    /**
     * Запрашивает значение.
     * <p>
     * 1. Если значение еще не было вычислено, то вызвавший `get` поток используется для вычисления значения.
     * <p>
     * 2. Если значение находится в процессе вычисления другим потоком, то вызвавший `get` поток блокируется
     * до тех пор, пока не произойдет одно из трех событий:
     * - вычисление значения завершено (возможно, аварийно)
     * - было выполнено прерывание данного потока.
     * - произошел spurious wake-up
     * <p>
     * 3. Вызов `get` может завершиться тремя различными способами:
     * - Если вычисление значения было успешно, то данное значение возвращается. Если на какой-то вызов `get`
     * было возвращено значение X, то гарантируется, что все последующие вызовы `get` закончатся именно возвращением
     * того же самого X.
     * - Если вычисление значения бросило RuntimeException e, то все вызовы `get` должны бросать тот же самый `e`
     * - Если вычисление было прервано (Thread.interrupt), то *данный* вызов `get` бросает InterruptedException.
     * Обратите внимание, что данный случай не гарантирует ничего относительно последующих вызовов `get`.
     */
    @Override
    public synchronized R get() throws RecursiveComputationException, InterruptedException {
        if (hasAnswer && exception == null) {
            return value;
        }
        throwException();

        try {
            value = function.get();
        } catch (Exception e) {
            exception = e;
        }
        hasAnswer = true;

        throwException();

        return value;
    }

    private void throwException() throws InterruptedException {
        if (exception != null) {
            if (exception instanceof RuntimeException)
                throw (RuntimeException) exception;
            if (exception instanceof InterruptedException)
                throw (InterruptedException) exception;
        }
    }

    @Override
    public boolean isReady() {
        return hasAnswer;
    }
}
