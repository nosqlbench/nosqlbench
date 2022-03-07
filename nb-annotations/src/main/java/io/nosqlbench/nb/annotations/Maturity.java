package io.nosqlbench.nb.annotations;

/**
 * This is a way for NoSQLBench code and driver maintainers to communicate about the maturity of various components.
 * There are no specific criteria for these designations for now. Developers should use their bet judgement about which
 * to assign to any component. Guidelines to follow:
 *
 * <ul>
 *     <li>If the component is new, and hasn't had external user testing, it should be marked as Experimental.</li>
 *     <li>If the component is being used by users or testers, it may be marked as Settling, but should not be marked as stable.</li>
 *     <li>If the component has been in use for several releases, has user feedback about the quality and a low number of issues file specifically against it,
 *     then it may be marked as Stable.</li>
 *     <li>Components which are marked stable should not be modified in place except for carefully compartmentalized and tested changes. If this is needed,
 *     then a new component selector under a different name should be used for those changes so that users and historic tests are not affected.</li>
 *     <li>By default, the runtime will mark a component as Unspecified unless/until the maturity field is set of its {@link Service} annotation.</li>
 * </ul>
 */
public enum Maturity {
    Stable(6,"Proven to work in practice and has has little to no change since"),
    Proven(5, "Proven to work in practice"),
    Verified(4, "Tested by a user and verified to function according to expectations"),
    Tested(1, "Is tested regularly with unit tests"),
    Unspecified(0, "The developer needs to be more opinionated about the status of this component"),
    Deprecated(-1, "This component is no loner recommended for general use"),
    Experimental(-2, "This component is provided as an experiment which may mature to something more reliable"),
    Sketch(-3,"The component is presented for consideration, and not intended to be promoted without community validation"),
    Any(-100,"Any component will match this, but no component should be assigned it");

    private final int stability;
    private final String description;

    Maturity(int stability, String description) {
        this.stability = stability;
        this.description = description;
    }

    public int getStability() {
        return stability;
    }
    public String getDescription() {
        return this.description;
    }

    public boolean equalOrGreater(Maturity reference) {
        return this.stability >= reference.stability;
    }
}
