package fr.insee.rmes.model.rbac;

public enum Strategy {
    ALL, STAMP;

    public static Strategy merge(Strategy strategy1, Strategy strategy2) {
        if (strategy1 != null) {
            return strategy1.merge(strategy2);
        }
        return strategy2;
    }

    private Strategy merge(Strategy other) {
        return switch (other) {
            case ALL -> ALL;
            case STAMP -> this;
            case null -> this;
        };
    }

    public boolean isAllStampAuthorized() {
        return this == ALL;
    }
}
