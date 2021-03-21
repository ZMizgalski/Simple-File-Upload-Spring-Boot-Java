package file.upload.main.repos;

import file.upload.main.models.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ItemRepository extends MongoRepository<Item,String> {

    Optional<Item> findItemByName(String name);

    Boolean existsByName(String name);
}
