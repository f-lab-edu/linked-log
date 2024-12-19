package flab.Linkedlog.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryStates {
    ACTIVE("active"),
    DELETED("deleted");

    private final String state;
    
    public static CategoryStates fromState(String state) {
        for (CategoryStates cs : CategoryStates.values()) {
            if (cs.getState().equals(state)) {
                return cs;
            }
        }
        throw new IllegalArgumentException("Invalid category state: " + state);
    }
}
