package dae.me.resource.domain;

public enum Container {
	HEADER_HEIGHT(Configuration.HEIGHT_APP * Configuration.HEADER_HEIGHT_PERCENTAGE),
	BODY_HEIGHT(Configuration.HEIGHT_APP - (Configuration.HEIGHT_APP * Configuration.HEADER_HEIGHT_PERCENTAGE));
	
	private final double value;

    private Container(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
