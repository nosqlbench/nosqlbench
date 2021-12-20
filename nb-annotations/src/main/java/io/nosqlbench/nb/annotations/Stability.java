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
public enum Stability {
    Experimental,
    Settling,
    Stable,
    Unspecified
}
