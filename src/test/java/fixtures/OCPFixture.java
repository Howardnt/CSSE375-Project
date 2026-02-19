package fixtures;

public class OCPFixture {
    /**
     * Fixtures for the OpenClosedPrinciple check:
     * - Methods with too many type checks (>= 3)
     * - Structurally rigid classes with type-branching
     * - Control cases that should NOT be flagged
     */

// Should be flagged for structural rigidity: concrete, no interfaces, no superclass, has type-branching
    class OCPRigidClass {

        // Should be flagged: 3 instanceof checks
        public String processShape(Object shape) {
            if (shape instanceof Circle) {
                return "circle";
            } else if (shape instanceof Square) {
                return "square";
            } else if (shape instanceof Triangle) {
                return "triangle";
            }
            return "unknown";
        }

        // Should NOT be flagged: only 2 instanceof checks (below method threshold)
        public String processTwoShapes(Object shape) {
            if (shape instanceof Circle) {
                return "circle";
            } else if (shape instanceof Square) {
                return "square";
            }
            return "unknown";
        }
    }

    // Should NOT be flagged for structural rigidity: implements an interface
    class OCPClassWithInterface implements Runnable {

        // Would normally trigger method check (3 instanceof), but class is not rigid
        public void run() {
            Object obj = new Object();
            if (obj instanceof Circle) {
                System.out.println("circle");
            } else if (obj instanceof Square) {
                System.out.println("square");
            } else if (obj instanceof Triangle) {
                System.out.println("triangle");
            }
        }
    }

    // Should NOT be flagged for structural rigidity: extends a non-Object class
    class OCPClassWithSuperclass extends OCPRigidClass {

        public String processMore(Object shape) {
            if (shape instanceof Circle) {
                return "circle";
            } else if (shape instanceof Square) {
                return "square";
            } else if (shape instanceof Triangle) {
                return "triangle";
            }
            return "unknown";
        }
    }

    // Should NOT be flagged for structural rigidity: abstract class
    abstract class OCPAbstractClass {

        public String processShape(Object shape) {
            if (shape instanceof Circle) {
                return "circle";
            } else if (shape instanceof Square) {
                return "square";
            } else if (shape instanceof Triangle) {
                return "triangle";
            }
            return "unknown";
        }
    }

    // Should be flagged for structural rigidity: final class with type-branching
    final class OCPFinalClass {

        // 2 instanceof checks - enough to trigger rigidity check
        public String processShape(Object shape) {
            if (shape instanceof Circle) {
                return "circle";
            } else if (shape instanceof Square) {
                return "square";
            }
            return "unknown";
        }
    }

    // Should NOT be flagged: concrete class with no extension points but NO type checks
    class OCPCleanClass {

        public int add(int a, int b) {
            return a + b;
        }

        public String greet(String name) {
            return "Hello, " + name;
        }
    }

    // Should be flagged for method violation: uses instanceof AND switch (3 total type checks)
    class OCPMixedTypeChecks {

        public String process(Object obj, int type) {
            if (obj instanceof Circle) {
                return "circle";
            } else if (obj instanceof Square) {
                return "square";
            }
            switch (type) {
                case 1: return "one";
                default: return "other";
            }
        }
    }

    // Should NOT be flagged: equals method with instanceof is explicitly skipped
    class OCPEqualsClass {

        private int value;

        public OCPEqualsClass(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OCPEqualsClass) {
                if (obj instanceof Runnable) {
                    if (obj instanceof Cloneable) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    // Dummy types used for instanceof checks above
    class Circle {}
    class Square {}
    class Triangle {}
}
