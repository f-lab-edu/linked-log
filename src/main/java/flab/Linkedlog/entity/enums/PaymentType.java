package flab.Linkedlog.entity.enums;

public enum PaymentType {
    CASH("현금"), CARD("카드"), NORMAL("일반");

    private String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentType fromString(String value) {
        for (PaymentType type : PaymentType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid payment type: " + value);
    }
}
