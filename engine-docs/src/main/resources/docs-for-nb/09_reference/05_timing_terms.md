---
title: Timing Terms
---

# Timing Terms

Often, terms used to describe latency can create confusion.
In fact, the term  _latency_ is so overloaded in practice that it is not useful by itself. Because of this, DSBench will avoid using the term latency _except in a specific way_. Instead, the terms described in this section will be used.

DSBench is a client-centric testing tool. The measurement of operations occurs on the client, without visibility to what happens in transport or on the server. This means that the client *can* see how long an operation takes, but it *cannot see* how much of the operational time is spent in transport and otherwise. This has a bearing on the terms that are adopted with DSBench.

Some terms are anchored by the context in which they are used. For latency terms, *service time* can be subjective. When using this term to describe other effects in your system, what is included depends on the perspective of the requester. The concept of service is universal, and every layer in a system can be seen as a service. Thus, the service time is defined by the vantage point of the requester. This is the perspective taken by the DSBench approach for naming and semantics below.

## responsetime

**The duration of time a user has to wait for a response from the time they submitted the request.** Response time is the duration of time from when a request was expected to start, to the time at which the response is finally seen by the user. A request is generally expected to start immediately when users make a request. For example, when a user enters a URL into a browser, they expect the request to start immediately when they hit enter.

In DSBench, the response time for any operation can be calculated by adding its wait time and its the service time together.

## waittime
 
**The duration of time between when an operation is intended to start and when it actually starts on a client.** This is also called *scheduling delay* in some places. Wait time occurs because clients are not able to make all requests instantaneously when expected. There is an ideal time at which the request would be made according to user demand. This ideal time is always earlier than the actual time in practice. When there is a shortage of resources *of any kind* that delays a client request, it must wait.

Wait time can accumulate when you are running something according to a dispatch rate, as with a rate limiter.

## servicetime

**The duration of time it takes a server or other system to fully process to a request and send a response.** From the perspective of a testing client, the _system_ includes the infrastructure as well as remote servers. As such, the service time metrics in DSBench include any operational time that is external to the client, including transport latency.

