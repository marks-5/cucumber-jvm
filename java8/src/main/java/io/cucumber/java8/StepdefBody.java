package io.cucumber.java8;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface StepdefBody {
    @FunctionalInterface
    interface A0 extends StepdefBody {
        void accept() throws Throwable;
    }
    @FunctionalInterface
    interface A1<T1> extends StepdefBody {
        void accept(T1 p1) throws Throwable;
    }
    @FunctionalInterface
    interface A2<T1, T2> extends StepdefBody {
        void accept(T1 p1, T2 p2) throws Throwable;
    }
    @FunctionalInterface
    interface A3<T1, T2, T3> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3) throws Throwable;
    }
    @FunctionalInterface
    interface A4<T1, T2, T3, T4> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4) throws Throwable;
    }
    @FunctionalInterface
    interface A5<T1, T2, T3, T4, T5> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5) throws Throwable;
    }
    @FunctionalInterface
    interface A6<T1, T2, T3, T4, T5, T6> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5, T6 p6) throws Throwable;
    }
    @FunctionalInterface
    interface A7<T1, T2, T3, T4, T5, T6, T7> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5, T6 p6, T7 p7) throws Throwable;
    }
    @FunctionalInterface
    interface A8<T1, T2, T3, T4, T5, T6, T7, T8> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5, T6 p6, T7 p7, T8 p8) throws Throwable;
    }
    @FunctionalInterface
    interface A9<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends StepdefBody {
        void accept(T1 p1, T2 p2, T3 p3, T4 p4, T5 p5, T6 p6, T7 p7, T8 p8, T9 p9) throws Throwable;
    }
}
