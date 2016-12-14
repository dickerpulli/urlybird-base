package de.tbosch.tools.consumption.service;

import java.text.NumberFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.tbosch.tools.consumption.dao.generic.EntryDao;
import de.tbosch.tools.consumption.model.Entry;

@Service
@Transactional
public class StandardDataService implements DataService {

	private static final Log LOG = LogFactory.getLog(StandardDataService.class);

	@Autowired
	private EntryDao entryDao;

	/**
	 * @see de.tbosch.tools.consumption.service.DataService#addData(java.lang.String, java.lang.String)
	 */
	@Override
	public void addData(String dateText, String valueText) {
		if (StringUtils.isNotBlank(dateText) && StringUtils.isNotBlank(valueText)) {
			LocalDate date;
			int value;
			try {
				date = DateTimeFormat.forPattern("dd.MM.yyyy").parseLocalDate(dateText);
				value = NumberFormat.getIntegerInstance().parse(valueText).intValue();
			} catch (Exception e) {
				LOG.error("data is not formatted correctly ", e);
				return;
			}
			entryDao.create(new Entry(date, value));
		} else {
			LOG.debug("empty string where transmitted as data");
		}
	}

	/**
	 * @see de.tbosch.tools.consumption.service.DataService#readAllData()
	 */
	@Override
	public List<Entry> readAllData() {
		return entryDao.findAll();
	}

}
