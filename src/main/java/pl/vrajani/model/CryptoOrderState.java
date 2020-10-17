package pl.vrajani.model;

public enum CryptoOrderState {
    CANCELED("Canceled"),
    FILLED("Filled"),
    REJECTED("Rejected"),
    PARTIALLY_FILLED("Partially Filled"),
    UNKNOWN("unknown");


    private final String state;

    CryptoOrderState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public static CryptoOrderState getState(String state){
        switch (state.toLowerCase()){
            case "canceled" :
                return CryptoOrderState.CANCELED;
            case "filled" :
                return CryptoOrderState.FILLED;
            case "partially filled" :
                return CryptoOrderState.PARTIALLY_FILLED;
            case "rejected" :
                return CryptoOrderState.REJECTED;
            default:
                return CryptoOrderState.UNKNOWN;
        }
    }
}
