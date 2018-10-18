package de.betzen.wordclock;


class Constants {
    static final int WORDCLOCK_UDP_PORT = 1234;    //fixed port the Wordclock is listening on for UDP discovery broadcasts by this app
    static final String WORDCLOCK_ECHO_MESSAGE = "Wordclock Handshake";  //discovery message send via UDP broadcast
    static final String BROADCAST_ACTION = "de.betzen.wordclock.BROADCAST_ACTION";
    static final String UDP_RECEIVER_STRING = "de.betzen.wordclock.UDP_RECEIVER_STRING";
    static final String MULTICAST_DUMP = "de.betzen.wordclock.MULTICAST_DUMP";
}
