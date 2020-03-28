---
title: state functions
weight: 30
---

Functions in the state category allow you to do things with side-effects in the function flow. Specifically, they allow
you to save or load values of named variables to thread-local registers. These work best when used with non-async
activities, since the normal statement grouping allows you to share data between statements in the sequence. It is not
advised to use these with async activities.

When using these functions, be careful that you call them when needed. For example, if you have a named binding which
will save a value, that action only occurs if some statement with this named binding is used.

For example, if you have an account records and transaction records, where you want to save the account identifier to
use within the transaction inserts, you must ensure that each account binding is used within the thread first.




