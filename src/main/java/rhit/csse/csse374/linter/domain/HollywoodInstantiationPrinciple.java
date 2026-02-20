package rhit.csse.csse374.linter.domain;

/**
 * Wrapper class strictly for GUI compatibility.
 * Exposes the Instantiation-Only Hollywood Strategy to the GUI scanner.
 */
public class HollywoodInstantiationPrinciple extends HollywoodPrinciple {

    public HollywoodInstantiationPrinciple() {
        super(new InstantiationOnlyStrategy());
    }

    @Override
    public String name() {
        return "Hollywood Principle (Instantiation Only)";
    }
}
