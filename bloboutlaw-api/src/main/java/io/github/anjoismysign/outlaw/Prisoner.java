package io.github.anjoismysign.outlaw;

import java.util.List;

public interface Prisoner {

    List<Conviction> convictions();

    default long getAggregateSentence() {
        return convictions().stream()
                .mapToLong(Conviction::getTerm)
                .sum();
    }

}
