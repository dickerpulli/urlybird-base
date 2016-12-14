package suncertify.network.sockets;

import java.io.Serializable;

/**
 * The response of one request send before.
 */
public class Result implements Serializable {

    /** serial id. */
    private static final long serialVersionUID = 1L;

    /** The record numbers to send. */
    private int[] recNos;

    /** The data to send. */
    private String[] data;

    /** The record number to send. */
    private int recNo;

    /** The cookie to send. */
    private long cookie;

    /** If an exception occurs at the server the client will get it. */
    private Exception exception;

    /**
     * @return the recNos
     */
    public int[] getRecNos() {
	return recNos;
    }

    /**
     * @param recNos
     *            the recNos to set
     */
    public void setRecNos(int[] recNos) {
	this.recNos = recNos;
    }

    /**
     * @return the data
     */
    public String[] getData() {
	return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(String[] data) {
	this.data = data;
    }

    /**
     * @return the recNo
     */
    public int getRecNo() {
	return recNo;
    }

    /**
     * @param recNo
     *            the recNo to set
     */
    public void setRecNo(int recNo) {
	this.recNo = recNo;
    }

    /**
     * @return the cookie
     */
    public long getCookie() {
	return cookie;
    }

    /**
     * @param cookie
     *            the cookie to set
     */
    public void setCookie(long cookie) {
	this.cookie = cookie;
    }

    /**
     * @return the exception
     */
    public Exception getException() {
	return exception;
    }

    /**
     * @param exception
     *            the exception to set
     */
    public void setException(Exception exception) {
	this.exception = exception;
    }

}
