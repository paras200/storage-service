package com.origin.storage.service;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.origin.storage.exception.FileStorageException;
import com.origin.storage.exception.MyFileNotFoundException;
import com.origin.storage.model.FileMetadata;
import com.origin.storage.model.UploadFileResponse;
import com.origin.storage.property.FileStorageProperties;
import com.origin.storage.repo.FileMetaDataRepository;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, long date) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename()+date+"."+FilenameUtils.getExtension(file.getOriginalFilename()));

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
    
    @Autowired
	private FileMetaDataRepository repository;
    
    public FileMetadata storeMetadata(FileMetadata metadata) {
    	//log.info(" retreive ID for saved file :" + metadata);
		return repository.save(metadata);
    }
    
    public Optional<FileMetadata> getFileById(String id) {
		return repository.findById(id);
    }

    public UploadFileResponse convertToPDF(MultipartFile htmlFile) throws FileNotFoundException, IOException, DocumentException {
		Date date = new Date();
		//TODO save the document, set all responses from the upload file method and then save the file, return the path for demo purposes
		String fileName = "Certificate-"+date+".pdf";
		PDDocument createDocument = new PDDocument();     
		createDocument.addPage(new PDPage()); 
		createDocument.save("./uploads/"+fileName); 
		createDocument.close();
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("./uploads/"+fileName));
		document.open();
		FileMetadata metadata = new FileMetadata();
		metadata.setDate(date);
		//metadata.setUserId(userId);
		metadata.setName(htmlFile.getOriginalFilename()+date);
		metadata = storeMetadata(metadata);
		XMLWorkerHelper.getInstance().parseXHtml(writer, document, new BufferedInputStream(htmlFile.getInputStream()));
		document.close();
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
				.path(fileName).toUriString();
		return new UploadFileResponse(metadata.id, fileName, fileDownloadUri, null, 0);
	}
    
}
