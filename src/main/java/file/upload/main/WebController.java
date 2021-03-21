package file.upload.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import file.upload.main.models.File;
import file.upload.main.models.FileModel;
import file.upload.main.models.Item;
import file.upload.main.models.ItemDTO;
import file.upload.main.repos.FileRepository;
import file.upload.main.repos.ItemRepository;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@RestController
@CrossOrigin(value = "*", maxAge = 3600)
@RequestMapping("/api")
public class WebController {

    @Autowired
    FileRepository fileRepository;

    @Autowired
    ItemRepository itemRepository;

    @SneakyThrows
    private ItemDTO mapDataToJSON(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(data, ItemDTO.class);
    }

    private byte[] compressBytes(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            //
        }
        return outputStream.toByteArray();
    }

    private byte[] decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException | DataFormatException ioe) {
            //
        }
        return outputStream.toByteArray();
    }

    @SneakyThrows
    @PostMapping(value = "/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("data") String itemRequest, @RequestPart("files") List<MultipartFile> files) {
        ItemDTO itemDTO = mapDataToJSON(itemRequest);
        val name = itemDTO.getName() == null ? "" : itemDTO.getName();
        val description = itemDTO.getDescription() == null ? "No description" : itemDTO.getDescription();
        if (itemRepository.existsByName(name) && !name.isBlank() && !name.isEmpty()) {
            return ResponseEntity.badRequest().body(String.format("Name: %s already exists or is blank!", name));
        }
        List<FileModel> formattedFiles = new ArrayList<>();
        for (MultipartFile file: files) {
            String id = UUID.randomUUID().toString();
            fileRepository.save(new File(id, file.getOriginalFilename(), file.getContentType(), compressBytes(file.getBytes())));
            formattedFiles.add(new FileModel(id));
        }
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setFiles(formattedFiles);
        itemRepository.save(item);
        return ResponseEntity.ok().body("Products uploaded!");
    }
    @GetMapping(path = { "/get/{id}" })
    public File getImage(@PathVariable("id") String id) {
        String formattedId = id == null ? "-": id;
            return fileRepository.findById(formattedId).map(file -> {
                return new File(
                        file.getName(),
                   file.getId(),
                   file.getType(),
                   decompressBytes(file.getFile())
                );
            }).orElse(null);
    }
}
