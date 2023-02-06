### Activity Inputs

Each activity has an input that controls which cycles it will run.
By default, the input is a simple interval that dispatches
cycle ranges to activity threads, starting at some cycle number
and ending at another. Example of this type of input include:

    # cycle 0 through cycle 4, 5 cycles total
    cycles=0..5

    # cycle 0 through cycle 9999999
    # When the interval start is left off, "0.." is assumed.
    cycles=10M

    # cycle 1000000000 through cycle 4999999999
    cycles=1000G..5000G

However, there are other ways to feed an activity. All inputs are
modular within the nosqlbench runtime. To see what inputs are
available, you can simpy run:

    ${PROG} --list-input-types

Any input listed this way should have its own documentation.
