package service;

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
	
	private static final String UPLOAD_DIR = "src/main/resources/static/uploads/products/";
	private static final String PUBLIC_PATH = "/uploads/products/";
	
	public FileStorageService() {
		try {
			Path uploadPath = Paths.get(UPLOAD_DIR);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not create upload directory", e);
		}
	}
	
	public String storeFile(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			return null;
		}
		
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null) {
			return null;
		}
		
		String extension = "";
		int lastDotIndex = originalFilename.lastIndexOf('.');
		if (lastDotIndex > 0) {
			extension = originalFilename.substring(lastDotIndex).toLowerCase();
		}
		
		if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
			throw new IllegalArgumentException("Only image files are allowed (jpg, jpeg, png, gif, webp)");
		}
		
		String filename = UUID.randomUUID().toString() + extension;
		Path targetLocation = Paths.get(UPLOAD_DIR + filename);
		
		Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
		
		return PUBLIC_PATH + filename;
	}
	
	public void deleteFile(String filePath) {
		if (filePath == null || filePath.isEmpty() || !filePath.startsWith(PUBLIC_PATH)) {
			return;
		}
		
		try {
			String filename = filePath.substring(PUBLIC_PATH.length());
			Path filePathToDelete = Paths.get(UPLOAD_DIR + filename);
			Files.deleteIfExists(filePathToDelete);
		} catch (IOException e) {
			System.err.println("Error deleting file: " + filePath);
		}
	}
}

