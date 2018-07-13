package dk.magenta.datafordeler.core.util;

import java.time.OffsetDateTime;
import java.util.Comparator;

public class BitemporalityComparator implements Comparator<Bitemporality> {

    public enum Type {
        REGISTRATION_FROM,
        REGISTRATION_TO,
        EFFECT_FROM,
        EFFECT_TO,
        ALL
    }

    private Type type;

    public BitemporalityComparator(Type type) {
        this.type = type;
    }

    private static final Comparator<OffsetDateTime> nullsFirst = Comparator.nullsFirst(OffsetDateTime.timeLineOrder());
    private static final Comparator<OffsetDateTime> nullsLast = Comparator.nullsLast(OffsetDateTime.timeLineOrder());

    private static final Type[] comparisonOrder = new Type[] {Type.REGISTRATION_FROM, Type.REGISTRATION_TO, Type.EFFECT_FROM, Type.EFFECT_TO};

    @Override
    public int compare(Bitemporality o1, Bitemporality o2) {
        if (this.type == Type.ALL) {
            int c;
            for (Type t : comparisonOrder) {
                if (t != Type.ALL) {
                    c = this.compareSingle(o1, o2, t);
                    if (c != 0) {
                        return c;
                    }
                }
            }
        }
        return this.compareSingle(o1, o2, this.type);
    }

    private int compareSingle(Bitemporality o1, Bitemporality o2, Type type) {
        switch (type) {
            case REGISTRATION_FROM:
                return nullsFirst.compare(o1.registrationFrom, o2.registrationFrom);
            case REGISTRATION_TO:
                return nullsLast.compare(o1.registrationTo, o2.registrationTo);
            case EFFECT_FROM:
                return nullsFirst.compare(o1.effectFrom, o2.effectFrom);
            case EFFECT_TO:
                return nullsLast.compare(o1.effectTo, o2.effectTo);
        }
        return 0;
    }

    public static final BitemporalityComparator ALL = new BitemporalityComparator(Type.ALL);
    public static final BitemporalityComparator REGISTRATION_FROM = new BitemporalityComparator(Type.REGISTRATION_FROM);
    public static final BitemporalityComparator REGISTRATION_TO = new BitemporalityComparator(Type.REGISTRATION_TO);
    public static final BitemporalityComparator EFFECT_FROM = new BitemporalityComparator(Type.EFFECT_FROM);
    public static final BitemporalityComparator EFFECT_TO = new BitemporalityComparator(Type.EFFECT_TO);

    public static final Comparator<Bitemporality> EFFECT = Comparator.nullsFirst(new BitemporalityComparator(BitemporalityComparator.Type.EFFECT_FROM))
                                                            .thenComparing(Comparator.nullsLast(new BitemporalityComparator(BitemporalityComparator.Type.EFFECT_TO)));


}
