package suncertify.gui;

/**
 * The connection mode of the client application. For the client it is possible
 * to connect to a server in remote mode or to connect to a local database.
 */
public enum ConnectionMode {

    /** The connection mode to a local database. */
    LOCAL,

    /** The connection mode to a remote database server. */
    REMOTE

}
