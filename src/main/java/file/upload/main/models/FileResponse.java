package file.upload.main.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.sql.Blob;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    @Id
    private String id;

    private String name;

    private String type;

    private Blob file;
}
