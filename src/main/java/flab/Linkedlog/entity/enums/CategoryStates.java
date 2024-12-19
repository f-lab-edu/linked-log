package flab.Linkedlog.entity.enums;

public enum CategoryStates {
    ACTIVE("active"),
    DELETED("deleted");

    private String state;

    CategoryStates(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public static CategoryStates fromState(String state) {
        for (CategoryStates cs : CategoryStates.values()) {
            if (cs.getState().equals(state)) {
                return cs;
            }
        }
        throw new IllegalArgumentException("Invalid category state: " + state);
    }
}
