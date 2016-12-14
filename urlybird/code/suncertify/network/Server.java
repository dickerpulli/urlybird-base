package suncertify.network;

/**
 * The interface for any server activities.
 */
public interface Server {

    /**
     * Starts the server.
     */
    public abstract void start();

    /**
     * Stops the server.
     */
    public abstract void stop();

    /**
     * Checks if the server is running.
     * 
     * @return <code>true</code> if the server is running.
     */
    public abstract boolean isRunning();

}