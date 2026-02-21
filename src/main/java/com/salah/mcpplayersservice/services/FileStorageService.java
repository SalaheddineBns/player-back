package com.salah.mcpplayersservice.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

	private final Path uploadDir;

	public FileStorageService(@Value("${media.upload-dir:uploads}") String uploadDirPath) {
		this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
	}

	@PostConstruct
	public void init() {
		try {
			Files.createDirectories(uploadDir);
		}
		catch (IOException ex) {
			throw new RuntimeException("Could not create upload directory", ex);
		}
	}

	public String storeFile(MultipartFile file, UUID playerId) {
		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}
		String uniqueFilename = playerId + "_" + UUID.randomUUID() + extension;
		try {
			Path targetLocation = uploadDir.resolve(uniqueFilename);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return targetLocation.toString();
		}
		catch (IOException ex) {
			throw new RuntimeException("Could not store file " + uniqueFilename, ex);
		}
	}

	public void deleteFile(String filePath) {
		try {
			Files.deleteIfExists(Paths.get(filePath).toAbsolutePath().normalize());
		}
		catch (IOException ex) {
			throw new RuntimeException("Could not delete file " + filePath, ex);
		}
	}

	public Path getFilePath(String filePath) {
		return Paths.get(filePath).toAbsolutePath().normalize();
	}

}
