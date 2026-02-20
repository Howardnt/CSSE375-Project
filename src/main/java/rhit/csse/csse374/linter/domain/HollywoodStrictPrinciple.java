package rhit.csse.csse374.linter.domain;

/**
 * Wrapper class strictly for GUI compatibility.
 * Exposes the Strict Hollywood Strategy to the GUI scanner.
 */
public class HollywoodStrictPrinciple extends HollywoodPrinciple {

    public HollywoodStrictPrinciple() {
        super(new StrictHollywoodStrategy());
    }

    @Override
    public String name() {
        return "Hollywood Principle (Strict Tolerance)";
    }
}
