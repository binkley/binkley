# Operate Support Interface - Logging

## Summary

Operational concerns are first-class concerns in enterprise systems and
require the same level of planning and execution as other architectural
concerns.  This is captured in an Operate Support Interface (OSI), analogous
to an Application Programming Interface (API).

## Background

Classic enterprise systems delivery focuses on business concerns first and
foremost.  In these processes build cycle follows a waterfall or phased
pattern: Imagine, Design, Construct, Integrate, Operate.  Each phase is
backward-facing for inputs and forward-facing for outputs in the sense that
requirements flow in one direction only.

_Linear diagram of the 5 phases_

We know this is not the best way to deliver value and change.  Modern software
development has moved to Agile and Continuous Deployment to address the
mismatch between traditional methods and fluid business needs.  Delaying
inputs and outputs between phases is costly and produces poor outcomes.
Rather each area should inform and be informed by others.

_Non-linear diagram - A hexagram diagram showing the interrelations_

Operate and support and equal partners in this view.  Software that cannot be
run effectively or efficiently fails to deliver value.  A strong support model
is a significant selling point for off-the-shelf systems, even when such
systems are inferior to bespoke solutions.

Thus systems must be designed and built to consider operational concerns on
par with other concerns.  Further, most enterprise funding models separate the
capital expenses in developing new systems from the operating expenses in
running them.  Once a system is live, its owner and stakeholders may change
considerably.

## Logging

A key feature in live systems is logging.  Meant here is logging is a general
sense, including alerting that traditionally would notify an operator at the
console.

Most systems treat logging as a single concern largely backward-facing towards
developers and to some extent problem investigation and resolution.  Much or
most logging outputs are relics of the construction phase with little
relevance to other concerns.  Yet other conerns are woven or layered into
logging as a universal solution.

These mingled concerns might include:

- Alerting, including capacity, performance, security and problem detection
- Historic capture of same
- Security, authentication, authorization and access controls
- Real-time and post hoc problem analysis and resolution
- Audit trails, including analytics and compliance
- Integration with other systems
- Maintenance and further development

Logging as a solution ranges in value from fairly good to adequate to poor,
and is akin to screen-scraping as a general purpose tool.  Mingling unrelated
concerns lowers the value of logging for any single concern.

Yet logging remains a valuable solution when applied appropriately.  To do so
requires a two-pronged approach.  First is to separate concerns that they may
be addressed independently.  This is largely a change in mindset.  Second is
to use logging appropriately when applicable, and equally importantly, avoid
logging if it is not the best solution.

## Separating Concerns

The right view of logging is as an important part of a larger whole, the
Operate Support Interface (OSI).  Generally Operations and Support are the
product owners of the OSI with key stakeholders in business domains.  Example
stakeholders include accounting, compliance or business forecasting.
Separating concerns means addressing product owners and stakeholders
separately, and balancing their needs within systems.

Three concerns of the OSI deserve special attention.

### Alerting

This is the most urgent concern of the OSI.  Failing systems in production are
expensive in multiple ways.  The system itself is no longer providing business
value.  It is impacting other systems which may in turn themselves fail.  And
it consumes valuable human resources in problem detection and resolution.  The
key point for logging in failing systems:

Failing systems should alert as quickly and loudly as possible.

The longer a failing system remains unnoticed, the greater the risk of adverse
outcomes.

### Audting

This is the most important concern of the OSI.  A failing system can be
intentionally ignored if the impact is judged low enough.  Having no record of
business activity may have little short-term impact but can have massive
impact on the bottom line.

The best example of this is regulatory reporting.  Failure to record key
business events in a timely manner can lead regulators to impose massive
fines, and in the worst case shutter the business.  The key point for
maintaining an audit trail:

Audit trails should be as complete and durable as possible.

Interestingly this does not require they be as fast or effecient as possible
and audting often follows a strongly different architecture than other logging
concerns.

### Application

Silence is Golden.  This rule is as true for systems as it is for children.
Essentially live systems should have no extraneous logging if they are running
normally, and become very noisy when malfunctioning.  The Boy Who Cried Wolf
is another apt analogy.  Noisy systems train users to ignore logging.

Maintenance and development concerns often make the majority of logging, yet
are a non-concern during actual operation.  Logging should reflect this, with
non-production logging avoided or eliminated.  Likewise, it should be easy to
re-enable such logging during development and testing.  The key point for
developers is:

Non-production logging should be kept to a minimum in production.

## Recommendations

### Log streams

The key means to avoid mingling concerns is to avoid combining the logging of
those concerns.  The most obvious way to do that is for concerns to have
dedicate logging streams configured and managed separately.  Each stream
should reflect the needs of that concern.  Examples:

#### Audit trail

Audit trails should use guaranteed delivery to durable storage.  If logging
fails the application should ALERT operate immediately.  Example
implementations include JMS or logging to database.  AUDIT logging stream
should be synchronous to guarantee delivery and ensure better order
preservation.  This stream may also be forked to provide a local on-disk
record for regulatory reasons.

#### Alerting

Alerting should use guaranteed delivery to a monitoring system.  This might be
an automated system with escalantion to manual intervention.  Example
implementations include JMS or an external alerting API to handle escalation,
alert fan-outs, etc.  ALERT logging stream should be synchronous to guarantee
delievery.

#### Operate Runbook

Audit trail and alerts should be documented in the Operate Runbook.  One
approach is to use a custom javadoc tag, e.g., `@alert` or `@runbook`, however
this is difficult to link to externally.  Another is to reference a common
data source for systems and operate to share.

Capture alert details in a common, shared format among systems, suitable for
consolidation.  Give known audit and alert events a known identifier that may
be cross-referenced against the runbook.  Capture enough information in
messages that operate may diagnose a likely resolution, or know to quickly
escalate.

An example format in YAML.  Note alert messages are suitable for use with
[`MessageFormat`](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html):

```yaml
errors:
  your-system-name:
    - some-problem:
      code: 1
      message: >
        ({0}) Widget '{1}' missing {2} wheel: {3}.  Please replace and
        restart.
    - some-other-problem:
      code: 2
      message: >
        ({0}) Widget '{1}' on fire: {2}.  Escalate to support team.
```

One drawback to `MessageFormat` is parameters are indexed, not named.

_Needs discussion and follow up._

#### Other

Application logging should use the lowest-impact means available for recording
logging events, typically asynchronous batched writes to local disk.  However,
unlike traditional logging, OSI should have a greatly reduced volume of
logging events for this stream, lessing system impact of logging.

### Log levels

ALERT - Special level requesting immediate operate attention.  Uses the ALERT
logging stream.  Within that stream using WARN and ERROR makes sense.  levels.

AUDIT - Special level contributing to the audit trail.  Uses the AUDIT logging
stream.  Within that stream using INFO, WARN and ERROR levels makes sense.

ERROR - All errors should produce an ALERT.  The point of an error message is
something is failing and needs immediate attention, automated or manual.
Either ERROR logging is a synonym for ALERT, or duplicates the ALERT logging
stream.  Business related events should send ERROR events to both AUDIT and
ALERT streams.  Operate or support events should send only to the ALERT
stream.

WARN - Avoid using warnings.  They are by definition ambiguous.  Is the system
failing?  Does it need immediate attention?  Prefer INFO or ERROR.

_More discussion needed - WARN audit events make sense, but it better
handled by a consumer with BI.  However WARN alert events are problematic._

INFO - Developers should use INFO to traditional logging streams.  This is the
usual level for non-production logging.

DEBUG - Generally avoid.  Either it is worth logging as INFO or it is not
worth logging at all.  To filter the logging use filters, not levels.  For
developers needing finer grained logging, rely on tags or similar to
seggregate logging events rather than level.

TRACE - Oddly this is preferable to DEBUG in some respects.  It provides the
greatest possible number of logging events, and hence the most complete
picture of system behavior.  This level should never be enabled by default,
and should only be enabled through run-time changes, e.g. JMX, for limited
periods.

| Stream | Level | Example | Notes |
|--------|-------|---------|-------|
| ALERT | ERROR | Failed database connection | Operate condition requiring immediate attention |
| ALERT | WARN | - | Use sparingly, if at all |
| ALERT | Other | - | Alert supports no other levels |
| AUDIT | ERROR | Failed login[1] | Auditable event requiring remediation |
| AUDIT | WARN | Rejected customer as per business rules | Normal level for problematic auditable events |
| AUDIT | INFO | Normal business events | Normal level for auditable events |
| AUDIT | Other | Audit supports no other levels |
| APPLICATION | ERROR | Failed database connection | Always use together with ALERT-ERROR |
| APPLICATION | WARN | - | Use sparingly, if at all |
| APPLICATION | INFO | Application start | Normal level for non-alert/audit events |
| APPLICATION | DEBUG | - | Use sparingly, if at all |
| APPLICATION | TRACE | Function calls | Use together with debugging |

[1] OSI implementation may choose to separate security events from audit
events

### Logging configuration

Logging concerns should be implemented separately from other concerns.  Some
logging implementations take the approach of excess flexibility on the
assumption the implemention could be swapped out for another.  This is
unnecessary if the logging implementation supports integration with multiple
producers and consumers.  Rather the logging implementation should be a fixed
part of a system as a framework.

Given a fixed implementation, logging configuration should be common among
systems with optional extension points for system-specific features.  However
such extension points should be used sparingly.

In general systems should produce the same logging for similar concerns and
logging events that adhere to common business and operate needs.  Ultimately
systems run within the same business and operate processes and share
organizational concerns.

### Logging testing and monitoring

Treating Operate and Support as first-class concerns means treating logging
with the same care as business and architecture concerns.  Logging should be
tested as any other part of a system is tested.  If a business event should
produce a certain audit trail, confim this with tests.

Similarly test logging extension points and implemention of other features.

Suitable OSI logging implementations need to support debugging of logging,
configuration, monitoring and extension points.

### Example Implementation with Logback

Java is the most common enterprise programming language.  Among the libraries
for logging, Logback is the best combination of popular, well-supported and
featureful.  Every recommendation is supported by Logback directly or through
simple extension.

On Github is an example implementation of OSI logging with Logback:

[binkley's Blog (Brian Oxley) - logging](https://github.com/binkley/binkley/tree/develop/logging)

### Logback support of recommendations

- Use of fixed, shared, parameterized logging configuration with extension
  points through a common `logback.xml` in the classpath, including run-time
  warnings if systems provide a custom `logback.xml` ignoring the common one.

- Custom logging streams, ALERT and AUDIT, supporting optional logging levels,
  e.g., INFO, WARN, ERROR.  The streams are independent and should be
  redirected appropriately such as to JMS, JDBC or an alerting system.

- Global and custom logging levels appropriate to environment with a suitable
  default "Silence is Golden" approach for typical system logging.  Also
  changeable both at start with a system property, e.g., `log.level`, and
  dynamically at runtime through JMX or programmatically.

- Additional extension points such as duplicate filters to prevent ALERT
  spamming and programmable filters for custom logging stream routing (this is
  how AUDIT and ALERT can be implemented).  This includes 3rd-party
  extensions such as Whisper.

- Bundled support for logging to JMS, JDBC, SMTP and Syslog.  Other
  3rd-parties provide integration, e.g., [Logstash
  (ELK)](https://blog.codecentric.de/en/2014/10/log-management-spring-boot-applications-logstash-elastichsearch-kibana/).

- Synchronous and asynchronous handling of logging events.  AUDIT and ALERT
  events are best synchronous to require timely delivery.  Other logging
  streams are best asynchronous to lower system impact.

- Log event markers to categorize more accurately than by originating Java
  class (used by AUDIT and ALERT streams).  Logging configuration can filter
  and route based on markers.

- Monitoring of logging implementation through JMX, including tracking of
  recent logging events, changing of logging levels.  Combined with Jolokia,
  this provides further remote management options.

- Use of a system property to enable/disable debugging of logging
  configuration, e.g., `-Dlogback.debug` on the command line.  The same flag
  can be used to set the developer logging stream to DEBUG level.

- Tracing of logging during development with the
  [`XLogger`](http://www.slf4j.org/extensions.html#extended_logger) facility.

- Logback itself can receive remote logging events and act as a consolidation
  service.  This is a suitable approach for combining logging for a master
  overview of multiple systems.  Other approaches may be more suitable.
