package ru.practicum.ewm.stats.model;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

public final class ActionTypeWeight {

    public static final float VIEW = 0.4f;
    public static final float REGISTER = 0.8f;
    public static final float LIKE = 1.0f;

    private ActionTypeWeight() {
        throw new AssertionError();
    }

    public static float from(final ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> ActionTypeWeight.VIEW;
            case REGISTER -> ActionTypeWeight.REGISTER;
            case LIKE -> ActionTypeWeight.LIKE;
        };
    }
}
