package com.vider.quantum.engine.controller;

import com.google.api.services.drive.model.File;
import com.vider.quantum.engine.manager.FileManager;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/vider/quantum/api/drive")
@AllArgsConstructor
@Slf4j
public class GoogleDriveController {
	private FileManager fileManager;

	@GetMapping({"/"})
	public ResponseEntity<List<File>> listEverything() throws IOException, GeneralSecurityException {
		List<File> files = fileManager.listEverything();
		return ResponseEntity.ok(files);
	}

	@GetMapping({"/list","/list/{parentId}"})
	public ResponseEntity<List<File>> list(@PathVariable(required = false) String parentId) throws IOException, GeneralSecurityException {
		List<File> files = fileManager.listFolderContent(parentId);
		return ResponseEntity.ok(files);
	}

	@GetMapping("/download/{id}")
	public void download(@PathVariable String id, HttpServletResponse response) throws IOException, GeneralSecurityException {
		fileManager.downloadFile(id, response.getOutputStream());
	}

	@GetMapping("/export/{id}/{exportType}")
	public void export(@PathVariable String id, @PathVariable String exportType, HttpServletResponse response) throws IOException, GeneralSecurityException {
		fileManager.exportFile(id, exportType, response.getOutputStream());
	}

	@GetMapping("/content/{id}")
	public ResponseEntity<Object> getFileAsJson(@PathVariable String id) throws IOException, GeneralSecurityException {
		Object fileAsJson = fileManager.getFileAsJson(id);
		return new ResponseEntity<>(fileAsJson, HttpStatus.OK);
	}

	@GetMapping("/directory/create")
	public ResponseEntity<String> createDirecory(@RequestParam String path) throws Exception {
		String parentId = fileManager.getFolderId(path);
		return ResponseEntity.ok("parentId: "+parentId);
	}

	@PostMapping(value = "/upload",
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE} )
	public ResponseEntity<String> uploadSingleFileExample4(@RequestBody MultipartFile file,@RequestParam(required = false) String path) throws IOException {
		log.info("Request contains, File: " + file.getOriginalFilename());
		String fileId = fileManager.uploadFile(file.getBytes(), file.getOriginalFilename(), file.getContentType(), path);
		if(fileId == null){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok("Success, FileId: "+ fileId);
	}


	@GetMapping("/delete/{id}")
	public void delete(@PathVariable String id) throws Exception {
		fileManager.deleteFile(id);
	}
}