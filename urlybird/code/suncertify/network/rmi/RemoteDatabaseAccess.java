package suncertify.network.rmi;

import java.rmi.Remote;

import suncertify.db.DatabaseAccess;

/**
 * The interface for the remote RMI database access.
 */
public interface RemoteDatabaseAccess extends Remote, DatabaseAccess {

}
