This is an implementation of a B-Tree in Java. It supports put(), get(), 
remove(), ceiling() and Iterators.

The persistence layer (FileNodeProvider) is rather simple and not ready
for production. There are a bunch of things missing such as managing the
number of pages that are kept in memory, lock coupling (or crabbing) for
efficient concurrent access, shadowing, cloning...

Some things can be added relatively easily but others get very tricky.
In particular the way Iterators work needs to be changed.

Vision: A full blown implementation of Rodeh's B-Tree in Java. These
B-Trees are Copy-on-Write (CoW) friendly and are the magic ingredient 
behind ZFS and Btrfs. 