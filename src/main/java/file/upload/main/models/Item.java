package file.upload.main.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "files")
public class Item {
    @Id
    private String id;

    private String name;

    private String description;

    private List<FileModel> files;
}

