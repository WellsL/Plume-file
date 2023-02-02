package com.coreoz.plume.file.webservices;

import com.coreoz.plume.file.service.FileDownloadJerseyService;
import com.coreoz.plume.file.service.configuration.FileDownloadConfigurationService;
import com.coreoz.plume.jersey.security.permission.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.util.Optional;

@Path("/files")
@Tag(name = "files", description = "Serve binary resources")
@Singleton
@PublicApi
public class FileWs {

	private final FileDownloadJerseyService fileDownloadService;
	private final long maxAgeCacheInSeconds;
	private final boolean keepOriginalNameOnDownload;
	private final int fileUidMinimumLength;

	@Inject
	public FileWs(
		FileDownloadJerseyService fileDownloadService,
		FileDownloadConfigurationService config
	) {
		this.fileDownloadService = fileDownloadService;

		this.maxAgeCacheInSeconds = config.fileCacheControlMaxAge().getSeconds();
		this.keepOriginalNameOnDownload = config.keepOriginalNameOnDownload();
		this.fileUidMinimumLength = config.fileUidMinimumLength();
	}

	@GET
	@Path("/{fileUniqueName}")
	@Operation(description = "Serve a file")
	public Response fetch(
		@Parameter(required = true) @PathParam("fileUniqueName") String fileUniqueName,
		@HeaderParam(HttpHeaders.IF_NONE_MATCH) String ifNoneMatchHeader
	) {
		if (fileUniqueName.length() < fileUidMinimumLength) {
			return Response.status(Status.NOT_FOUND).build();
		}

		return this.fileDownloadService
			.fetchMetadata(fileUniqueName)
			.flatMap(fileMetadata -> {
				if (ifNoneMatchHeader != null && ifNoneMatchHeader.equals(fileMetadata.getChecksum())) {
					return Optional.of(Response.notModified().build());
				}

				return this.fileDownloadService.fetchData(fileUniqueName)
					.map(fileData -> {
						ResponseBuilder response = Response.ok(fileData);

						// Adding checksum in etag to enable client basic caching
						response.header(HttpHeaders.ETAG, fileMetadata.getChecksum());

						if (fileMetadata.getMimeType() != null) {
							response.header(HttpHeaders.CONTENT_TYPE, fileMetadata.getMimeType());
						}

						if(maxAgeCacheInSeconds > 0) {
							response.header(
								HttpHeaders.CACHE_CONTROL,
								"public, max-age=" + maxAgeCacheInSeconds
							);
						}
						if (keepOriginalNameOnDownload && fileMetadata.getFileOriginalName() != null) {
							response
								.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getFileOriginalName() + "\"");
						}
						return response.build();
					});
			})
			.orElseGet(() -> Response.status(Status.NOT_FOUND).build());
	}
}
