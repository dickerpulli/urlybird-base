package suncertify.network.sockets;

import java.io.Serializable;

/**
 * The container for the request.
 */
public class Command implements Serializable {

    /** serial id. */
    private static final long serialVersionUID = 1L;

    /**
     * The type of request.
     */
    public enum RequestType {
	READ, CREATE, FIND, DELETE, UPDATE
    }

    /** The type of this request. */
    private RequestType requestType;

    /** The record number to send. */
    private int recNo;

    /** The data to send. */
    private String[] data;

    /** The cookie to send. */
    private long cookie;

    /**
     * @return the requestType
     */
    public RequestType getRequestType() {
	return requestType;
    }

    /**
     * @param requestType
     *            the requestType to set
     */
    public void setRequestType(RequestType requestType) {
	this.requestType = requestType;
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

}
