# Dynafig

Example microproject demonstrating dynamically updated key-value pairs.

The integrations with other libraries is not well-tested.  They are more
explorations of those APIs, not battle-hardened code.

(NB - this file is GitHub-flavored MarkDown.  The IntelliJ plugin does not
correctly render though _class_ links in Preview do work, _method_ links do
not.)

## TODO

1. Javadoc.
2. Default wiring implementations with common remote stores, e.g., JSR107 or
Cassandra.
3. Spring:
  * Factory for `Optional<Atomic*>`
  * Connect `@Inject @Named(key)` to `track*`
4. Cloud environment source for non-git.
5. Update Spring `@Configuration` with constraints.

## Interfaces

### Tracking

[`Tracking`](dynafig-core/src/main/java/lab/dynafig/Tracking.java) is a
factory for key-value pairs.  The tracking calls take a `String` key name and
return a non-`null` `Optional<Atomic*>` (`*` can be `AtomicBoolean`,
`AtomicInteger` or `AtomicReference`).  The optional is empty if the key is
missing.

Typical use looks like, e.g.:

```
private final AtomicInteger rapidity;

public MerryGoRound(@Nonnull final Tracking settings) {
   rapidity = settings.trackInt("app.rapidity").
       orElseThrow(() -> new IllegalStateException(
               "No property for 'app.rapidity'"));
}

public void spin() {
   final int rapidity = this.rapidity.get();
   // Use rapidity
}
```

Or can use a callback variant, e.g.:

```
public MerryGoRound(@Nonnull final Tracking settings) {
  // Spin at different rate when setting is updated
  settings.trackInt("app.rapidity", (key, rapidity) -> spin(rapidity));
}
```

The callback is initially invoked with the current value for the key, and
reinvoked as the key is updated.

The four tracking choices are:

* [`track`](dynafig-core/src/main/java/lab/dynafig/Tracking.java#L55)
  tracks plain string values returning `Optional<AtomicReference<String>>`
* [`trackBool`](dynafig-core/src/main/java/lab/dynafig/Tracking.java#L84)
  tracks boolean values returning `Optional<AtomicBoolean>`
* [`trackInt`](dynafig-core/src/main/java/lab/dynafig/Tracking.java#L111)
  tracks int values returning `Optional<AtomicInteger>`
* [`trackAs`](dynafig-core/src/main/java/lab/dynafig/Tracking.java#L141)
  tracks values of type `S` given a conversion function, returning
  `Optional<AtomicReference<S>>`

### Updating

[`Updating`](dynafig-core/src/main/java/lab/dynafig/Updating.java) updates
pair values:

* [`update(key,value)`](dynafig-core/src/main/java/lab/dynafig/Updating.java#L23)
  updates a pair value or throws `IllegalArgumentException` if `key` is
  undefined (TODO: should it create a new pair instead?)
* [`update(entry)`](dynafig-core/src/main/java/lab/dynafig/Updating.java#L33)
  is a convenience to update from a map entry
* [`updateAll`](dynafig-core/src/main/java/lab/dynafig/Updating.java#L45)
  is a convenience to update a set of key-value in one call

## Default implementation

[`Default`](dynafig-core/src/main/java/lab/dynafig/Default.java) is a
default implementation of `Tracking` and `Updating`,
[`DefaultTest`](dynafig-core/src/test/java/lab/dynafig/DefaultTest.java)
tests it

## Integrations

* [Apache ZooKeeper](dynafig-zookeeper/src/main/java/lab/dynafig/zookeeper/ZookeeperListener)
* TODO: Netflix Archaius2
* TODO: JCache
* [Spring Boot](dynafig-spring/src/main/java/lab/dynafig/spring/DynafixAutoConfiguration)
