package rhit.csse.csse374.linter.domain;

/**
 * Wrapper class strictly for GUI compatibility.
 * Exposes the Threshold Hollywood Strategy to the GUI scanner.
 */
public class HollywoodThresholdPrinciple extends HollywoodPrinciple {

    public HollywoodThresholdPrinciple() {
        super(new ThresholdHollywoodStrategy(3));
    }

    @Override
    public String name() {
        return "Hollywood Principle (Threshold 3)";
    }
}
