package com.origin.storage.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.itextpdf.text.DocumentException;
import com.origin.storage.model.FileDataAsJson;
import com.origin.storage.model.FileMetadata;
import com.origin.storage.model.UploadFileResponse;
import com.origin.storage.service.ExcelToJsonCoverter;
import com.origin.storage.service.FileStorageService;

@SuppressWarnings("deprecation")
@RestController
@CrossOrigin(origins = "*")
public class FileController {

	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

	@Autowired
	private FileStorageService fileStorageService;

	@PostMapping("/uploadFile")
	public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
		Date date = new Date();
		FileMetadata metadata = new FileMetadata();
		String fileName = fileStorageService.storeFile(file, date.getTime());
		metadata.setDate(date);
		metadata.setName(file.getOriginalFilename()+date);
		metadata = fileStorageService.storeMetadata(metadata);
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
				.path(fileName).toUriString();

		return new UploadFileResponse(metadata.id, fileName, fileDownloadUri, file.getContentType(), file.getSize());
	}
	
	@PostMapping("/uploadFile/{userId:.+}")
	public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file, @PathVariable String userId, HttpServletRequest request) {
		Date date = new Date();
		FileMetadata metadata = new FileMetadata();
		String fileName = fileStorageService.storeFile(file, date.getTime());
		metadata.setDate(date);
		metadata.setUserId(userId);
		metadata.setName(file.getOriginalFilename()+date.getTime()+FilenameUtils.getExtension(file.getOriginalFilename()));
		metadata = fileStorageService.storeMetadata(metadata);
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
				.path(fileName).toUriString();

		return new UploadFileResponse(metadata.id, fileName, fileDownloadUri, file.getContentType(), file.getSize());
	}

	@PostMapping("/uploadMultipleFiles")
	public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
		return Arrays.asList(files).stream().map(file -> uploadFile(file)).collect(Collectors.toList());
	}

	@GetMapping("/downloadFile/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		// Load file as Resource
		Resource resource = fileStorageService.loadFileAsResource(fileName);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.info("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@GetMapping("/downloadFileAsString/{fileName:.+}")
	public String getFileContentAsString(@PathVariable String fileName, HttpServletRequest request) throws IOException {
		Resource resource = fileStorageService.loadFileAsResource(fileName);

		return IOUtils.toString(resource.getInputStream(), "UTF-8");
	}

	@GetMapping("/getFileAsJson/{id:.+}")
    public FileDataAsJson getFileAsJson(@PathVariable String id, HttpServletRequest request) throws IOException {
		return ExcelToJsonCoverter.creteJSONAndTextFileFromExcel("./uploads/"+fileStorageService.getFileById(id).get().getName());
    }
	
	@GetMapping("/getFileAsJsonbyName/{fileName:.+}")
    public FileDataAsJson getFileAsJsobyNamen(@PathVariable String fileName, HttpServletRequest request) throws IOException {
		return ExcelToJsonCoverter.creteJSONAndTextFileFromExcel("./uploads/"+fileName);
    }
	@PostMapping("/generatePDF")
    public UploadFileResponse generatePDF(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException, DocumentException {
		return fileStorageService.convertToPDF(file);
    }
	
}
