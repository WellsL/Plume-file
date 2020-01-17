package com.coreoz.plume.file.services.file;

import com.carlosbecker.guice.GuiceModules;
import com.carlosbecker.guice.GuiceTestRunner;
import com.coreoz.plume.file.db.FileDaoDatabase;
import com.coreoz.plume.file.db.querydsl.database.FileDaoDatabaseQuerydsl;
import com.coreoz.plume.file.db.querydsl.beans.FileEntryDatabase;
import com.coreoz.plume.file.services.cache.FileCacheService;
import com.coreoz.plume.file.services.cache.FileCacheServiceGuava;
import com.coreoz.plume.file.services.configuration.FileConfigurationService;
import com.coreoz.plume.file.services.file.data.FileData;
import com.coreoz.plume.file.services.hash.ChecksumServiceSha1;
import com.google.common.cache.LoadingCache;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceTestRunner.class)
@GuiceModules(FileTestModule.class)
public class FileServiceDatabaseTest {

	@Inject
	private FileConfigurationService configurationService;

	@Inject
	private FileCacheServiceGuava fileCacheService;

	@Inject
	private ChecksumServiceSha1 checksumService;

	// testing url(Long fileId)

	@Test
	public void url__should_return_empty_if_id_file_is_null() {
		FileServiceDatabase fileService = new FileServiceDatabase(null, null, null, configurationService, fileCacheService);

		assertThat(fileService.url(null)).isEmpty();
	}

	@Test
	public void url__should_return_use_file_data_cache_if_present() {
		FileServiceDatabase fileService = new FileServiceDatabase(null, null, null, configurationService, new FileCacheService() {
			@SuppressWarnings("unchecked")
			@Override
			public LoadingCache<String, FileData> newFileDataCache(Function<String, FileData> loadingData) {
				return new LoadingCacheTest<String, FileData>(fileUid -> {
					if("efaaeb68-f973-11e8-8eb2-f2801f1b9fd1".equals(fileUid)) {
						return FileData.of(1L,
							"efaaeb68-f973-11e8-8eb2-f2801f1b9fd1",
							"file.ext",
							null,
							null,
							null,
							null);
					}

					return null;
				});
			}

			@Override
			public LoadingCache<String, String> newFileUrlCache(Function<String, String> loadingData) {
				return null;
			}
		});

		assertThat(fileService.url("efaaeb68-f973-11e8-8eb2-f2801f1b9fd1")).hasValue("/api/files/efaaeb68-f973-11e8-8eb2-f2801f1b9fd1/file.ext");
	}

	@Test
	public void url__should_load_file_name_from_dao() {
		FileServiceDatabase fileService = new FileServiceDatabase(
			fileDaoMock(), null, null, configurationService, fileCacheService
		);

		assertThat(fileService.url("846c36cc-f973-11e8-8eb2-f2801f1b9fd1")).hasValue("/api/files/846c36cc-f973-11e8-8eb2-f2801f1b9fd1/file.ext");
	}

	@Test
	public void url__should_return_raw_url_if_file_name_is_null() {
		FileServiceDatabase fileService = new FileServiceDatabase(
			fileDaoMock(), null, null, configurationService, fileCacheService
		);

		assertThat(fileService.url("7b3cf3de-f973-11e8-8eb2-f2801f1b9fd1")).hasValue("/api/files/7b3cf3de-f973-11e8-8eb2-f2801f1b9fd1");
	}

	@Test
	public void url__should_return_empty_if_no_file_exists() {
		FileServiceDatabase fileService = new FileServiceDatabase(
			fileDaoMock(), null, null, configurationService, fileCacheService
		);

		assertThat(fileService.url("efaaeb68-f973-11e8-8eb2-f2801f1b9fd1")).isEmpty();
	}

	// testing fetch(Long fileId)

	@Test
	public void fetch__should_load_file_from_dao() {
		FileServiceDatabase fileService = new FileServiceDatabase(
			fileDaoMock(), null, checksumService, configurationService, fileCacheService
		);

		assertThat(fileService.fetch("846c36cc-f973-11e8-8eb2-f2801f1b9fd1").map(FileData::getFilename)).hasValue("file.ext");
	}

	@Test
	public void fetch__should_return_empty_if_no_file_exists() {
		FileServiceDatabase fileService = new FileServiceDatabase(
			fileDaoMock(), null, checksumService, configurationService, fileCacheService
		);

		assertThat(fileService.fetch("efaaeb68-f973-11e8-8eb2-f2801f1b9fd1")).isEmpty();
	}

	// utils

	private FileDaoDatabase fileDaoMock() {
		return new FileDaoDatabaseQuerydsl(null) {
			@Override
			public String fileName(String fileUid) {
				if("846c36cc-f973-11e8-8eb2-f2801f1b9fd1".equals(fileUid)) {
					return "file.ext";
				}
				if("7b3cf3de-f973-11e8-8eb2-f2801f1b9fd1".equals(fileUid)) {
					return "";
				}
				return null;
			}
			@Override
			public FileEntryDatabase findByUid(String uid) {
				if("846c36cc-f973-11e8-8eb2-f2801f1b9fd1".equals(uid)) {
					return FileEntryDatabase.of(
						5L,
						"846c36cc-f973-11e8-8eb2-f2801f1b9fd1",
						"file.ext",
						null,
						null
					);
				}
				return null;
			}
		};
	}

}
