package com.coreoz.plume.file.db;

import javax.inject.Inject;

import org.junit.runner.RunWith;

import com.carlosbecker.guice.GuiceModules;
import com.carlosbecker.guice.GuiceTestRunner;
import com.coreoz.plume.file.db.querydsl.database.FileDaoDatabaseQuerydsl;

@RunWith(GuiceTestRunner.class)
@GuiceModules(FileTestDbModule.class)
public class FileDaoDatabaseQuerydslTest extends FileDaoTest {

	@Inject
	private FileDaoDatabaseQuerydsl fileDao;

	@Override
	protected FileDao fileDao() {
		return fileDao;
	}

}