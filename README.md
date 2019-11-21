# SDIS_ELECTION


Leader election is a basic building block in many fault-tolerant distributed systems. The goal of this project is to evaluate one of the protocols proposed in [1], [2], [3] and [4]. This evaluation may be done via either an experimental study or simulation.

For an example of the metrics that may be useful to consider in this evaluation, you may find it interesting to take a look at [4]. You should discuss these metrics with other groups that will evaluate other leader election protocols.

If you choose to implement the algorithm, in your evaluation you can use processes 1) on the same computer, 2) on different virtual machines on the same computer, 3) on different computers. Because all three settings are likely to be rather aseptic, you may wish to use some layer that artificially adds delays and/or drops messages. It is likely that you'll be able to find some one else's code that provides these capabilities. If you get this far, I suggest that you discuss this with other groups that may need similar code.

References
[1] Vasudevan, S.; Kurose, J.; Towsley, D., "Design and analysis of a leader election algorithm for mobile ad hoc networks," Network Protocols, 2004. ICNP 2004. Proceedings of the 12th IEEE International Conference on , vol., no., pp.350,360, 5-8 Oct. 2004

[2] Ingram, R.; Shields, P.; Walter, J.E.; Welch, J.L., "An asynchronous leader election algorithm for dynamic networks," Parallel & Distributed Processing, 2009. IPDPS 2009. IEEE International Symposium on , vol., no., pp.1,12, 23-29 May 2009

[1] Fernandez, A.; Jimenez, E.; Raynal, M., "Eventual Leader Election with Weak Assumptions on Initial Knowledge, Communication Reliability, and Synchrony," Dependable Systems and Networks, 2006. DSN 2006. International Conference on , vol., no., pp.166,178, 25-28 June 2006

[4] Christof Fetzer, Flaviu Cristian, "A Highly Available Local Leader Election Service," IEEE Transactions on Software Engineering, vol. 25, no. 5, pp. 603-618, September/October, 1999
