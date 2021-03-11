## Quick Start Project for the Chat - server

Simple Maven Project which can be used for the Chat-CA 

Using this project as your start code will make deploying your server (the jar-file) to Digital Ocean a "no brainer" if you follow the instructions given here

https://docs.google.com/document/d/1aE1MlsTAAYksCPpI4YZu-I_uLYqZssoobsmA-GHmiHk/edit?usp=sharing


GROUP C5 - Kasper, Nicklas, Kristoffer

Active droplet server ip: 46.101.246.201


DESIGN DESCRIPTION:
- We worked based on the project we'd developed with Daniel in his classes. It contains a client utilising threads to handle input and output separately, so we don't get locked down by blocking-calls. To this end, our server handles the clients using a Vector list, as this utilises the 'Synchronised' identifier to ensure that Race Condition doesn't occur, when we have several active clients connected.
- It is a bit difficult to say a lot about our design choices, aside from this, as we were tasked to follow the project structure to the letter. We therefore didn't add anything that we weren't told to add, aside from the log, which seemed to have been redacted as a requirement sometime during the week.

WHO DID WHAT:
- Kris spent a lot of time Wednesday figuring out problems with the droplet. Kasper and Nicklas did basically all the UnitTesting. But aside from that, all three of us mostly coded everything together using code-with-me.

ACCEPTANCE TESTS:
- 