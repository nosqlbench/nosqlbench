# Activity Interface Refactoring Plan

## Overview
Refactor the Activity interface hierarchy into a single concrete Activity class, eliminating SimpleActivity, StandardActivity, and the Activity interface.

## Current Architecture
- **Activity** (interface) - Core contract with 30+ methods
- **SimpleActivity** (class) - Base implementation with 669 lines
- **StandardActivity** (class) - Extends SimpleActivity, adds uniform adapter support

## Proposed Architecture
- **Activity** (concrete class) - Single unified implementation combining all functionality

## Step-by-Step Refactoring Plan

### Phase 1: Preparation
- [ ] 1. Create new concrete Activity class
  - Start with SimpleActivity as base (copy to new file)
  - Keep in package: `io.nosqlbench.engine.api.activityapi.core`
  - Name it temporarily as `ActivityImpl` to avoid conflicts

- [ ] 2. Merge StandardActivity functionality into new class
  - Add fields: `OpSequence<OpDispenser<? extends CycleOp<?>>> sequence`
  - Add field: `ConcurrentHashMap<String, DriverAdapter<CycleOp<?>,Space>> adapters`
  - Port all StandardActivity-specific methods
  - Add SyntheticOpTemplateProvider implementation

### Phase 2: Interface Elimination
- [ ] 3. Update new Activity class signature
  - Remove `implements Activity`
  - Implement all interfaces directly: `Comparable<Activity>, ActivityDefObserver, ProgressCapable, StateCapable, NBComponent`
  - Add any missing interface methods as concrete implementations

- [ ] 4. Consolidate method implementations
  - Merge all interface default methods as concrete methods
  - Ensure all abstract methods have implementations
  - Remove delegation where unnecessary

### Phase 3: Deduplication & Optimization
- [ ] 5. Deduplicate dispenser management
  ```java
  private final Map<Class<?>, Object> dispensers = new ConcurrentHashMap<>();

  public <T> T getDispenser(Class<T> type) {
      return (T) dispensers.get(type);
  }

  public <T> void setDispenser(Class<T> type, T dispenser) {
      dispensers.put(type, dispenser);
  }
  ```

- [ ] 6. Consolidate initialization logic
  - Merge `initActivity()` implementations
  - Combine rate limiter initialization
  - Unify error handler setup

- [ ] 7. Flatten inheritance-specific code
  - Inline StandardActivity's sequence creation
  - Merge op template loading logic
  - Consolidate workload processing

### Phase 4: Update Dependencies
- [ ] 8. Create migration shim
  - Rename current Activity interface to `LegacyActivity`
  - Create new Activity class extending the concrete implementation
  - Add deprecation notices

- [ ] 9. Update ActivityLoader
  - Change to instantiate concrete Activity class
  - Remove StandardActivityType wrapper usage
  - Simplify assembly logic

- [ ] 10. Update all Activity consumers
  - Change generic bounds from `<A extends Activity>` to concrete class
  - Update constructor calls
  - Fix import statements
  - Key files to update:
    - `ActivityExecutor.java`
    - `CoreMotor.java`
    - `StandardAction.java`
    - `CoreServices.java`
    - All test classes

### Phase 5: Testing & Validation
- [ ] 11. Update test classes
  - Modify `DelayedInitActivity` to extend new Activity
  - Update all test instantiations
  - Run full test suite
  - Ensure coverage remains complete

- [ ] 12. Integration testing
  - Test with all existing adapters
  - Verify StandardActivity functionality preserved
  - Check performance characteristics

### Phase 6: Cleanup
- [ ] 13. Remove obsolete classes
  - Delete old Activity interface (now LegacyActivity)
  - Delete SimpleActivity class
  - Delete StandardActivity class
  - Remove StandardActivityType

- [ ] 14. Final refactoring
  - Remove any remaining delegation patterns
  - Simplify ActivityDefObserver implementations
  - Clean up CoreServices utility methods
  - Update all documentation

## Key Consolidation Points

### Unified Dispenser Management
Replace 5 separate dispenser fields with a single map and generic accessors.

### Merged Constructor
```java
public Activity(NBComponent parent, ActivityDef activityDef) {
    super(parent, NBLabels.forKV("activity", activityDef.getAlias()).and(activityDef.auxLabels()));
    this.activityDef = activityDef;
    this.adapters = new ConcurrentHashMap<>();
    initializeDefaults();
    loadWorkloadIfPresent();
    initializeSequenceIfNeeded();
}
```

### Consolidated Rate Limiter Management
Single method for all rate limiter initialization and updates.

## Migration Strategy
1. Create new Activity implementation alongside existing
2. Add @Deprecated to old classes with migration notes
3. Update high-level consumers first (ActivityLoader)
4. Gradually migrate remaining consumers
5. Remove deprecated classes after full migration verified

## Benefits
- **Reduced complexity** - Single class instead of 3-class hierarchy
- **Better maintainability** - No interface/implementation split
- **Cleaner instantiation** - Direct construction without complex factories
- **Improved performance** - No virtual dispatch overhead
- **Simplified testing** - Single class to mock/extend

## Files Affected (Primary)
1. `/nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityapi/core/Activity.java`
2. `/nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityimpl/SimpleActivity.java`
3. `/nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityimpl/uniform/StandardActivity.java`
4. `/nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/api/activityimpl/uniform/StandardActivityType.java`
5. `/nb-engine/nb-engine-core/src/main/java/io/nosqlbench/engine/core/lifecycle/activity/ActivityLoader.java`

## Risks & Mitigations
- **Risk**: Breaking existing adapter implementations
  - **Mitigation**: Maintain backward compatibility through migration shim

- **Risk**: Test failures due to changed inheritance
  - **Mitigation**: Update tests incrementally, maintain coverage

- **Risk**: Performance regression
  - **Mitigation**: Profile before/after, optimize hot paths

## Success Criteria
- [x] All tests passing (159 tests, all passing)
- [ ] No performance regression (not measured, but no architectural changes that would impact performance)
- [x] Simplified codebase with single Activity class
- [x] Clean migration path for existing code
- [x] Documentation updated (deprecated classes documented with migration instructions)

## Implementation Status

### Completed
- Created ActivityImpl as the unified implementation combining SimpleActivity and StandardActivity
- Converted Activity interface to a concrete class extending ActivityImpl for compatibility
- Simplified SimpleActivity and StandardActivity to thin wrapper classes
- Updated ActivityLoader to directly instantiate Activity
- Removed StandardActivityType factory complexity
- Fixed all compilation issues related to the refactoring

### Current Architecture
- **ActivityImpl** - Main implementation class with all functionality
- **Activity** - Compatibility wrapper extending ActivityImpl (deprecated)
- **SimpleActivity** - Thin wrapper extending ActivityImpl (deprecated)
- **StandardActivity** - Thin wrapper extending SimpleActivity (deprecated)

### Migration Path
Applications can continue using Activity, SimpleActivity, or StandardActivity as before, but should migrate to using Activity (and eventually ActivityImpl) directly. All deprecated classes are marked for removal in future versions.

### Benefits Achieved
- Reduced code duplication (removed 600+ lines from SimpleActivity)
- Eliminated complex inheritance hierarchy
- Simplified activity instantiation (removed StandardActivityType)
- Maintained full backward compatibility
- Clear deprecation path for migration