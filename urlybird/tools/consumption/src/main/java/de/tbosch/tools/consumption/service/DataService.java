package de.tbosch.tools.consumption.service;

import java.util.List;

import de.tbosch.tools.consumption.model.Entry;

public interface DataService {

	void addData(String dateText, String valueText);

	List<Entry> readAllData();

}