# Magic Bus

Example microproject demonstrating intraprocess messaging.

## Bus types

* [Simple](src/main/java/hm/binkley/SimpleMagicBus.java) - simple, synchronous internal implementation.  Orders message handlers that supertypes receive posts before subtypes
* [Reactor](src/main/java/hm/binkley/ReactorMagicBus.java) - synchronous implementation using [Reactor IO](http://projectreactor.io/)

## Creating a bus

```java
import hm.binkley.MagicBus;
import hm.binkley.MagicBus.FailedMessage;
import hm.binkley.MagicBus.ReturnedMessage;

final Consumer<ReturnedMessage> returned = message -> {...};
final Consumer<FailedMessage> failed = message -> {...};
final MessageBus bus = new MessageBus(returned, failed);
```

### Ignoring returned or failed messages

```java
import static hm.binkley.MagicBus.discard;

final MessageBus bus = new MessageBus(discard(), discard());
```

## Subscribing to messages

```java
final Mailbox<MyType> mailbox = message -> {...};
bus.subscribe(MyType.class, mailbox);
```

### Spying on all messages

```java
bus.subscribe(Object.class, System.out::println);
```

### Unsubscribing to messages

```java
bus.unsubscribe(MyType.class, mailbox);
```

## Publishing messages

```java
bus.publish(new MyType());
```
